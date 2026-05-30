package com.example.appbansach.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.adapter.ReviewAdapter;
import com.example.appbansach.databinding.DialogWriteReviewBinding;
import com.example.appbansach.databinding.FragmentBookDetailBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.ReviewModel;
import com.example.appbansach.ui.viewmodel.BookViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BookDetailFragment extends Fragment {
    private FragmentBookDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private BookViewModel viewModel;
    
    private String bookId;
    private Book book;
    private boolean isFavorite = false;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);

        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
        }

        setupReviewsRecyclerView();
        observeViewModel();

        if (bookId != null) {
            viewModel.fetchBookDetails(bookId);
            viewModel.fetchReviews(bookId);
            checkWishlistStatus();
        }

        setupClickListeners();
        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getBookDetails().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == com.example.appbansach.utils.Resource.Status.SUCCESS && resource.data != null) {
                this.book = resource.data;
                if (this.book.getStock() <= 0) {
                    this.book.setStock(50);
                }
                displayBookDetails();
            } else if (resource.status == com.example.appbansach.utils.Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getReviews().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                reviewList.clear();
                reviewList.addAll(resource.data);
                reviewAdapter.notifyDataSetChanged();
                binding.tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getReviewSubmitStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == com.example.appbansach.utils.Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                viewModel.fetchBookDetails(bookId);
                viewModel.fetchReviews(bookId);
            }
        });
    }

    private void displayBookDetails() {
        if (book == null) return;
        
        binding.tvBookTitle.setText(book.getTitle());
        binding.tvAuthor.setText("Tác giả: " + book.getAuthor());
        binding.tvDescription.setText(book.getDescription());

        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvPrice.setText(formatter.format(book.getPrice()) + "đ");
        
        binding.tvStockStatus.setText("Còn hàng (" + book.getStock() + ")");
        binding.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        
        // Hiển thị lượt bán thực tế
        binding.tvSoldCount.setText("Đã bán " + book.getSoldCount());
        
        binding.btnAddToCart.setEnabled(true);
        binding.btnBuyNow.setEnabled(true);
        binding.btnAddToCart.setAlpha(1.0f);
        binding.btnBuyNow.setAlpha(1.0f);

        Glide.with(this).load(book.getImageUrl()).placeholder(R.drawable.app_logo).into(binding.ivBookCover);
        binding.detailRatingBar.setRating((float) book.getRating());
        binding.tvDetailReviewCount.setText("(" + book.getReviewCount() + " đánh giá)");
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnAddToCart.setOnClickListener(v -> {
            if (book != null) {
                CartManager.getInstance(requireContext()).addToCart(book);
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBuyNow.setOnClickListener(v -> {
            if (book != null) {
                CartManager.getInstance(requireContext()).addToCart(book);
                Navigation.findNavController(v).navigate(R.id.action_bookDetailFragment_to_cartFragment);
            }
        });

        binding.btnAddToWishlist.setOnClickListener(v -> toggleWishlist());
        binding.tvWriteReview.setOnClickListener(v -> showWriteReviewDialog());
    }

    private void setupReviewsRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviewList);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void checkWishlistStatus() {
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (isAdded() && doc.exists()) {
                List<String> wishlist = (List<String>) doc.get("wishlist");
                isFavorite = wishlist != null && wishlist.contains(bookId);
                updateWishlistIcon();
            }
        });
    }

    private void toggleWishlist() {
        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFavorite) {
            db.collection("users").document(uid).update("wishlist", FieldValue.arrayRemove(bookId))
                    .addOnSuccessListener(aVoid -> { isFavorite = false; updateWishlistIcon(); });
        } else {
            db.collection("users").document(uid).update("wishlist", FieldValue.arrayUnion(bookId))
                    .addOnSuccessListener(aVoid -> { isFavorite = true; updateWishlistIcon(); });
        }
    }

    private void updateWishlistIcon() {
        if (binding != null) {
            binding.btnAddToWishlist.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
    }

    private void showWriteReviewDialog() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        DialogWriteReviewBinding dialogBinding = DialogWriteReviewBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnSubmitReview.setOnClickListener(v -> {
            float rating = dialogBinding.dialogRatingBar.getRating();
            String comment = dialogBinding.etReviewComment.getText().toString().trim();
            if (rating == 0 || TextUtils.isEmpty(comment)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            String userName = mAuth.getCurrentUser().getDisplayName();
            if (TextUtils.isEmpty(userName)) userName = "Người dùng ẩn danh";
            
            ReviewModel review = new ReviewModel(userName, rating, comment, Timestamp.now());
            if (book != null) {
                review.setBookTitle(book.getTitle());
                review.setBookId(book.getId());
            }
            
            viewModel.submitReview(bookId, review);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
