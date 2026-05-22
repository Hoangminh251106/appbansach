package com.example.bookstore.ui.home;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookstore.data.model.Book;
import com.example.appbansach.databinding.ItemBookBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final List<Book> books;
    private final OnBookClickListener listener;
    private final boolean isHorizontal;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(List<Book> books, boolean isHorizontal, OnBookClickListener listener) {
        this.books = books;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (isHorizontal) {
            ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
            params.width = (int) (parent.getWidth() * 0.45);
            binding.getRoot().setLayoutParams(params);
        }
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.binding.tvBookTitle.setText(book.getTitle());
        holder.binding.tvAuthor.setText(book.getAuthor());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvPrice.setText(formatter.format(book.getPrice()));

        if (book.getOriginalPrice() > book.getPrice()) {
            holder.binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.binding.tvOriginalPrice.setText(formatter.format(book.getOriginalPrice()));
            holder.binding.tvOriginalPrice.setPaintFlags(holder.binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.tvOriginalPrice.setVisibility(View.GONE);
        }

        holder.binding.ratingBar.setRating((float) book.getRating());
        holder.binding.tvRatingCount.setText("(" + book.getReviewCount() + ")");

        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivBookCover);

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ItemBookBinding binding;
        public BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}