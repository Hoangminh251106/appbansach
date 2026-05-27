package com.example.appbansach.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityShippingDetailBinding;
import com.example.appbansach.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShippingDetailActivity extends AppCompatActivity {
    private ActivityShippingDetailBinding binding;
    private FirebaseFirestore db;
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShippingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order");

        if (orderId == null) {
            finish();
            return;
        }

        setupToolbar();
        loadOrderDetail();
        setupActions();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết vận chuyển");
        }
    }

    private void loadOrderDetail() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentOrder = documentSnapshot.toObject(Order.class);
                if (currentOrder != null) {
                    currentOrder.setOrderId(documentSnapshot.getId());
                    displayOrder(currentOrder);
                }
            } else {
                Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayOrder(Order order) {
        Map<String, String> addr = order.getShippingAddress();
        if (addr != null) {
            binding.tvDetailCustomer.setText(addr.get("name"));
            binding.tvDetailPhone.setText("SĐT: " + addr.get("phone"));
            binding.tvDetailAddress.setText("Địa chỉ: " + addr.get("address"));
        }
        
        binding.tvDetailNote.setText("Thanh toán: " + order.getPaymentMethod());
        binding.tvDetailFee.setText(String.format(Locale.getDefault(), "Phí ship: %,dđ", order.getShippingFee()));
        binding.tvDetailTotal.setText(String.format(Locale.getDefault(), "Tổng tiền: %,dđ", order.getTotalAmount()));
        
        // Map nhãn trạng thái
        String statusLabel = getStatusLabel(order.getStatus());
        binding.tvDetailStatus.setText("Trạng thái: " + statusLabel);
        
        StringBuilder sb = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                sb.append("• ").append(item.get("title"))
                  .append(" (x").append(item.get("quantity")).append(")\n");
            }
        }
        binding.tvDetailItems.setText(sb.toString().isEmpty() ? "Trống" : sb.toString());
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Chờ duyệt";
        switch (status) {
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return "Chờ duyệt";
        }
    }

    private void setupActions() {
        binding.btnUpdateStatus.setOnClickListener(v -> showStatusDialog());
        binding.btnUpdateFee.setOnClickListener(v -> showFeeDialog());
    }

    private void showStatusDialog() {
        String[] statuses = {"pending", "shipping", "delivered", "cancelled"};
        String[] labels = {"Chờ xác nhận", "Đang giao hàng", "Giao thành công", "Hủy đơn"};
        
        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái")
                .setItems(labels, (dialog, which) -> updateStatus(statuses[which]))
                .show();
    }

    private void showFeeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_fee, null);
        EditText etFee = view.findViewById(R.id.etFee);
        etFee.setText(String.valueOf(currentOrder.getShippingFee()));

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật phí ship")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String feeStr = etFee.getText().toString();
                    if (!feeStr.isEmpty()) {
                        updateFee(Long.parseLong(feeStr));
                    }
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void updateStatus(String status) {
        db.collection("orders").document(orderId).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    loadOrderDetail();
                });
    }

    private void updateFee(long fee) {
        db.collection("orders").document(orderId).update("shippingFee", fee)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật phí ship", Toast.LENGTH_SHORT).show();
                    loadOrderDetail();
                });
    }
}
