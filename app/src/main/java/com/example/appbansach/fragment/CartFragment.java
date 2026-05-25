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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.activity.CheckoutActivity;
import com.example.appbansach.adapter.CartAdapter;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.databinding.FragmentCartBinding;
import com.example.appbansach.helper.CartManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private List<CartItemEntity> currentItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        
        setupCartList();
        observeCart();

        binding.btnProceedCheckout.setOnClickListener(v -> {
            if (currentItems == null || currentItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getActivity(), CheckoutActivity.class));
            }
        });

        return binding.getRoot();
    }

    private void setupCartList() {
        adapter = new CartAdapter(new ArrayList<>(), new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChange(CartItemEntity item, int newQuantity) {
                CartManager.getInstance(requireContext()).updateQuantity(item.getBookId(), newQuantity);
            }

            @Override
            public void onRemoveItem(CartItemEntity item) {
                CartManager.getInstance(requireContext()).removeFromCart(item.getBookId());
            }
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
    }

    private void observeCart() {
        CartManager.getInstance(requireContext()).getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                currentItems = items;
                adapter.setData(items);
                updateUI(items);
            }
        });
    }

    private void updateUI(List<CartItemEntity> items) {
        if (items == null || items.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.layoutCheckout.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.layoutCheckout.setVisibility(View.VISIBLE);
            
            long total = 0;
            for (CartItemEntity item : items) {
                total += item.getPrice() * item.getQuantity();
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
