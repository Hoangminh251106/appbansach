package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.activity.CheckoutActivity;
import com.example.appbansach.adapter.ReviewAdapter;
import com.example.appbansach.databinding.FragmentBookDetailBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Review;
import com.example.appbansach.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookDetailFragment extends Fragment {
    private FragmentBookDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String bookId;
    private Book currentBook;
    private boolean isFavorite = false;
    private List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
        }

        binding.tvWriteReview.setVisibility(View.GONE);

        if (bookId != null) {
            loadBookDetails();
            checkIfFavorite();
            loadReviews();
            checkIfCanReview();
        }

        setupListeners();
        setupRecyclerView();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviewList);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void setupListeners() {
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        binding.btnAddToWishlist.setOnClickListener(v -> toggleFavorite());

        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentBook != null) {
                CartManager.getInstance(requireContext()).addToCart(currentBook);
                CartManager.getInstance(requireContext()).updateSelection(currentBook.getId(), true);
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBuyNow.setOnClickListener(v -> {
            if (currentBook != null) {
                CartManager.getInstance(requireContext()).addToCart(currentBook);
                CartManager.getInstance(requireContext()).updateSelection(currentBook.getId(), true);
                startActivity(new Intent(getActivity(), CheckoutActivity.class));
            }
        });

        binding.tvWriteReview.setOnClickListener(v -> showWriteReviewDialog());
    }

    private void checkIfCanReview() {
        if (mAuth.getUid() == null) return;

        db.collection("orders")
                .whereEqualTo("userId", mAuth.getUid())
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                if (bookId.equals(item.get("bookId"))) {
                                    binding.tvWriteReview.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        }
                    }
                });
    }

    private void showWriteReviewDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_write_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);

        new AlertDialog.Builder(requireContext())
                .setTitle("Viết đánh giá")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();
                    if (rating > 0) {
                        submitReview(rating, comment);
                    } else {
                        Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReview(float rating, String comment) {
        String uid = mAuth.getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            User user = userDoc.toObject(User.class);
            String userName = (user != null) ? user.getFullName() : "Người dùng";
            String userAvatar = (user != null) ? user.getAvatarUrl() : "";

            Review review = new Review();
            review.setBookId(bookId);
            review.setUserId(uid);
            review.setUserName(userName);
            review.setUserAvatar(userAvatar);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(Timestamp.now());

            db.collection("reviews").add(review).addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                loadReviews();
                updateBookRating(rating);
            });
        });
    }

    private void updateBookRating(float newRating) {
        db.collection("books").document(bookId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                double currentRating = doc.getDouble("rating") != null ? doc.getDouble("rating") : 0;
                int count = doc.getLong("reviewCount") != null ? doc.getLong("reviewCount").intValue() : 0;
                
                double updatedRating = (currentRating * count + newRating) / (count + 1);
                db.collection("books").document(bookId).update(
                        "rating", updatedRating,
                        "reviewCount", count + 1
                );
            }
        });
    }

    private void loadReviews() {
        db.collection("reviews")
                .whereEqualTo("bookId", bookId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            reviewList.add(doc.toObject(Review.class));
                        }
                        reviewAdapter.notifyDataSetChanged();
                        binding.tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadBookDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("books").document(bookId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                binding.progressBar.setVisibility(View.GONE);
                currentBook = documentSnapshot.toObject(Book.class);
                if (currentBook != null) {
                    displayBook();
                }
            }
        });
    }

    private void displayBook() {
        binding.tvBookTitle.setText(currentBook.getTitle());
        binding.tvAuthor.setText(currentBook.getAuthor());
        binding.tvDescription.setText(currentBook.getDescription());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvPrice.setText(formatter.format(currentBook.getPrice()) + "đ");

        if (currentBook.getOriginalPrice() > currentBook.getPrice()) {
            binding.tvDetailOriginalPrice.setText(formatter.format(currentBook.getOriginalPrice()) + "đ");
            binding.tvDetailOriginalPrice.setPaintFlags(binding.tvDetailOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            binding.tvDetailOriginalPrice.setVisibility(View.GONE);
        }

        binding.detailRatingBar.setRating((float) currentBook.getRating());
        binding.tvDetailReviewCount.setText("(" + currentBook.getReviewCount() + " đánh giá)");
        binding.tvStockStatus.setText("Kho: " + currentBook.getStock());

        Glide.with(this)
                .load(currentBook.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivBookCover);
    }

    private void checkIfFavorite() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists() && isAdded()) {
                List<String> wishlist = (List<String>) doc.get("wishlist");
                isFavorite = wishlist != null && wishlist.contains(bookId);
                updateFavoriteUI();
            }
        });
    }

    private void updateFavoriteUI() {
        binding.btnAddToWishlist.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void toggleFavorite() {
        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            db.collection("users").document(uid).update("wishlist", FieldValue.arrayRemove(bookId))
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").document(uid).update("wishlist", FieldValue.arrayUnion(bookId))
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã thêm vào yêu thích ❤️", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
