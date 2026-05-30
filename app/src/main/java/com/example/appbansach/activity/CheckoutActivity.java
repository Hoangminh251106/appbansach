package com.example.appbansach.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.databinding.ActivityCheckoutBinding;
import com.example.appbansach.helper.CartManager;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Order;
import com.example.appbansach.model.Voucher;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

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
    private long shippingDiscount = 0;

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
                if (checkoutItems.isEmpty()) { finish(); return; }
                calculateTotal();
            }
        });

        binding.btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        binding.btnApplyShippingCode.setOnClickListener(v -> applyShippingCode());
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrderWithTransaction());
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void calculateTotal() {
        subtotal = 0;
        for (CartItemEntity item : checkoutItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        updateUI();
    }

    private void updateUI() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        binding.tvSubtotal.setText(formatter.format(subtotal) + "đ");
        binding.tvDiscount.setText("-" + formatter.format(discount) + "đ");
        binding.tvShippingFee.setText(formatter.format(shippingFee) + "đ");
        binding.tvShippingDiscount.setText("-" + formatter.format(shippingDiscount) + "đ");
        long finalTotal = subtotal + Math.max(0, shippingFee - shippingDiscount) - discount;
        binding.tvCheckoutTotal.setText(formatter.format(finalTotal) + "đ");
    }

    private void applyVoucher() {
        String code = binding.etVoucherCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) return;

        db.collection("vouchers").document(code).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Voucher v = doc.toObject(Voucher.class);
                if (v != null) {
                    if (subtotal >= v.getMinOrderAmount()) {
                        discount = v.getDiscountAmount();
                        updateUI();
                        Toast.makeText(this, "Đã áp dụng mã giảm giá!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Đơn hàng chưa đủ tối thiểu " + v.getMinOrderAmount() + "đ", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Mã giảm giá không tồn tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyShippingCode() {
        String code = binding.etShippingCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) return;

        db.collection("shipping_codes").document(code).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Voucher v = doc.toObject(Voucher.class); // Dùng chung model Voucher vì cấu trúc giống nhau
                if (v != null) {
                    if (subtotal >= v.getMinOrderAmount()) {
                        shippingDiscount = v.getDiscountAmount();
                        updateUI();
                        Toast.makeText(this, "Đã áp dụng mã vận chuyển!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Đơn hàng chưa đủ tối thiểu " + v.getMinOrderAmount() + "đ", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Mã vận chuyển không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeOrderWithTransaction() {
        String name = binding.etCheckoutName.getText().toString().trim();
        String phone = binding.etCheckoutPhone.getText().toString().trim();
        String address = binding.etCheckoutAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        final long finalTotal = subtotal + Math.max(0, shippingFee - shippingDiscount) - discount;

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            for (CartItemEntity item : checkoutItems) {
                DocumentReference bookRef = db.collection("books").document(item.getBookId());
                Book book = transaction.get(bookRef).toObject(Book.class);
                if (book == null || book.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Sách '" + item.getTitle() + "' không đủ hàng!");
                }
            }

            for (CartItemEntity item : checkoutItems) {
                DocumentReference bookRef = db.collection("books").document(item.getBookId());
                transaction.update(bookRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
                transaction.update(bookRef, "soldCount", com.google.firebase.firestore.FieldValue.increment(item.getQuantity()));
            }

            DocumentReference orderRef = db.collection("orders").document();
            Order order = new Order();
            order.setOrderId(orderRef.getId());
            order.setUserId(mAuth.getUid());
            order.setStatus("pending");
            order.setCreatedAt(Timestamp.now());
            order.setTotalAmount(finalTotal);
            order.setPaymentMethod(binding.rbTransfer.isChecked() ? "Transfer" : "COD");

            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (CartItemEntity item : checkoutItems) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookId", item.getBookId());
                map.put("title", item.getTitle());
                map.put("quantity", item.getQuantity());
                map.put("price", item.getPrice());
                map.put("imageUrl", item.getImageUrl());
                itemsList.add(map);
            }
            order.setItems(itemsList);

            Map<String, String> addr = new HashMap<>();
            addr.put("name", name);
            addr.put("phone", phone);
            addr.put("address", address);
            order.setShippingAddress(addr);

            transaction.set(orderRef, order);
            return null;
        }).addOnSuccessListener(aVoid -> {
            CartManager.getInstance(this).deleteSelectedItems();
            sendNotification(finalTotal);
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnPlaceOrder.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void sendNotification(long amount) {
        Map<String, Object> noti = new HashMap<>();
        noti.put("userId", mAuth.getUid());
        noti.put("title", "Đặt hàng thành công");
        noti.put("content", "Đơn hàng trị giá " + amount + "đ đã được ghi nhận và đang chờ xử lý.");
        noti.put("sentAt", Timestamp.now());
        noti.put("isRead", false);
        db.collection("notifications").add(noti);
    }
}
