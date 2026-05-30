package com.example.appbansach.fragment;

import android.content.Intent;
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

import com.example.appbansach.activity.CheckoutActivity;
import com.example.appbansach.adapter.CartAdapter;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.databinding.FragmentCartBinding;
import com.example.appbansach.ui.viewmodel.CartViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private CartViewModel viewModel;
    private List<CartItemEntity> currentItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);
        
        setupCartList();
        observeCart();
        setupListeners();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.cbSelectAll.setOnClickListener(v -> {
            boolean isChecked = binding.cbSelectAll.isChecked();
            viewModel.updateAllSelection(isChecked);
        });

        binding.btnDeleteSelected.setOnClickListener(v -> {
            viewModel.deleteSelectedItems();
            Toast.makeText(getContext(), "Đã xóa các mục đã chọn", Toast.LENGTH_SHORT).show();
        });

        binding.btnProceedCheckout.setOnClickListener(v -> {
            boolean hasSelected = false;
            for (CartItemEntity item : currentItems) {
                if (item.isSelected()) {
                    hasSelected = true;
                    break;
                }
            }

            if (!hasSelected) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getActivity(), CheckoutActivity.class));
            }
        });
    }

    private void setupCartList() {
        adapter = new CartAdapter(new ArrayList<>(), new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChange(CartItemEntity item, int newQuantity) {
                viewModel.updateQuantity(item.getBookId(), newQuantity);
            }

            @Override
            public void onRemoveItem(CartItemEntity item) {
                viewModel.removeFromCart(item.getBookId());
            }

            @Override
            public void onSelectionChange(CartItemEntity item, boolean isSelected) {
                viewModel.updateSelection(item.getBookId(), isSelected);
            }
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
    }

    private void observeCart() {
        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                currentItems = items;
                adapter.setData(items);
                updateUI(items);
                updateSelectAllCheckbox();
            }
        });
    }

    private void updateSelectAllCheckbox() {
        if (currentItems == null || currentItems.isEmpty()) {
            binding.cbSelectAll.setChecked(false);
            binding.btnDeleteSelected.setVisibility(View.GONE);
            return;
        }

        boolean allSelected = true;
        boolean anySelected = false;
        for (CartItemEntity item : currentItems) {
            if (!item.isSelected()) {
                allSelected = false;
            } else {
                anySelected = true;
            }
        }
        binding.cbSelectAll.setChecked(allSelected);
        binding.btnDeleteSelected.setVisibility(anySelected ? View.VISIBLE : View.GONE);
    }

    private void updateUI(List<CartItemEntity> items) {
        if (items == null || items.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.layoutCheckout.setVisibility(View.GONE);
            binding.layoutHeader.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.layoutCheckout.setVisibility(View.VISIBLE);
            binding.layoutHeader.setVisibility(View.VISIBLE);
            
            long total = 0;
            for (CartItemEntity item : items) {
                if (item.isSelected()) {
                    total += item.getPrice() * item.getQuantity();
                }
            }
            DecimalFormat formatter = new DecimalFormat("#,###");
            binding.tvTotalCart.setText(formatter.format(total) + "đ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
