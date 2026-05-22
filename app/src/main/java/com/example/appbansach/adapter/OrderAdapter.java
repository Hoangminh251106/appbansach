package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemOrderBinding;
import com.example.appbansach.model.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;

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
        
        String orderId = order.getOrderId();
        if (orderId != null) {
            holder.binding.tvOrderId.setText("Mã đơn: #" + (orderId.length() > 8 ? orderId.substring(0, 8).toUpperCase() : orderId.toUpperCase()));
        }
        
        holder.binding.chipStatus.setText(getStatusText(order.getStatus()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (order.getCreatedAt() != null) {
            holder.binding.tvOrderDate.setText("Ngày đặt: " + sdf.format(order.getCreatedAt().toDate()));
        }

        StringBuilder itemsSummary = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                itemsSummary.append(item.get("title")).append(" (x").append(item.get("quantity")).append("), ");
            }
        }
        if (itemsSummary.length() > 2) {
            itemsSummary.setLength(itemsSummary.length() - 2);
        }
        holder.binding.tvOrderItems.setText(itemsSummary.toString());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalAmount() + order.getShippingFee()) + "đ");
    }

    private String getStatusText(String status) {
        if (status == null) return "N/A";
        switch (status) {
            case "pending": return "Chờ duyệt";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
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
