package com.example.bookstore.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookstore.data.repository.OrderRepository;
import com.example.bookstore.data.repository.UserRepository;
import com.example.appbansach.databinding.FragmentOrderHistoryBinding;
import com.example.appbansach.utils.Resource;

import java.util.ArrayList;

public class OrderHistoryFragment extends Fragment {
    private FragmentOrderHistoryBinding binding;
    private OrderAdapter adapter;
    private OrderRepository orderRepository;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderRepository = new OrderRepository();
        userRepository = new UserRepository();

        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(new ArrayList<>());
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderHistory.setAdapter(adapter);
    }

    private void loadOrders() {
        String userId = userRepository.getCurrentUid();
        if (userId == null) return;

        orderRepository.getOrderHistory(userId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvNoOrders.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        adapter = new OrderAdapter(resource.data);
                        binding.rvOrderHistory.setAdapter(adapter);
                        binding.tvNoOrders.setVisibility(View.GONE);
                    } else {
                        binding.tvNoOrders.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}