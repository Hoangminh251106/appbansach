package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appbansach.databinding.FragmentStatisticsBinding;
import com.example.appbansach.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.Map;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        loadStatistics();

        return binding.getRoot();
    }

    private void loadStatistics() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded()) return;
            binding.progressBar.setVisibility(View.GONE);

            long totalRevenue = 0;
            int totalOrders = queryDocumentSnapshots.size();
            int totalBooksSold = 0;

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                
                // Chỉ tính doanh thu cho các đơn đã giao thành công
                if ("delivered".equals(order.getStatus())) {
                    totalRevenue += order.getTotalAmount();
                }

                // Tính tổng số lượng sách đã bán (tất cả đơn trừ đơn bị hủy)
                if (!"cancelled".equals(order.getStatus())) {
                    if (order.getItems() != null) {
                        for (Map<String, Object> item : order.getItems()) {
                            Object qty = item.get("quantity");
                            if (qty instanceof Long) {
                                totalBooksSold += ((Long) qty).intValue();
                            } else if (qty instanceof Integer) {
                                totalBooksSold += (Integer) qty;
                            }
                        }
                    }
                }
            }

            DecimalFormat formatter = new DecimalFormat("#,###");
            binding.tvTotalRevenue.setText(formatter.format(totalRevenue) + "đ");
            binding.tvTotalOrdersCount.setText(String.valueOf(totalOrders));
            binding.tvTotalBooksSold.setText(String.valueOf(totalBooksSold));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
