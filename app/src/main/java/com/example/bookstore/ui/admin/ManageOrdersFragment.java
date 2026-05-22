package com.example.bookstore.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookstore.data.model.Order;
import com.example.appbansach.databinding.FragmentManageOrdersBinding;
import com.example.appbansach.utils.Resource;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageOrdersFragment extends Fragment {
    private FragmentManageOrdersBinding binding;
    private AdminViewModel viewModel;
    private AdminOrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        setupRecyclerView();
        setupTabs();
        observeViewModel();

        viewModel.loadAllOrders();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(new ArrayList<>(), this::showStatusUpdateDialog);
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterOrders(int position) {
        String status;
        switch (position) {
            case 1: status = "pending"; break;
            case 2: status = "shipping"; break;
            case 3: status = "delivered"; break;
            case 4: status = "cancelled"; break;
            default:
                adapter = new AdminOrderAdapter(allOrders, this::showStatusUpdateDialog);
                binding.rvAdminOrders.setAdapter(adapter);
                return;
        }

        List<Order> filteredList = allOrders.stream()
                .filter(o -> status.equals(o.getStatus()))
                .collect(Collectors.toList());
        adapter = new AdminOrderAdapter(filteredList, this::showStatusUpdateDialog);
        binding.rvAdminOrders.setAdapter(adapter);
    }

    private void showStatusUpdateDialog(Order order) {
        String[] statuses = {"pending", "shipping", "delivered", "cancelled"};
        String[] displayStatuses = {"Chờ duyệt", "Đang giao", "Đã giao", "Đã hủy"};
        
        int currentSelection = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                currentSelection = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật trạng thái")
                .setSingleChoiceItems(displayStatuses, currentSelection, (dialog, which) -> {
                    viewModel.updateOrderStatus(order.getOrderId(), statuses[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void observeViewModel() {
        viewModel.allOrders.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        allOrders = resource.data;
                        filterOrders(binding.tabLayoutStatus.getSelectedTabPosition());
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.operationStatus.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}