package com.example.bookstore.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemAdminOrderBinding;
import com.example.bookstore.data.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onUpdateStatusClick(Order order);
    }

    public AdminOrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
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
        Order order = orders.get(position);
        
        if (order.getOrderId() != null) {
            String id = order.getOrderId();
            holder.binding.tvAdminOrderId.setText("Mã: #" + (id.length() > 8 ? id.substring(0, 8).toUpperCase() : id.toUpperCase()));
        }
        
        Map<String, String> address = order.getShippingAddress();
        if (address != null) {
            // Updated tvAdminOrderUser to tvAdminCustomerInfo to match item_admin_order.xml
            holder.binding.tvAdminCustomerInfo.setText("Khách hàng: " + address.get("name"));
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvAdminOrderTotal.setText(formatter.format(order.getTotalAmount() + order.getShippingFee()));

        holder.binding.tvAdminOrderStatus.setText("Trạng thái: " + getStatusDisplay(order.getStatus()));
        
        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.binding.tvAdminOrderDate.setText(sdf.format(order.getCreatedAt().toDate()));
        }

        holder.binding.btnUpdateStatus.setOnClickListener(v -> listener.onUpdateStatusClick(order));
    }

    private String getStatusDisplay(String status) {
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
        return orders.size();
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        ItemAdminOrderBinding binding;
        public AdminOrderViewHolder(ItemAdminOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
