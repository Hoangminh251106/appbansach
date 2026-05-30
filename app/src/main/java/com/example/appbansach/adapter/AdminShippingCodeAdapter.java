package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbansach.databinding.ItemAdminShippingCodeBinding;
import com.example.appbansach.model.ShippingCode;
import java.text.DecimalFormat;
import java.util.List;

public class AdminShippingCodeAdapter extends RecyclerView.Adapter<AdminShippingCodeAdapter.ViewHolder> {
    private List<ShippingCode> list;
    private OnShippingCodeListener listener;

    public interface OnShippingCodeListener {
        void onDelete(ShippingCode code);
    }

    public AdminShippingCodeAdapter(List<ShippingCode> list, OnShippingCodeListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAdminShippingCodeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingCode item = list.get(position);
        holder.binding.tvShipCode.setText(item.getCode());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.binding.tvShipDiscount.setText("Giảm: " + formatter.format(item.getDiscountAmount()) + "đ");
        holder.binding.tvShipMinAmount.setText("Đơn tối thiểu: " + formatter.format(item.getMinOrderAmount()) + "đ");

        holder.binding.btnDeleteShipCode.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminShippingCodeBinding binding;
        public ViewHolder(ItemAdminShippingCodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
