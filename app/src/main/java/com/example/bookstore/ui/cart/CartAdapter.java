package com.example.bookstore.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookstore.data.local.CartItemEntity;
import com.example.appbansach.databinding.ItemCartBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<CartItemEntity> cartItems;
    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChange(String bookId, int newQuantity);
        void onRemoveItem(String bookId);
    }

    public CartAdapter(List<CartItemEntity> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemEntity item = cartItems.get(position);
        holder.binding.tvCartTitle.setText(item.getTitle());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvCartPrice.setText(formatter.format(item.getPrice()));
        holder.binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .into(holder.binding.ivCartBook);

        holder.binding.btnPlus.setOnClickListener(v -> 
            listener.onQuantityChange(item.getBookId(), item.getQuantity() + 1)
        );

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChange(item.getBookId(), item.getQuantity() - 1);
            }
        });

        holder.binding.btnRemove.setOnClickListener(v -> listener.onRemoveItem(item.getBookId()));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}