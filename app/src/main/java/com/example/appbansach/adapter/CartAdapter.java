package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.databinding.ItemCartBinding;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItemEntity> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChange(CartItemEntity item, int newQuantity);
        void onRemoveItem(CartItemEntity item);
    }

    public CartAdapter(List<CartItemEntity> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void setData(List<CartItemEntity> newData) {
        this.cartItems = newData;
        notifyDataSetChanged();
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
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvCartPrice.setText(formatter.format(item.getPrice()) + "đ");
        holder.binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivCartBook);

        holder.binding.btnPlus.setOnClickListener(v -> {
            listener.onQuantityChange(item, item.getQuantity() + 1);
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChange(item, item.getQuantity() - 1);
            } else {
                listener.onRemoveItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
