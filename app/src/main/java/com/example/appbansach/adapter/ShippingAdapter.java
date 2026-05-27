package com.example.appbansach.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.R;
import com.example.appbansach.admin.ShippingDetailActivity;
import com.example.appbansach.databinding.ItemShippingBinding;
import com.example.appbansach.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShippingAdapter extends RecyclerView.Adapter<ShippingAdapter.ViewHolder> {
    private final Context context;
    private List<Order> list;
    private List<Order> listFull;

    public ShippingAdapter(Context context, List<Order> list) {
        this.context = context;
        this.list = list;
        this.listFull = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShippingBinding binding = ItemShippingBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order item = list.get(position);
        
        holder.binding.tvOrderId.setText("Mã đơn: #" + item.getOrderId());
        
        Map<String, String> address = item.getShippingAddress();
        if (address != null) {
            holder.binding.tvCustomerName.setText(address.get("name"));
            holder.binding.tvPhone.setText("SĐT: " + address.get("phone"));
            holder.binding.tvAddress.setText("Địa chỉ: " + address.get("address"));
        }

        holder.binding.tvShippingFee.setText(String.format(Locale.getDefault(), "Phí ship: %,dđ", item.getShippingFee()));
        
        if (item.getCreatedAt() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.binding.tvDate.setText(dateFormat.format(item.getCreatedAt().toDate()));
        }

        // Xử lý trạng thái UI
        String status = item.getStatus();
        updateStatusUI(holder, status);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShippingDetailActivity.class);
            intent.putExtra("order", item.getOrderId());
            context.startActivity(intent);
        });
    }

    private void updateStatusUI(ViewHolder holder, String status) {
        String label = "Chờ duyệt";
        int bgRes = R.drawable.bg_status_active;

        if (status != null) {
            switch (status) {
                case "shipping":
                    label = "Đang giao";
                    bgRes = R.drawable.bg_status_active;
                    break;
                case "delivered":
                    label = "Đã giao";
                    bgRes = R.drawable.bg_status_active;
                    break;
                case "cancelled":
                    label = "Đã hủy";
                    bgRes = R.drawable.bg_status_locked;
                    break;
            }
        }
        
        holder.binding.tvStatus.setText(label);
        holder.binding.tvStatus.setBackgroundResource(bgRes);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<Order> newList) {
        this.list = newList;
        this.listFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query, String statusFilter) {
        list = new ArrayList<>();
        String targetStatus = "";
        switch (statusFilter) {
            case "Chờ xác nhận": targetStatus = "pending"; break;
            case "Đang giao": targetStatus = "shipping"; break;
            case "Đã giao": targetStatus = "delivered"; break;
            case "Đã hủy": targetStatus = "cancelled"; break;
        }

        for (Order item : listFull) {
            Map<String, String> addr = item.getShippingAddress();
            boolean matchQuery = query.isEmpty() || 
                    item.getOrderId().toLowerCase().contains(query.toLowerCase()) ||
                    (addr != null && addr.get("name").toLowerCase().contains(query.toLowerCase())) ||
                    (addr != null && addr.get("phone").contains(query));
            
            boolean matchStatus = statusFilter.equals("Tất cả") || (item.getStatus() != null && item.getStatus().equals(targetStatus));

            if (matchQuery && matchStatus) {
                list.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemShippingBinding binding;
        public ViewHolder(ItemShippingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
