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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.AdminOrderAdapter;
import com.example.appbansach.databinding.FragmentManageOrdersBinding;
import com.example.appbansach.model.NotificationModel;
import com.example.appbansach.model.Order;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersFragment extends Fragment {
    private FragmentManageOrdersBinding binding;
    private AdminOrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageOrdersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadAllOrders();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(orderList, this::showUpdateStatusDialog);
        binding.rvManageOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvManageOrders.setAdapter(adapter);
    }

    private void loadAllOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statuses = {"pending", "shipping", "delivered", "cancelled"};
        String[] statusLabels = {"Chờ duyệt", "Đang giao hàng", "Đã giao hàng", "Hủy đơn hàng"};
        
        int currentSelection = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                currentSelection = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật trạng thái")
                .setSingleChoiceItems(statusLabels, currentSelection, (dialog, which) -> {
                    updateStatusAndNotify(order, statuses[which], statusLabels[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void updateStatusAndNotify(Order order, String newStatus, String label) {
        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã cập nhật: " + label, Toast.LENGTH_SHORT).show();
                    
                    // LIÊN KẾT: Tự động gửi thông báo cho User khi trạng thái thay đổi
                    NotificationModel noti = new NotificationModel(
                            "Cập nhật đơn hàng",
                            "Đơn hàng #" + order.getOrderId().substring(0, 8) + " đã chuyển sang trạng thái: " + label,
                            order.getUserId(),
                            Timestamp.now()
                    );
                    db.collection("notifications").add(noti);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
