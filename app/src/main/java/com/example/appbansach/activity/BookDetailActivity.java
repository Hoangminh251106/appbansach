package com.example.appbansach.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.example.appbansach.databinding.ActivityBookDetailBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;

import java.text.DecimalFormat;

public class BookDetailActivity extends AppCompatActivity {
    private ActivityBookDetailBinding binding;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        book = (Book) getIntent().getSerializableExtra("book");
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        if (book != null) {
            displayBookDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnAddToCart.setOnClickListener(v -> {
            if (book != null) {
                CartManager.getInstance().addToCart(book);
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookDetails() {

        binding.collapsingToolbar.setTitle(book.getTitle());
        binding.tvDetailTitle.setText(book.getTitle());
        binding.tvDetailAuthor.setText("Tác giả: " + book.getAuthor());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvDetailPrice.setText(formatter.format(book.getPrice()) + "đ");
        binding.tvDetailDescription.setText(book.getDescription());

        Glide.with(this)
                .load(book.getImageUrl())
                .into(binding.ivBookLarge);
    }
}
