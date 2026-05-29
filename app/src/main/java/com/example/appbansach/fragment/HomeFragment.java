package com.example.appbansach.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.appbansach.adapter.BannerAdapter;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.adapter.CategoryAdapter;
import com.example.appbansach.data.model.GoogleBooksResponse;
import com.example.appbansach.data.repository.BookApiService;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.model.Banner;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    
    private BookAdapter newestAdapter, featuredAdapter, bestSellingAdapter, mostLikedAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    
    private List<Book> newestBooks = new ArrayList<>();
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Book> bestSellingBooks = new ArrayList<>();
    private List<Book> mostLikedBooks = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Banner> bannerList = new ArrayList<>();
    
    private BookApiService apiService;

    private final ActivityResultLauncher<String> getAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) uploadAvatarAsBase64(uri); }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        initRetrofit();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupBanners();
        setupListeners();
        loadUserAvatar();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                cleanAndFixCategories();
                loadCategories();
                loadBanners();
                fetchBooksForHome();
            }
        }, 500);
    }

    private void setupListeners() {
        binding.tvSeeAllFeatured.setOnClickListener(v -> navigateToSearch("văn học Việt Nam"));
        binding.tvSeeAllBestSelling.setOnClickListener(v -> navigateToSearch("kinh tế"));
        binding.tvSeeAllMostLiked.setOnClickListener(v -> navigateToSearch("kỹ năng sống"));
        
        binding.ivUserAvatar.setOnLongClickListener(v -> {
            getAvatarLauncher.launch("image/*");
            return true;
        });
        
        binding.ivUserAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
    }

    private void uploadAvatarAsBase64(Uri uri) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                
                int maxSize = 400;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float ratio = (float) width / (float) height;
                if (ratio > 1) {
                    width = maxSize;
                    height = (int) (width / ratio);
                } else {
                    height = maxSize;
                    width = (int) (height * ratio);
                }
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                db.collection("users").document(uid).update("avatarUrl", base64Image)
                        .addOnSuccessListener(aVoid -> {
                            if (isAdded()) {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void loadUserAvatar() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
                if (doc != null && doc.exists() && isAdded() && binding != null) {
                    String avatarData = doc.getString("avatarUrl");
                    displayAvatar(avatarData, binding.ivUserAvatar);
                }
            });
        }
    }

    private void displayAvatar(String data, ImageView imageView) {
        if (data == null || data.isEmpty() || !isAdded()) return;
        
        if (data.length() > 200) { // Nếu là Base64
            try {
                byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
                Glide.with(this).asBitmap().load(decodedString).circleCrop().into(imageView);
            } catch (Exception e) {
                Log.e("AvatarError", "Error decoding base64");
            }
        } else { // Nếu là URL
            Glide.with(this).load(data).circleCrop().placeholder(R.drawable.ic_profile).into(imageView);
        }
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(BookApiService.class);
    }

    private void setupBanners() {
        bannerAdapter = new BannerAdapter(bannerList, banner -> {});
        binding.viewPagerBanners.setAdapter(bannerAdapter);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(categoryList, category -> navigateToSearch(category.getName()));
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

    private void loadBanners() {
        db.collection("banners").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isAdded()) {
                bannerList.clear();
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Banner banner = doc.toObject(Banner.class);
                        bannerList.add(banner);
                    }
                } else {
                    bannerList.add(new Banner("1", "https://img.freepik.com/free-vector/book-store-banner-template_23-2148685121.jpg"));
                    bannerList.add(new Banner("2", "https://img.freepik.com/free-vector/hand-drawn-book-club-banner_23-2149721453.jpg"));
                }
                bannerAdapter.notifyDataSetChanged();
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
        fetchSection("sách mới", newestBooks, newestAdapter, 3);
    }

    private void fetchSection(String query, List<Book> list, BookAdapter adapter, int sectionIndex) {
        apiService.searchBooks(query, API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleBooksResponse> call, @NonNull Response<GoogleBooksResponse> response) {
                if (!isAdded() || binding == null) return;
                if (sectionIndex == 3) binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<GoogleBooksResponse.Item> items = response.body().getItems();
                    if (items != null) {
                        list.clear();
                        for (GoogleBooksResponse.Item item : items) {
                            Book book = convertToBook(item);
                            if (isValidBook(book)) list.add(book);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override public void onFailure(@NonNull Call<GoogleBooksResponse> call, @NonNull Throwable t) {
                if (isAdded() && binding != null && sectionIndex == 3) binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isValidBook(Book book) {
        return book.getTitle() != null && book.getImageUrl() != null && book.getPrice() > 0;
    }

    private Book convertToBook(GoogleBooksResponse.Item item) {
        Book book = new Book();
        book.setId(item.getId());
        GoogleBooksResponse.VolumeInfo info = item.getVolumeInfo();
        if (info != null) {
            book.setTitle(info.getTitle() != null ? info.getTitle() : "N/A");
            book.setAuthor(info.getAuthors() != null && !info.getAuthors().isEmpty() ? info.getAuthors().get(0) : "Unknown");
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
        return book;
    }

    private void navigateToDetail(Book book) {
        db.collection("books").document(book.getId()).set(book).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Bundle bundle = new Bundle();
                bundle.putString("bookId", book.getId());
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
            }
        });
    }

    private void cleanAndFixCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null && name.equalsIgnoreCase("hello")) {
                    db.collection("categories").document(doc.getId()).delete();
                }
            }
        });
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

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
