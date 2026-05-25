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
        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(bannerList, banner -> {
            if (banner.getTargetBookId() != null && !banner.getTargetBookId().isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("bookId", banner.getTargetBookId());
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
            }
        });
        binding.viewPagerBanners.setAdapter(bannerAdapter);

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

        featuredBooks = new ArrayList<>();
        featuredAdapter = new BookAdapter(featuredBooks, this::navigateToDetail);
        binding.rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedBooks.setAdapter(featuredAdapter);

        newestBooks = new ArrayList<>();
        newestAdapter = new BookAdapter(newestBooks, this::navigateToDetail);
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setNestedScrollingEnabled(false);
        binding.rvNewestBooks.setAdapter(newestAdapter);
    }

    private void setupClickListeners() {
        binding.ivSearch.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchFragment));
        binding.tvSeeAllFeatured.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("type", "featured");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_bookListFragment, bundle);
        });
        binding.btnCart.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.cartFragment));
        
        // Nhấn Banner để nạp dữ liệu thật nếu cần
        binding.viewPagerBanners.setOnClickListener(v -> {
            seedDataOnce();
            Toast.makeText(getContext(), "Đang nạp dữ liệu sách thật...", Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateToDetail(Book book) {
        Bundle bundle = new Bundle();
        bundle.putString("bookId", book.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
    }

    private void loadBanners() {
        db.collection("banners").orderBy("order").addSnapshotListener((value, error) -> {
            if (value != null) {
                bannerList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Banner b = doc.toObject(Banner.class);
                    b.setId(doc.getId());
                    bannerList.add(b);
                }
                bannerAdapter.notifyDataSetChanged();
                if (bannerList.isEmpty()) seedDataOnce();
            }
        });
    }

    private void loadCategories() {
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (value != null) {
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

    private void loadBooks() {
        db.collection("books").whereEqualTo("featured", true).limit(10).addSnapshotListener((value, error) -> {
            if (value != null) {
                featuredBooks.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Book b = doc.toObject(Book.class);
                    b.setId(doc.getId());
                    featuredBooks.add(b);
                }
                featuredAdapter.notifyDataSetChanged();
            }
        });

        db.collection("books").orderBy("createdAt", Query.Direction.DESCENDING).limit(20).addSnapshotListener((value, error) -> {
            if (value != null) {
                newestBooks.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Book b = doc.toObject(Book.class);
                    b.setId(doc.getId());
                    newestBooks.add(b);
                }
                newestAdapter.notifyDataSetChanged();
            }
        });
    }

    private void seedDataOnce() {
        db.collection("categories").limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                // Tạo danh mục
                String[] cats = {"Kinh điển", "Kinh tế", "Kỹ năng sống", "Văn học"};
                for (String name : cats) {
                    Category c = new Category("", name, "https://cdn-icons-png.flaticon.com/512/3389/3389081.png");
                    db.collection("categories").add(c).addOnSuccessListener(doc -> addProBooks(doc.getId(), name));
                }
                
                // Tạo banner
                String[] bannerUrls = {
                    "https://img.freepik.com/free-vector/hand-drawn-bookstore-sale-banner_23-2149724103.jpg",
                    "https://img.freepik.com/free-vector/flat-world-book-day-background_23-2149312154.jpg"
                };
                for (int i = 0; i < bannerUrls.length; i++) {
                    Banner b = new Banner();
                    b.setImageUrl(bannerUrls[i]);
                    b.setOrder(i);
                    db.collection("banners").add(b);
                }
            }
        });
    }

    private void addProBooks(String catId, String catName) {
        if (catName.equals("Kinh điển")) {
            createBook(catId, "Nhà Giả Kim", "Paulo Coelho", 79000, 99000, 
                "https://salt.tikicdn.com/cache/w1200/ts/product/45/3d/81/8f8b248ad0d1300966f368132049d5a7.jpg",
                "Tất cả những trải nghiệm trong chuyến phiêu lưu theo đuổi định mệnh của Santiago đã giúp anh thấu hiểu được ý nghĩa sâu xa nhất của hạnh phúc, hòa hợp với vũ trụ và con người. Cuốn sách là một trong những tác phẩm bán chạy nhất mọi thời đại.", true);
        } else if (catName.equals("Kỹ năng sống")) {
            createBook(catId, "Đắc Nhân Tâm", "Dale Carnegie", 85000, 110000,
                "https://salt.tikicdn.com/cache/w1200/ts/product/7e/1d/94/f9f1092a7f0e34c9c1b9708709f19616.jpg",
                "Đắc Nhân Tâm là cuốn sách nổi tiếng nhất, có tầm ảnh hưởng nhất mọi thời đại. Cuốn sách đã được chuyển ngữ sang hầu hết các thứ tiếng trên thế giới và có mặt ở hàng trăm quốc gia.", true);
        } else {
            createBook(catId, "Sách hay: " + catName, "Tác giả uy tín", 120000, 150000, 
                "https://via.placeholder.com/300x400.png?text=Book",
                "Đây là cuốn sách tuyệt vời giúp bạn mở mang kiến thức về " + catName + ". Sách trình bày chi tiết và có nhiều ví dụ thực tiễn.", false);
        }
    }

    private void createBook(String catId, String title, String author, long price, long oldPrice, String url, String desc, boolean featured) {
        Book b = new Book();
        b.setCategoryId(catId);
        b.setTitle(title);
        b.setAuthor(author);
        b.setPrice(price);
        b.setOriginalPrice(oldPrice);
        b.setImageUrl(url);
        b.setDescription(desc);
        b.setFeatured(featured);
        b.setStock(100);
        b.setRating(4.8);
        b.setReviewCount(152);
        b.setCreatedAt(Timestamp.now());
        b.setNew(true);
        db.collection("books").add(b);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
