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

import com.example.appbansach.R;
import com.example.appbansach.adapter.OrderAdapter;
import com.example.appbansach.databinding.FragmentOrdersBinding;
import com.example.appbansach.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdersFragment extends Fragment {
    private FragmentOrdersBinding binding;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration ordersListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        startListeningOrders();

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(orderList, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                Bundle bundle = new Bundle();
                bundle.putString("orderId", order.getOrderId());
                Navigation.findNavController(requireView()).navigate(R.id.action_ordersFragment_to_orderDetailFragment, bundle);
            }

            @Override
            public void onDeleteClick(Order order) {
                showDeleteConfirmDialog(order);
            }
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(Order order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch sử đơn hàng")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này khỏi lịch sử không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteOrder(order);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteOrder(Order order) {
        db.collection("orders").document(order.getOrderId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startListeningOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        ordersListener = db.collection("orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !isAdded()) return;

                    orderList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orderList.add(order);
                    }

                    // Sắp xếp theo thời gian mới nhất
                    Collections.sort(orderList, (o1, o2) -> {
                        if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    });

                    if (orderList.isEmpty()) {
                        binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                        binding.rvOrders.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyOrders.setVisibility(View.GONE);
                        binding.rvOrders.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null) ordersListener.remove();
        binding = null;
    }
}
