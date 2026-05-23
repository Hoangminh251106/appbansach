package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.FragmentBookDetailBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class BookDetailFragment extends Fragment {
    private FragmentBookDetailBinding binding;
    private FirebaseFirestore db;
    private String bookId;
    private Book book;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
        }

        if (bookId != null) {
            loadBookDetails();
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnAddToCart.setOnClickListener(v -> {
            if (book != null) {
                CartManager.getInstance().addToCart(book);
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void loadBookDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("Books").document(bookId).get().addOnSuccessListener(documentSnapshot -> {
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
                .into(binding.ivBookLarge);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
