package com.example.appbansach.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbansach.R;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.databinding.ActivityCheckoutBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Order;
import com.example.appbansach.model.Voucher;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<CartItemEntity> checkoutItems = new ArrayList<>();
    private long subtotal = 0;
    private long shippingFee = 30000;
    private long discount = 0;
    private String selectedVoucherCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        CartManager.getInstance(this).getCartItems().observe(this, items -> {
            if (items != null) {
                this.checkoutItems.clear();
                for (CartItemEntity item : items) {
                    if (item.isSelected()) {
                        this.checkoutItems.add(item);
                    }
                }
                
                if (checkoutItems.isEmpty()) {
                    Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                calculateTotal();
            }
        });

        binding.btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void calculateTotal() {
        subtotal = 0;
        for (CartItemEntity item : checkoutItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        updateUI();
    }

    private void applyVoucher() {
        String code = binding.etVoucherCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("vouchers").document(code).get().addOnSuccessListener(documentSnapshot -> {
            binding.progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                Voucher voucher = documentSnapshot.toObject(Voucher.class);
                if (voucher != null) {
                    if (subtotal >= voucher.getMinOrderAmount()) {
                        discount = voucher.getDiscountAmount();
                        selectedVoucherCode = code;
                        updateUI();
                        Toast.makeText(this, "Đã áp dụng mã giảm giá!", Toast.LENGTH_SHORT).show();
                    } else {
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        Toast.makeText(this, "Đơn hàng tối thiểu " + formatter.format(voucher.getMinOrderAmount()) + "đ", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Mã không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi kiểm tra mã", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvSubtotal.setText(formatter.format(subtotal) + "đ");
        binding.tvDiscount.setText("-" + formatter.format(discount) + "đ");
        long grandTotal = subtotal + shippingFee - discount;
        binding.tvCheckoutTotal.setText(formatter.format(grandTotal) + "đ");
    }

    private void placeOrder() {
        String name = binding.etCheckoutName.getText().toString().trim();
        String phone = binding.etCheckoutPhone.getText().toString().trim();
        String address = binding.etCheckoutAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        WriteBatch batch = db.batch();
        DocumentReference orderRef = db.collection("orders").document();
        String orderId = orderRef.getId();
        long finalTotal = subtotal + shippingFee - discount;
        boolean isTransfer = binding.rbTransfer.isChecked();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(mAuth.getUid());
        order.setStatus("pending");
        order.setCreatedAt(Timestamp.now());
        order.setShippingFee(shippingFee);
        order.setTotalAmount(finalTotal);
        order.setPaymentMethod(isTransfer ? "Transfer" : "COD");

        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItemEntity item : checkoutItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("bookId", item.getBookId());
            itemMap.put("title", item.getTitle());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getImageUrl());
            items.add(itemMap);
            
            // CẬP NHẬT: Trừ số lượng tồn kho của sách
            DocumentReference bookRef = db.collection("books").document(item.getBookId());
            batch.update(bookRef, "stock", FieldValue.increment(-item.getQuantity()));
        }
        order.setItems(items);

        Map<String, String> addressMap = new HashMap<>();
        addressMap.put("name", name);
        addressMap.put("phone", phone);
        addressMap.put("address", address);
        order.setShippingAddress(addressMap);

        batch.set(orderRef, order);
        
        // Nếu có dùng voucher, có thể đánh dấu voucher đã sử dụng (tùy logic dự án)

        batch.commit().addOnSuccessListener(aVoid -> {
            CartManager.getInstance(this).deleteSelectedItems();
            
            if (isTransfer) {
                Intent intent = new Intent(this, PaymentQRActivity.class);
                intent.putExtra("amount", finalTotal);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnPlaceOrder.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
