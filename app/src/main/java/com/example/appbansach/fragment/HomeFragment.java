package com.example.appbansach.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.adapter.CategoryAdapter;
import com.example.appbansach.data.model.GoogleBooksResponse;
import com.example.appbansach.data.repository.BookApiService;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private static final String API_KEY = "AIzaSyCFaQyaHAn-95mXvT_2tFNqC2ugEGhquv8";
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private BookAdapter newestAdapter, featuredAdapter, bestSellingAdapter, mostLikedAdapter;
    private CategoryAdapter categoryAdapter;
    
    private List<Book> newestBooks = new ArrayList<>();
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Book> bestSellingBooks = new ArrayList<>();
    private List<Book> mostLikedBooks = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private BookApiService apiService;

    // Launcher for picking avatar from device
    private final ActivityResultLauncher<String> getAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        initRetrofit();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupListeners();
        loadUserAvatar();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                cleanAndFixCategories();
                loadCategories();
                fetchBooksForHome();
            }
        }, 500);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(BookApiService.class);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Bundle bundle = new Bundle();
            bundle.putString("query", category.getName());
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

    private void setupListeners() {
        binding.tvSeeAllFeatured.setOnClickListener(v -> navigateToSearch("văn học Việt Nam"));
        binding.tvSeeAllBestSelling.setOnClickListener(v -> navigateToSearch("kinh tế"));
        binding.tvSeeAllMostLiked.setOnClickListener(v -> navigateToSearch("kỹ năng sống"));
        
        // Long click to change avatar from laptop/device
        binding.ivUserAvatar.setOnLongClickListener(v -> {
            getAvatarLauncher.launch("image/*");
            return true;
        });
        
        binding.ivUserAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
    }

    private void uploadAvatar(Uri uri) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        StorageReference ref = storage.getReference().child("avatars/" + uid + ".jpg");
        ref.putFile(uri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            db.collection("users").document(uid).update("avatarUrl", downloadUri.toString())
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                            Glide.with(this).load(downloadUri).into(binding.ivUserAvatar);
                        }
                    });
        })).addOnFailureListener(e -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, bundle);
    }

    private void fetchBooksForHome() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        fetchSection("văn học Việt Nam", featuredBooks, featuredAdapter, 0);
        fetchSection("kinh tế", bestSellingBooks, bestSellingAdapter, 1);
        fetchSection("kỹ năng sống", mostLikedBooks, mostLikedAdapter, 2);
        fetchSection("sách mới xuất bản", newestBooks, newestAdapter, 3);
    }

    private void fetchSection(String query, List<Book> list, BookAdapter adapter, int sectionIndex) {
        apiService.searchBooks(query, API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleBooksResponse> call, @NonNull Response<GoogleBooksResponse> response) {
                if (!isAdded() || binding == null) return;
                
                if (sectionIndex == 3) {
                    binding.progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<GoogleBooksResponse.Item> items = response.body().getItems();
                    if (items != null) {
                        list.clear();
                        for (GoogleBooksResponse.Item item : items) {
                            Book book = convertToBook(item);
                            // Only add books that have essential information to avoid "errors"
                            if (isValidBook(book)) {
                                list.add(book);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleBooksResponse> call, @NonNull Throwable t) {
                if (isAdded() && binding != null && sectionIndex == 3) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean isValidBook(Book book) {
        return book.getTitle() != null && !book.getTitle().equals("N/A") 
                && book.getImageUrl() != null && !book.getImageUrl().isEmpty()
                && book.getPrice() > 0;
    }

    private Book convertToBook(GoogleBooksResponse.Item item) {
        Book book = new Book();
        book.setId(item.getId());
        GoogleBooksResponse.VolumeInfo info = item.getVolumeInfo();
        if (info != null) {
            book.setTitle(info.getTitle() != null ? info.getTitle() : "N/A");
            book.setAuthor(info.getAuthors() != null && !info.getAuthors().isEmpty() ? info.getAuthors().get(0) : "Unknown");
            book.setDescription(info.getDescription());
            if (info.getImageLinks() != null) {
                String url = info.getImageLinks().getThumbnail();
                if (url != null) book.setImageUrl(url.replace("http://", "https://"));
            }
        }
        if (item.getSaleInfo() != null && item.getSaleInfo().getListPrice() != null) {
            book.setPrice((long) item.getSaleInfo().getListPrice().getAmount());
        } else {
            book.setPrice(85000 + (long)(Math.random() * 150000));
        }
        book.setStock(100);
        return book;
    }

    private void navigateToDetail(Book book) {
        db.collection("books").document(book.getId()).set(book)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("bookId", book.getId());
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
                    }
                });
    }

    private void loadUserAvatar() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
                if (doc != null && doc.exists() && isAdded() && binding != null) {
                    String url = doc.getString("avatarUrl");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this).load(url).circleCrop().placeholder(R.drawable.ic_profile).into(binding.ivUserAvatar);
                    }
                }
            });
        }
    }

    private void cleanAndFixCategories() {
        // Delete "hello" category and fix icons for others
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) {
                    if (name.equalsIgnoreCase("hello")) {
                        db.collection("categories").document(doc.getId()).delete();
                    } else {
                        updateCategoryIcon(doc.getId(), name);
                    }
                }
            }
        });
    }

    private void updateCategoryIcon(String id, String name) {
        String iconUrl = "";
        String n = name.toLowerCase();
        if (n.contains("văn học") || n.contains("tiểu thuyết")) iconUrl = "https://cdn-icons-png.flaticon.com/512/3389/3389081.png";
        else if (n.contains("kinh tế") || n.contains("tài chính")) iconUrl = "https://cdn-icons-png.flaticon.com/512/2761/2761118.png";
        else if (n.contains("kỹ năng") || n.contains("self help")) iconUrl = "https://cdn-icons-png.flaticon.com/512/3079/3079204.png";
        else if (n.contains("thiếu nhi") || n.contains("trẻ em")) iconUrl = "https://cdn-icons-png.flaticon.com/512/3082/3082060.png";
        else if (n.contains("ngoại ngữ")) iconUrl = "https://cdn-icons-png.flaticon.com/512/3898/3898082.png";
        else if (n.contains("khoa học")) iconUrl = "https://cdn-icons-png.flaticon.com/512/2103/2103633.png";
        else if (n.contains("lịch sử")) iconUrl = "https://cdn-icons-png.flaticon.com/512/2234/2234668.png";
        else iconUrl = "https://cdn-icons-png.flaticon.com/512/2436/2436636.png";

        db.collection("categories").document(id).update("iconUrl", iconUrl);
    }

    private void loadCategories() {
        db.collection("categories").limit(12).get().addOnSuccessListener(value -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
