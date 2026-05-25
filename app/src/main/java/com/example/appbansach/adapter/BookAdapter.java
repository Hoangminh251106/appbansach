package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ItemBookBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> bookList;
    private OnBookClickListener listener;
    private List<String> wishlistIds = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
        fetchWishlist();
    }

    private void fetchWishlist() {
        if (userId == null) return;
        db.collection("users").document(userId).addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                List<String> list = (List<String>) value.get("wishlist");
                if (list != null) {
                    wishlistIds = list;
                    notifyDataSetChanged();
                }
            }
        });
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.binding.tvBookTitle.setText(book.getTitle());
        holder.binding.tvAuthor.setText(book.getAuthor());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvPrice.setText(formatter.format(book.getPrice()) + "đ");

        if (book.getOriginalPrice() > book.getPrice()) {
            holder.binding.tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
            holder.binding.tvOriginalPrice.setText(formatter.format(book.getOriginalPrice()) + "đ");
            holder.binding.tvOriginalPrice.setPaintFlags(holder.binding.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }

        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivBookCover);
        
        // Xử lý Wishlist icon
        boolean isFavorite = wishlistIds.contains(book.getId());
        holder.binding.btnWishlist.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        
        holder.binding.btnWishlist.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(v.getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isFavorite) {
                db.collection("users").document(userId).update("wishlist", FieldValue.arrayRemove(book.getId()));
            } else {
                db.collection("users").document(userId).update("wishlist", FieldValue.arrayUnion(book.getId()));
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ItemBookBinding binding;
        public BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
