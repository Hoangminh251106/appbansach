package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemAdminVoucherBinding;
import com.example.appbansach.model.Voucher;

import java.text.DecimalFormat;
import java.util.List;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.AdminVoucherViewHolder> {
    private List<Voucher> voucherList;
    private OnVoucherActionListener listener;

    public interface OnVoucherActionListener {
        void onDelete(Voucher voucher);
    }

    public AdminVoucherAdapter(List<Voucher> voucherList, OnVoucherActionListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminVoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminVoucherBinding binding = ItemAdminVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminVoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminVoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.binding.tvVoucherCode.setText(voucher.getCode());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        String detail = "Giảm " + formatter.format(voucher.getDiscountAmount()) + "đ cho đơn từ " + formatter.format(voucher.getMinOrderAmount()) + "đ";
        holder.binding.tvVoucherDetail.setText(detail);
        
        holder.binding.btnDeleteVoucher.setOnClickListener(v -> listener.onDelete(voucher));
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class AdminVoucherViewHolder extends RecyclerView.ViewHolder {
        ItemAdminVoucherBinding binding;
        public AdminVoucherViewHolder(ItemAdminVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
