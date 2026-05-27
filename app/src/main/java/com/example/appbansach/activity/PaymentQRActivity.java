package com.example.appbansach.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbansach.databinding.ActivityPaymentQrBinding;
import java.text.DecimalFormat;

public class PaymentQRActivity extends AppCompatActivity {
    private ActivityPaymentQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long amount = getIntent().getLongExtra("amount", 0);
        String orderId = getIntent().getStringExtra("orderId");

        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvPaymentAmount.setText(formatter.format(amount) + "đ");
        binding.tvPaymentContent.setText("Thanh toan don hang " + orderId);

        // Gia lap ma QR (Trong thuc te se dung thu vien nhu ZXing hoac API ngan hang)
        // O day ta giu nguyen anh mac dinh hoac tai tu mot URL mau
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnConfirmPaid.setOnClickListener(v -> {
            Toast.makeText(this, "Yêu cầu xác nhận đã được gửi. Shop sẽ kiểm tra và duyệt đơn của bạn!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
