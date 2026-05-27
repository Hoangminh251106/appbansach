package com.example.appbansach.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityUserDetailBinding;
import com.example.appbansach.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class UserDetailActivity extends AppCompatActivity {

    private ActivityUserDetailBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            finish();
            return;
        }

        setupToolbar();
        loadUserDetails();
        setupButtons();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            binding.progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    displayUser(currentUser);
                }
            } else {
                Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void displayUser(User user) {
        binding.tvDetailName.setText(user.getFullName());
        binding.tvDetailEmail.setText("Email: " + user.getEmail());
        binding.tvDetailPhone.setText("SĐT: " + (user.getPhone() != null ? user.getPhone() : "Chưa cập nhật"));
        binding.tvDetailAddress.setText("Địa chỉ: " + (user.getAddress() != null ? user.getAddress() : "Chưa cập nhật"));

        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.tvDetailJoinedDate.setText("Ngày tham gia: " + sdf.format(user.getCreatedAt().toDate()));
        } else {
            binding.tvDetailJoinedDate.setText("Ngày tham gia: N/A");
        }

        String status = user.getStatus() != null ? user.getStatus() : "active";
        if (status.equals("active")) {
            binding.tvDetailStatus.setText("Hoạt động");
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_status_active);
            binding.btnLockUnlock.setText("KHÓA TÀI KHOẢN");
            binding.btnLockUnlock.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
        } else {
            binding.tvDetailStatus.setText("Bị khóa");
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_status_locked);
            binding.btnLockUnlock.setText("MỞ KHÓA TÀI KHOẢN");
            binding.btnLockUnlock.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_myplaces).into(binding.ivDetailAvatar);
        } else {
            binding.ivDetailAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void setupButtons() {
        binding.btnLockUnlock.setOnClickListener(v -> toggleUserStatus());
        binding.btnDeleteUser.setOnClickListener(v -> confirmDeleteUser());
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;

        String currentStatus = currentUser.getStatus() != null ? currentUser.getStatus() : "active";
        String newStatus = currentStatus.equals("active") ? "locked" : "active";

        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    currentUser.setStatus(newStatus);
                    displayUser(currentUser);
                    Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeleteUser() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
