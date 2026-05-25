package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        return binding.getRoot();
    }

    private void loadOrderDetail() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                binding.progressBar.setVisibility(View.GONE);
                Order order = documentSnapshot.toObject(Order.class);
                if (order != null) {
                    displayOrderDetail(order);
                }
            }
        });
    }

    private void displayOrderDetail(Order order) {
        binding.chipDetailStatus.setText(getStatusText(order.getStatus()));
        
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

        // Hiển thị danh sách sản phẩm bằng OrderItemAdapter
        OrderItemAdapter adapter = new OrderItemAdapter(order.getItems());
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(adapter);
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
