package com.example.bookstore.ui.checkout;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentCheckoutBinding;
import com.example.appbansach.utils.Resource;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutFragment extends Fragment {
    private FragmentCheckoutBinding binding;
    private CheckoutViewModel viewModel;
    private long subtotal = 0;
    private final long shippingFee = 30000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        observeCart();

        binding.btnPlaceOrder.setOnClickListener(v -> {
            String name = binding.etCheckoutName.getText().toString().trim();
            String phone = binding.etCheckoutPhone.getText().toString().trim();
            String address = binding.etCheckoutAddress.getText().toString().trim();
            String paymentMethod = binding.rbCOD.isChecked() ? "cod" : "momo";

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.placeOrder(name, phone, address, paymentMethod).observe(getViewLifecycleOwner(), resource -> {
                if (resource == null) return;
                switch (resource.status) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnPlaceOrder.setEnabled(false);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        viewModel.clearCart();
                        Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnPlaceOrder.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });
    }

    private void observeCart() {
        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                subtotal = 0;
                for (com.example.bookstore.data.local.CartItemEntity item : items) {
                    subtotal += item.getPrice() * item.getQuantity();
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvSubtotal.setText(formatter.format(subtotal));
        binding.tvShippingFee.setText(formatter.format(shippingFee));
        binding.tvTotalCheckout.setText(formatter.format(subtotal + shippingFee));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
