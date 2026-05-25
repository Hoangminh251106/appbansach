package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemAdminOrderBinding;
import com.example.appbansach.model.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onUpdateStatus(Order order);
    }

    public AdminOrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminOrderBinding binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        String orderId = order.getOrderId();
        if (orderId != null && orderId.length() >= 8) {
            orderId = orderId.substring(0, 8).toUpperCase();
        } else if (orderId == null) {
            orderId = "N/A";
        }
        holder.binding.tvAdminOrderId.setText("Mã đơn: #" + orderId);

        // Fixed: changed tvAdminOrderCustomer to tvAdminCustomerInfo to match layout
        String customerName = "N/A";
        if (order.getShippingAddress() != null && order.getShippingAddress().containsKey("name")) {
            customerName = order.getShippingAddress().get("name");
        }
        holder.binding.tvAdminCustomerInfo.setText("Khách hàng: " + customerName);
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvAdminOrderTotal.setText("Tổng: " + formatter.format(order.getTotalAmount() + order.getShippingFee()) + "đ");
        
        // Fixed: changed chipAdminOrderStatus to tvAdminOrderStatus to match layout
        holder.binding.tvAdminOrderStatus.setText("Trạng thái: " + getStatusText(order.getStatus()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (order.getCreatedAt() != null) {
            holder.binding.tvAdminOrderDate.setText(sdf.format(order.getCreatedAt().toDate()));
        }

        holder.binding.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateStatus(order);
            }
        });
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
        return orderList != null ? orderList.size() : 0;
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        ItemAdminOrderBinding binding;
        public AdminOrderViewHolder(ItemAdminOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
