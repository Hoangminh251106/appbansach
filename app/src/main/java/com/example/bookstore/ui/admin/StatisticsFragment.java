package com.example.bookstore.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookstore.data.model.Order;
import com.example.appbansach.databinding.FragmentStatisticsBinding;
import com.example.bookstore.utils.Utils;

import java.util.Map;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private AdminViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        observeStatistics();
        viewModel.loadAllOrders();
    }

    private void observeStatistics() {
        viewModel.allOrders.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        calculateStats(resource.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void calculateStats(java.util.List<Order> orders) {
        long totalRevenue = 0;
        int totalBooksSold = 0;
        int deliveredOrders = 0;

        for (Order order : orders) {
            if ("delivered".equals(order.getStatus())) {
                totalRevenue += order.getTotalAmount();
                deliveredOrders++;
                for (Map<String, Object> item : order.getItems()) {
                    Object quantityObj = item.get("quantity");
                    if (quantityObj instanceof Long) {
                        totalBooksSold += ((Long) quantityObj).intValue();
                    } else if (quantityObj instanceof Integer) {
                        totalBooksSold += (Integer) quantityObj;
                    }
                }
            }
        }

        binding.tvTotalRevenue.setText(Utils.formatCurrency(totalRevenue));
        binding.tvTotalOrdersCount.setText(String.valueOf(deliveredOrders));
        binding.tvTotalBooksSold.setText(String.valueOf(totalBooksSold));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}