package com.example.appbansach.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbansach.databinding.ActivityCheckoutBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.CartItem;
import java.text.DecimalFormat;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long total = 0;
        for (CartItem item : CartManager.getInstance().getCartItems()) {
            total += item.getBook().getPrice() * item.getQuantity();
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        // Đổi tvTotalAmount thành tvCheckoutTotal để khớp với activity_checkout.xml
        binding.tvCheckoutTotal.setText(formatter.format(total) + "đ");

        // Đổi btnConfirmCheckout thành btnPlaceOrder để khớp với activity_checkout.xml
        binding.btnPlaceOrder.setOnClickListener(v -> {
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            CartManager.getInstance().clearCart();
            finish();
        });
    }
}
