package com.example.appbansach.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityPaymentQrBinding;
import java.text.DecimalFormat;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PaymentQRActivity extends AppCompatActivity {
    private ActivityPaymentQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long amount = getIntent().getLongExtra("amount", 0);
        String orderId = getIntent().getStringExtra("orderId");
        String paymentContent = "Thanh toan don hang " + (orderId != null ? orderId : "");

        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvPaymentAmount.setText(formatter.format(amount) + "đ");
        binding.tvPaymentContent.setText(paymentContent);

        // Tạo mã QR thật thông qua API QRServer
        try {
            String qrData = "Chuyen khoan: " + amount + " VND. Noi dung: " + paymentContent;
            String encodedData = URLEncoder.encode(qrData, "UTF-8");
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + encodedData;
            
            Glide.with(this)
                    .load(qrUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.ivQRCode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnConfirmPaid.setOnClickListener(v -> {
            Toast.makeText(this, "Yêu cầu xác nhận đã được gửi. Shop sẽ kiểm tra và duyệt đơn của bạn!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
