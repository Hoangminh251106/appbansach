package com.example.appbansach.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.data.model.GoogleBooksResponse;
import com.example.appbansach.data.repository.BookApiService;
import com.example.appbansach.databinding.FragmentSearchBinding;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {
    private static final String API_KEY = "AIzaSyCFaQyaHAn-95mXvT_2tFNqC2ugEGhquv8";
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";

    private FragmentSearchBinding binding;
    private BookAdapter adapter;
    private final List<Book> fullList = new ArrayList<>();
    private final List<Book> displayList = new ArrayList<>();
    private FirebaseFirestore db;
    private BookApiService apiService;
    
    private float minPrice = 0;
    private float maxPrice = 1000000;
    
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        initRetrofit();
        loadCategories();

        // Xử lý tham số truyền từ Trang chủ
        if (getArguments() != null) {
            if (getArguments().containsKey("category_name")) {
                String category = getArguments().getString("category_name");
                loadBooksByCategory(category);
            } else if (getArguments().containsKey("query")) {
                String query = getArguments().getString("query");
                binding.etSearch.setText(query);
                executeApiCall(query);
            } else {
                loadBooksByCategory("Fiction");
            }
        } else {
            loadBooksByCategory("Fiction");
        }
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(BookApiService.class);
    }

    private void loadBooksByCategory(String categoryName) {
        if (binding != null) {
            binding.etSearch.setText(categoryName);
            // Sửa lỗi: Không dùng "subject:" để tìm kiếm rộng hơn theo ý người dùng
            executeApiCall(categoryName);
        }
    }

    private void executeApiCall(String query) {
        if (binding == null || apiService == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        
        apiService.searchBooks(query, API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleBooksResponse> call, @NonNull Response<GoogleBooksResponse> response) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<GoogleBooksResponse.Item> items = response.body().getItems();
                    fullList.clear();
                    if (items != null) {
                        for (GoogleBooksResponse.Item item : items) {
                            fullList.add(convertToBook(item));
                        }
                    }
                    updateUiList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleBooksResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI() {
        adapter = new BookAdapter(displayList, book -> {
            db.collection("books").document(book.getId()).set(book)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded() && binding != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("bookId", book.getId());
                            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_bookDetailFragment, bundle);
                        }
                    });
        });
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> executeApiCall(query);
                    searchHandler.postDelayed(searchRunnable, 1000);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            updateUiList();
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
            book.setPrice(75000 + (long)(Math.random() * 80000));
        }
        book.setStock(100);
        return book;
    }

    private void updateUiList() {
        if (binding == null) return;
        displayList.clear();
        for (Book b : fullList) {
            if (b.getPrice() >= minPrice && b.getPrice() <= maxPrice) {
                displayList.add(b);
            }
        }
        adapter.notifyDataSetChanged();
        binding.layoutNoResults.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded() || binding == null) return;
            binding.chipGroupCategories.removeAllViews();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name == null) continue;
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_category, binding.chipGroupCategories, false);
                chip.setText(name);
                chip.setOnClickListener(v -> loadBooksByCategory(name));
                binding.chipGroupCategories.addView(chip);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        searchHandler.removeCallbacksAndMessages(null);
    }
}
