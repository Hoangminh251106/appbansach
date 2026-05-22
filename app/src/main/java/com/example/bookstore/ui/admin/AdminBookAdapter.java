package com.example.bookstore.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookstore.data.model.Book;
import com.example.appbansach.databinding.ItemAdminBookBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.AdminBookViewHolder> {
    private final List<Book> books;
    private final OnAdminBookClickListener listener;

    public interface OnAdminBookClickListener {
        void onEditClick(Book book);
        void onDeleteClick(Book book);
    }

    public AdminBookAdapter(List<Book> books, OnAdminBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminBookBinding binding = ItemAdminBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminBookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.binding.tvAdminBookTitle.setText(book.getTitle());
        holder.binding.tvAdminBookAuthor.setText(book.getAuthor());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvAdminBookPrice.setText(formatter.format(book.getPrice()));
        holder.binding.tvAdminBookStock.setText("Kho: " + book.getStock());

        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivAdminBookCover);

        holder.binding.btnEditBook.setOnClickListener(v -> listener.onEditClick(book));
        holder.binding.btnDeleteBook.setOnClickListener(v -> listener.onDeleteClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class AdminBookViewHolder extends RecyclerView.ViewHolder {
        ItemAdminBookBinding binding;
        public AdminBookViewHolder(ItemAdminBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}