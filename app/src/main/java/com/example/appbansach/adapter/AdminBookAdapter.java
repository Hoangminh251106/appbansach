package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.ItemAdminBookBinding;
import com.example.appbansach.model.Book;

import java.text.DecimalFormat;
import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.AdminBookViewHolder> {
    private List<Book> bookList;
    private OnBookActionListener listener;

    public interface OnBookActionListener {
        void onEdit(Book book);
        void onDelete(Book book);
    }

    public AdminBookAdapter(List<Book> bookList, OnBookActionListener listener) {
        this.bookList = bookList;
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
        Book book = bookList.get(position);
        holder.binding.tvAdminBookTitle.setText(book.getTitle());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvAdminBookPrice.setText(formatter.format(book.getPrice()) + "đ");
        holder.binding.tvAdminBookStock.setText("Kho: " + book.getStock());

        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivAdminBookCover);

        holder.binding.btnEditBook.setOnClickListener(v -> listener.onEdit(book));
        holder.binding.btnDeleteBook.setOnClickListener(v -> listener.onDelete(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class AdminBookViewHolder extends RecyclerView.ViewHolder {
        ItemAdminBookBinding binding;
        public AdminBookViewHolder(ItemAdminBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
