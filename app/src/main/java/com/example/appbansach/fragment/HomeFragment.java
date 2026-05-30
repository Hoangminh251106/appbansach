package com.example.appbansach.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.adapter.BannerAdapter;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.adapter.CategoryAdapter;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.model.Banner;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.example.appbansach.ui.viewmodel.BookViewModel;
import com.example.appbansach.utils.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private BookViewModel viewModel;
    private SharedPreferences sharedPreferences;
    
    private BookAdapter newestAdapter, featuredAdapter, bestSellingAdapter, mostLikedAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    
    private List<Book> newestBooks = new ArrayList<>();
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Book> bestSellingBooks = new ArrayList<>();
    private List<Book> mostLikedBooks = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Banner> bannerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);
        sharedPreferences = requireContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupListeners();
        loadUserAvatar();
        setupMarquee();
        setupNotificationBadge();
        
        observeViewModel();
        loadCategories();
        loadBanners();
        refreshData();
    }

    private void setupNotificationBadge() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getUid();

        // Lắng nghe thông báo chưa đọc (bao gồm tin riêng và tin chung "all")
        db.collection("notifications")
                .whereIn("userId", Arrays.asList(uid, "all"))
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (value != null && binding != null) {
                        int count = value.size();
                        if (count > 0) {
                            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                            binding.tvNotificationBadge.setText(String.valueOf(count));
                        } else {
                            binding.tvNotificationBadge.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void setupMarquee() {
        boolean isMarqueeEnabled = sharedPreferences.getBoolean("marquee_enabled", true);
        if (!isMarqueeEnabled) {
            binding.layoutMarquee.setVisibility(View.GONE);
            return;
        }

        db.collection("system").document("broadcast").addSnapshotListener((value, error) -> {
            if (value != null && value.exists() && binding != null) {
                Boolean active = value.getBoolean("active");
                String content = value.getString("content");
                if (active != null && active && content != null) {
                    binding.layoutMarquee.setVisibility(View.VISIBLE);
                    binding.tvMarquee.setText(content);
                    binding.tvMarquee.setSelected(true);
                } else {
                    binding.layoutMarquee.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupListeners() {
        binding.ivUserAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
        binding.layoutNotification.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.notificationsFragment));
        binding.fabChat.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.chatFragment));
        
        binding.tvSeeAllFeatured.setOnClickListener(v -> navigateToSearch("Sách hay"));
        binding.tvSeeAllBestSelling.setOnClickListener(v -> navigateToSearch("Kinh tế"));
    }

    private void navigateToSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, bundle);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Bundle bundle = new Bundle();
            bundle.putString("category_name", category.getName());
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, bundle);
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        featuredAdapter = new BookAdapter(featuredBooks, this::navigateToDetail);
        binding.rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedBooks.setAdapter(featuredAdapter);

        bestSellingAdapter = new BookAdapter(bestSellingBooks, this::navigateToDetail);
        binding.rvBestSellingBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBestSellingBooks.setAdapter(bestSellingAdapter);

        mostLikedAdapter = new BookAdapter(mostLikedBooks, this::navigateToDetail);
        binding.rvMostLikedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvMostLikedBooks.setAdapter(mostLikedAdapter);

        newestAdapter = new BookAdapter(newestBooks, this::navigateToDetail);
        newestAdapter.setGridMode(true);
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setAdapter(newestAdapter);
    }

    private void navigateToDetail(Book book) {
        Bundle bundle = new Bundle();
        bundle.putString("bookId", book.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
    }

    private void observeViewModel() {
        viewModel.getFeaturedBooks().observe(getViewLifecycleOwner(), resource -> 
            handleBookResource(resource, featuredBooks, featuredAdapter, binding.shimmerFeatured.getRoot(), binding.rvFeaturedBooks));
        viewModel.getBestSellingBooks().observe(getViewLifecycleOwner(), resource -> 
            handleBookResource(resource, bestSellingBooks, bestSellingAdapter, binding.shimmerBestSelling.getRoot(), binding.rvBestSellingBooks));
        viewModel.getMostLikedBooks().observe(getViewLifecycleOwner(), resource -> 
            handleBookResource(resource, mostLikedBooks, mostLikedAdapter, null, binding.rvMostLikedBooks));
        viewModel.getNewestBooks().observe(getViewLifecycleOwner(), resource -> 
            handleBookResource(resource, newestBooks, newestAdapter, binding.shimmerNewest, binding.rvNewestBooks));
    }

    private void handleBookResource(Resource<List<Book>> resource, List<Book> list, BookAdapter adapter, View shimmerView, View recyclerView) {
        if (resource == null || binding == null) return;
        if (resource.status == Resource.Status.LOADING) {
            if (shimmerView != null) shimmerView.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else if (resource.status == Resource.Status.SUCCESS) {
            if (shimmerView != null) shimmerView.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            if (resource.data != null) {
                list.clear();
                list.addAll(resource.data);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void loadUserAvatar() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
                if (doc != null && doc.exists() && isAdded() && binding != null) {
                    String avatarUrl = doc.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Object source = avatarUrl.length() > 300 ? Base64.decode(avatarUrl, Base64.DEFAULT) : avatarUrl;
                        Glide.with(this).load(source).circleCrop().placeholder(R.drawable.ic_profile).into(binding.ivUserAvatar);
                    }
                }
            });
        }
    }

    private void loadCategories() {
        db.collection("categories").orderBy("name").get().addOnSuccessListener(value -> {
            if (value != null && isAdded()) {
                categoryList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Category cat = doc.toObject(Category.class);
                    cat.setId(doc.getId());
                    categoryList.add(cat);
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadBanners() {
        bannerList.clear();
        bannerList.add(new Banner("v1", "banner_bookstore")); // Cập nhật sang banner mới (viết thường)
        bannerAdapter = new BannerAdapter(bannerList, banner -> {});
        binding.viewPagerBanners.setAdapter(bannerAdapter);
    }

    private void refreshData() {
        viewModel.fetchFeaturedBooks();
        viewModel.fetchBestSellingBooks();
        viewModel.fetchMostLikedBooks();
        viewModel.fetchNewestBooks();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
