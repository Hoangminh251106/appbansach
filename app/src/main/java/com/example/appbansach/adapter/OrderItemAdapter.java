package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.ItemCartBinding; // Reuse item_cart layout for order items
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<Map<String, Object>> itemList;

    public OrderItemAdapter(List<Map<String, Object>> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Map<String, Object> item = itemList.get(position);
        holder.binding.tvCartTitle.setText((String) item.get("title"));
        
        Object price = item.get("price");
        long priceVal = 0;
        if (price instanceof Long) priceVal = (Long) price;
        else if (price instanceof Integer) priceVal = (Integer) price;

        Object quantity = item.get("quantity");
        int qtyVal = 0;
        if (quantity instanceof Long) qtyVal = ((Long) quantity).intValue();
        else if (quantity instanceof Integer) qtyVal = (Integer) quantity;

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvCartPrice.setText(formatter.format(priceVal) + "đ");
        holder.binding.tvQuantity.setText("x" + qtyVal);

        Glide.with(holder.itemView.getContext())
                .load((String) item.get("imageUrl"))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivCartBook);
        
        // Hide quantity controls since it's read-only in order detail
        holder.binding.btnPlus.setVisibility(android.view.View.GONE);
        holder.binding.btnMinus.setVisibility(android.view.View.GONE);
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public OrderItemViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
