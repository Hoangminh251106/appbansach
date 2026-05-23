package com.example.appbansach.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.BannerAdapter;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.adapter.CategoryAdapter;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.model.Banner;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private BookAdapter newestAdapter, featuredAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    private List<Book> newestBooks, featuredBooks;
    private List<Category> categoryList;
    private List<Banner> bannerList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        setupClickListeners();
        loadBanners();
        loadCategories();
        loadBooks();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        // Banners
        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(bannerList, banner -> {
            if (banner.getTargetBookId() != null && !banner.getTargetBookId().isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("bookId", banner.getTargetBookId());
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
            }
        });
        binding.viewPagerBanners.setAdapter(bannerAdapter);

        // Categories
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Bundle bundle = new Bundle();
            bundle.putString("categoryId", category.getId());
            bundle.putString("categoryName", category.getName());
            bundle.putString("type", "category");
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookListFragment, bundle);
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        // Featured Books
        featuredBooks = new ArrayList<>();
        featuredAdapter = new BookAdapter(featuredBooks, this::navigateToDetail);
        binding.rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedBooks.setAdapter(featuredAdapter);

        // Newest Books
        newestBooks = new ArrayList<>();
        newestAdapter = new BookAdapter(newestBooks, this::navigateToDetail);
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setNestedScrollingEnabled(false);
        binding.rvNewestBooks.setAdapter(newestAdapter);
    }

    private void setupClickListeners() {
        // Tìm kiếm
        binding.ivSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchFragment);
        });

        // "Xem tất cả" cho sách nổi bật
        binding.tvSeeAllFeatured.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("type", "featured");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_bookListFragment, bundle);
        });

        // Nút giỏ hàng
        binding.btnCart.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.cartFragment);
        });
    }

    private void navigateToDetail(Book book) {
        Bundle bundle = new Bundle();
        bundle.putString("bookId", book.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
    }

    private void loadBanners() {
        db.collection("banners").orderBy("order").get().addOnSuccessListener(queryDocumentSnapshots -> {
            bannerList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Banner banner = doc.toObject(Banner.class);
                banner.setId(doc.getId());
                bannerList.add(banner);
            }
            bannerAdapter.notifyDataSetChanged();
            if (bannerList.isEmpty()) {
                seedDataOnce();
            }
        });
    }

    private void loadCategories() {
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categoryList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
                if (categoryList.isEmpty()) {
                    seedDataOnce();
                }
            }
        });
    }

    private void loadBooks() {
        db.collection("books").whereEqualTo("featured", true).limit(5).get().addOnSuccessListener(queryDocumentSnapshots -> {
            featuredBooks.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Book book = doc.toObject(Book.class);
                book.setId(doc.getId());
                featuredBooks.add(book);
            }
            featuredAdapter.notifyDataSetChanged();
        });

        db.collection("books").orderBy("createdAt", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            newestBooks.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Book book = doc.toObject(Book.class);
                book.setId(doc.getId());
                newestBooks.add(book);
            }
            newestAdapter.notifyDataSetChanged();
        });
    }

    private void seedDataOnce() {
        Log.d(TAG, "Đang tạo dữ liệu mẫu...");
        
        // Seed Banners
        db.collection("banners").limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                String[] bannerUrls = {
                    "https://img.freepik.com/free-vector/hand-drawn-bookstore-sale-banner_23-2149724103.jpg",
                    "https://img.freepik.com/free-vector/flat-world-book-day-background_23-2149312154.jpg",
                    "https://img.freepik.com/free-vector/flat-world-book-day-vertical-poster-template_23-2149323719.jpg"
                };
                for (int i = 0; i < bannerUrls.length; i++) {
                    Banner b = new Banner();
                    b.setImageUrl(bannerUrls[i]);
                    b.setOrder(i);
                    db.collection("banners").add(b);
                }
                loadBanners();
            }
        });

        // Seed Categories & Books
        db.collection("categories").limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                String[] catNames = {"Văn học", "Kinh tế", "Kỹ năng", "Thiếu nhi"};
                for (String name : catNames) {
                    Category c = new Category("", name, "https://cdn-icons-png.flaticon.com/512/3389/3389081.png");
                    db.collection("categories").add(c).addOnSuccessListener(doc -> {
                        addSampleBooks(doc.getId(), name);
                    });
                }
                Toast.makeText(getContext(), "Đã tạo dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleBooks(String catId, String catName) {
        for (int i = 1; i <= 3; i++) {
            Book b = new Book();
            b.setCategoryId(catId);
            b.setPrice(120000 + (i * 10000));
            b.setOriginalPrice(150000 + (i * 10000));
            b.setFeatured(i == 1);
            b.setStock(50);
            b.setRating(4.0 + (i * 0.2));
            b.setReviewCount(10 * i);
            b.setCreatedAt(Timestamp.now());
            b.setDescription("Đây là mô tả chi tiết cho cuốn sách thuộc thể loại " + catName + ". Cuốn sách mang lại nhiều giá trị kiến thức và cảm hứng cho người đọc trong cuộc sống hiện đại.");
            
            if (catName.equals("Văn học")) {
                b.setTitle("Số Đỏ - Tập " + i);
                b.setAuthor("Vũ Trọng Phụng");
                b.setImageUrl("https://salt.tikicdn.com/cache/w1200/ts/product/d1/7b/72/7b0f69d2d0b4d45d62544259b3f9464e.jpg");
            } else if (catName.equals("Kinh tế")) {
                b.setTitle("Nhà Giả Kim - Phiên bản " + i);
                b.setAuthor("Paulo Coelho");
                b.setImageUrl("https://salt.tikicdn.com/cache/w1200/ts/product/45/3d/81/8f8b248ad0d1300966f368132049d5a7.jpg");
            } else {
                b.setTitle("Sách " + catName + " " + i);
                b.setAuthor("Tác giả " + i);
                b.setImageUrl("https://via.placeholder.com/300x400.png?text=Book+" + i);
            }
            db.collection("books").add(b).addOnSuccessListener(v -> loadBooks());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
