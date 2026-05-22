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
import com.example.appbansach.databinding.FragmentCartBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.CartItem;

import java.text.DecimalFormat;
import java.util.List;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private List<CartItem> cartItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        
        setupCartList();
        updateUI();

        binding.btnProceedCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getActivity(), CheckoutActivity.class));
            }
        });

        return binding.getRoot();
    }

    private void setupCartList() {
        cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                cartItems.get(position).setQuantity(newQuantity);
                adapter.notifyItemChanged(position);
                updateUI();
            }

            @Override
            public void onRemoveItem(int position) {
                CartManager.getInstance().removeProduct(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, cartItems.size());
                updateUI();
            }
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.layoutCheckout.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.layoutCheckout.setVisibility(View.VISIBLE);
            
            DecimalFormat formatter = new DecimalFormat("#,###");
            binding.tvTotalCart.setText(formatter.format(CartManager.getInstance().getTotalAmount()) + "đ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
