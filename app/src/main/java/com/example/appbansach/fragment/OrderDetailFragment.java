package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.OrderItemAdapter;
import com.example.appbansach.databinding.FragmentOrderDetailBinding;
import com.example.appbansach.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Map;

public class OrderDetailFragment extends Fragment {
    private FragmentOrderDetailBinding binding;
    private FirebaseFirestore db;
    private String orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        if (orderId != null) {
            loadOrderDetail();
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnCancelOrder.setOnClickListener(v -> showCancelConfirmation());

        return binding.getRoot();
    }

    private void loadOrderDetail() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId).addSnapshotListener((documentSnapshot, e) -> {
            if (!isAdded() || binding == null) return;
            binding.progressBar.setVisibility(View.GONE);
            
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Order order = documentSnapshot.toObject(Order.class);
                if (order != null) {
                    order.setOrderId(documentSnapshot.getId());
                    displayOrderDetail(order);
                }
            }
        });
    }

    private void displayOrderDetail(Order order) {
        binding.chipDetailStatus.setText(getStatusText(order.getStatus()));
        
        // Cập nhật hiển thị nút Hủy đơn
        if ("pending".equals(order.getStatus())) {
            binding.btnCancelOrder.setVisibility(View.VISIBLE);
            binding.tvCancelNote.setVisibility(View.VISIBLE);
        } else {
            binding.btnCancelOrder.setVisibility(View.GONE);
            binding.tvCancelNote.setVisibility(View.GONE);
        }

        Map<String, String> addr = order.getShippingAddress();
        if (addr != null) {
            binding.tvDetailName.setText(addr.get("name"));
            binding.tvDetailPhone.setText(addr.get("phone"));
            binding.tvDetailAddress.setText(addr.get("address"));
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvSubtotal.setText(formatter.format(order.getTotalAmount()) + "đ");
        binding.tvShipping.setText(formatter.format(order.getShippingFee()) + "đ");
        binding.tvGrandTotal.setText(formatter.format(order.getTotalAmount() + order.getShippingFee()) + "đ");

        OrderItemAdapter adapter = new OrderItemAdapter(order.getItems());
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(adapter);
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void cancelOrder() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
