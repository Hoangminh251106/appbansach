package com.example.appbansach.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private BookAdapter newestAdapter, featuredAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Book> newestBooks = new ArrayList<>();
    private List<Book> featuredBooks = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private BookApiService apiService;

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
        setupListeners();
        loadUserAvatar();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                loadCategories();
                fetchBooksForHome("sách tiếng việt"); // Thay đổi query mặc định sang tiếng Việt
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
            bundle.putString("category_name", category.getName());
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, bundle);
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        featuredAdapter = new BookAdapter(featuredBooks, this::navigateToDetail);
        binding.rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedBooks.setAdapter(featuredAdapter);

        newestAdapter = new BookAdapter(newestBooks, this::navigateToDetail);
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setAdapter(newestAdapter);
    }

    private void setupListeners() {
        binding.tvSeeAllFeatured.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("query", "sách hay nhất");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchFragment, bundle);
        });
    }

    private void fetchBooksForHome(String query) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.searchBooks(query, API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleBooksResponse> call, @NonNull Response<GoogleBooksResponse> response) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<GoogleBooksResponse.Item> items = response.body().getItems();
                    if (items != null) {
                        List<Book> filteredBooks = new ArrayList<>();
                        for (GoogleBooksResponse.Item item : items) {
                            Book book = convertToBook(item);
                            
                            // Lọc sách Tiếng Việt: Kiểm tra title chứa dấu tiếng Việt hoặc các từ thông dụng
                            if (isVietnamese(book.getTitle()) || isVietnamese(book.getDescription())) {
                                filteredBooks.add(book);
                            }
                        }
                        
                        // Nếu sau khi lọc quá ít, lấy thêm sách phổ thông để tránh trắng màn hình
                        if (filteredBooks.size() < 4) {
                           for (GoogleBooksResponse.Item item : items) {
                               Book b = convertToBook(item);
                               if (!filteredBooks.contains(b)) filteredBooks.add(b);
                               if (filteredBooks.size() >= 10) break;
                           }
                        }

                        updateAdapters(filteredBooks);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleBooksResponse> call, @NonNull Throwable t) {
                if (isAdded() && binding != null) binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isVietnamese(String text) {
        if (text == null) return false;
        // Kiểm tra cơ bản: chứa các nguyên âm có dấu của tiếng Việt
        String regex = ".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*";
        return text.toLowerCase().matches(regex);
    }

    private void updateAdapters(List<Book> allBooks) {
        if (allBooks.size() >= 4) {
            featuredBooks.clear();
            featuredBooks.addAll(allBooks.subList(0, 4));
            featuredAdapter.notifyDataSetChanged();
            
            newestBooks.clear();
            newestBooks.addAll(allBooks.subList(4, Math.min(allBooks.size(), 12)));
            newestAdapter.notifyDataSetChanged();
        }
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
                        Glide.with(this).load(url).placeholder(android.R.drawable.ic_menu_report_image).into(binding.ivUserAvatar);
                    }
                }
            });
        }
        binding.ivUserAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
    }

    private void loadCategories() {
        db.collection("categories").limit(8).get().addOnSuccessListener(value -> {
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
            book.setPrice(85000 + (long)(Math.random() * 50000));
        }
        book.setStock(100);
        return book;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
