package com.example.bookstore.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentCartBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupRecyclerView();
        observeViewModel();

        binding.btnProceedCheckout.setOnClickListener(v -> {
            // Navigate to Checkout
            Navigation.findNavController(v).navigate(R.id.checkoutFragment);
        });
    }

    private void setupRecyclerView() {
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observeViewModel() {
        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvCart.setVisibility(View.GONE);
                binding.layoutCheckout.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvCart.setVisibility(View.VISIBLE);
                binding.layoutCheckout.setVisibility(View.VISIBLE);
                
                adapter = new CartAdapter(items, new CartAdapter.OnCartItemChangeListener() {
                    @Override
                    public void onQuantityChange(String bookId, int newQuantity) {
                        viewModel.updateQuantity(bookId, newQuantity);
                    }

                    @Override
                    public void onRemoveItem(String bookId) {
                        viewModel.removeItem(bookId);
                    }
                });
                binding.rvCart.setAdapter(adapter);
            }
        });

        viewModel.getTotalPrice().observe(getViewLifecycleOwner(), total -> {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvTotalCart.setText(formatter.format(total));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}