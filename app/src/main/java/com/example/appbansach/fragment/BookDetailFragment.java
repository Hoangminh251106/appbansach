package com.example.appbansach.fragment;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.adapter.ReviewAdapter;
import com.example.appbansach.databinding.DialogWriteReviewBinding;
import com.example.appbansach.databinding.FragmentBookDetailBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Review;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BookDetailFragment extends Fragment {
    private FragmentBookDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String bookId;
    private Book book;
    private boolean isFavorite = false;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
        }

        setupReviewsRecyclerView();

        if (bookId != null) {
            loadBookDetails();
            checkWishlistStatus();
            loadReviews();
        }

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupReviewsRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviewList);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
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
                Navigation.findNavController(v).navigate(R.id.cartFragment);
            }
        });

        binding.fabWishlist.setOnClickListener(v -> toggleWishlist());

        binding.tvWriteReview.setOnClickListener(v -> showWriteReviewDialog());
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

            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(getContext(), "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(rating, comment, dialog);
        });

        dialog.show();
    }

    private void submitReview(float rating, String comment, AlertDialog dialog) {
        String userId = mAuth.getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) userName = "Người dùng ẩn danh";

        Review review = new Review(userName, rating, comment, Timestamp.now());

        db.collection("books").document(bookId).collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadReviews();
                    updateBookOverallRating(rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookOverallRating(float newRating) {
        // Trong thực tế nên dùng Cloud Function hoặc Transaction để tính toán chính xác
        DocumentReference bookRef = db.collection("books").document(bookId);
        db.runTransaction(transaction -> {
            Book bookSnapshot = transaction.get(bookRef).toObject(Book.class);
            if (bookSnapshot != null) {
                int count = bookSnapshot.getReviewCount();
                double currentRating = bookSnapshot.getRating();
                double newAverage = ((currentRating * count) + newRating) / (count + 1);
                
                transaction.update(bookRef, "reviewCount", count + 1);
                transaction.update(bookRef, "rating", newAverage);
            }
            return null;
        });
    }

    private void loadBookDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("books").document(bookId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    book = documentSnapshot.toObject(Book.class);
                    if (book != null) {
                        book.setId(documentSnapshot.getId());
                        displayBookDetails();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi khi tải thông tin sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkWishlistStatus() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                List<String> wishlist = (List<String>) documentSnapshot.get("wishlist");
                isFavorite = wishlist != null && wishlist.contains(bookId);
                updateWishlistFabIcon();
            }
        });
    }

    private void toggleWishlist() {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            db.collection("users").document(userId).update("wishlist", FieldValue.arrayRemove(bookId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateWishlistFabIcon();
                        Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users").document(userId).update("wishlist", FieldValue.arrayUnion(bookId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateWishlistFabIcon();
                        Toast.makeText(getContext(), "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateWishlistFabIcon() {
        binding.fabWishlist.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    private void loadReviews() {
        db.collection("books").document(bookId).collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                        binding.tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void displayBookDetails() {
        binding.collapsingToolbar.setTitle(book.getTitle());
        binding.tvDetailTitle.setText(book.getTitle());
        binding.tvDetailAuthor.setText("Tác giả: " + book.getAuthor());

        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvDetailPrice.setText(formatter.format(book.getPrice()) + "đ");
        
        if (book.getOriginalPrice() > 0) {
            binding.tvDetailOriginalPrice.setText(formatter.format(book.getOriginalPrice()) + "đ");
            binding.tvDetailOriginalPrice.setPaintFlags(binding.tvDetailOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvDetailOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailOriginalPrice.setVisibility(View.GONE);
        }

        binding.tvDetailDescription.setText(book.getDescription());
        binding.detailRatingBar.setRating((float) book.getRating());
        binding.tvDetailReviewCount.setText("(" + book.getReviewCount() + " đánh giá)");
        
        if (book.getStock() > 0) {
            binding.tvStockStatus.setText("Còn hàng (" + book.getStock() + ")");
            binding.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            binding.tvStockStatus.setText("Hết hàng");
            binding.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }

        Glide.with(this)
                .load(book.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivBookLarge);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
