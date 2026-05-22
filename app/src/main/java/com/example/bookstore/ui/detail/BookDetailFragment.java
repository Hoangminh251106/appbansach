package com.example.bookstore.ui.detail;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.bookstore.data.model.Book;
import com.example.appbansach.databinding.FragmentBookDetailBinding;
import com.example.appbansach.utils.Resource;
import com.example.bookstore.utils.Utils;

import java.util.ArrayList;

public class BookDetailFragment extends Fragment {
    private FragmentBookDetailBinding binding;
    private BookDetailViewModel viewModel;
    private String bookId;
    private Book currentBook;
    private ReviewAdapter reviewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BookDetailViewModel.class);

        setupToolbar();
        setupReviewRecyclerView();
        observeBookDetails();
        observeReviews();

        if (bookId != null) {
            viewModel.loadReviews(bookId);
        }

        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentBook != null) {
                viewModel.addToCart(currentBook);
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBuyNow.setOnClickListener(v -> {
            if (currentBook != null) {
                viewModel.addToCart(currentBook);
                Navigation.findNavController(requireView()).navigate(R.id.cartFragment);
            }
        });
    }

    private void setupToolbar() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        }
    }

    private void setupReviewRecyclerView() {
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void observeBookDetails() {
        if (bookId == null) return;

        viewModel.getBookById(bookId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        currentBook = resource.data;
                        displayBook(currentBook);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void observeReviews() {
        viewModel.reviews.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null && !resource.data.isEmpty()) {
                        reviewAdapter = new ReviewAdapter(resource.data);
                        binding.rvReviews.setAdapter(reviewAdapter);
                        binding.tvNoReviews.setVisibility(View.GONE);
                    } else {
                        binding.tvNoReviews.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    // Silent fail for reviews or show error
                    break;
            }
        });
    }

    private void displayBook(Book book) {
        binding.collapsingToolbar.setTitle(book.getTitle());
        binding.tvDetailTitle.setText(book.getTitle());
        binding.tvDetailAuthor.setText("Tác giả: " + book.getAuthor());
        
        binding.tvDetailPrice.setText(Utils.formatCurrency(book.getPrice()));

        if (book.getOriginalPrice() > book.getPrice()) {
            binding.tvDetailOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvDetailOriginalPrice.setText(Utils.formatCurrency(book.getOriginalPrice()));
            binding.tvDetailOriginalPrice.setPaintFlags(binding.tvDetailOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            binding.tvDetailOriginalPrice.setVisibility(View.GONE);
        }

        binding.detailRatingBar.setRating((float) book.getRating());
        binding.tvDetailReviewCount.setText(book.getReviewCount() + " đánh giá");
        
        if (book.getStock() > 0) {
            binding.tvStockStatus.setText("Còn hàng (" + book.getStock() + ")");
            binding.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            binding.btnAddToCart.setEnabled(true);
            binding.btnBuyNow.setEnabled(true);
        } else {
            binding.tvStockStatus.setText("Hết hàng");
            binding.tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            binding.btnAddToCart.setEnabled(false);
            binding.btnBuyNow.setEnabled(false);
        }

        binding.tvDetailDescription.setText(book.getDescription());

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
