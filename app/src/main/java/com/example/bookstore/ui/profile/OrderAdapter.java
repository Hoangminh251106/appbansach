package com.example.bookstore.ui.profile;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstore.data.model.Order;
import com.example.appbansach.databinding.ItemOrderBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private final List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.binding.tvOrderId.setText("Mã đơn: #" + order.getOrderId().substring(0, 8).toUpperCase());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.binding.tvOrderDate.setText("Ngày đặt: " + (order.getCreatedAt() != null ? sdf.format(order.getCreatedAt().toDate()) : "---"));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalAmount() + order.getShippingFee()));

        StringBuilder itemsSummary = new StringBuilder();
        for (Map<String, Object> item : order.getItems()) {
            itemsSummary.append(item.get("title")).append(" (x").append(item.get("quantity")).append("), ");
        }
        if (itemsSummary.length() > 2) itemsSummary.setLength(itemsSummary.length() - 2);
        holder.binding.tvOrderItems.setText(itemsSummary.toString());

        setStatusChip(holder, order.getStatus());
    }

    private void setStatusChip(OrderViewHolder holder, String status) {
        holder.binding.chipStatus.setText(getStatusText(status));
        int color = getStatusColor(status);
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
        holder.binding.chipStatus.setTextColor(Color.WHITE);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Chờ duyệt";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "pending": return Color.parseColor("#FFC107"); // Yellow
            case "shipping": return Color.parseColor("#2196F3"); // Blue
            case "delivered": return Color.parseColor("#4CAF50"); // Green
            case "cancelled": return Color.parseColor("#F44336"); // Red
            default: return Color.GRAY;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;
        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}