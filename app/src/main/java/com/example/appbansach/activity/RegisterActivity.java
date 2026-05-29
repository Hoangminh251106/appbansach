package com.example.appbansach.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbansach.databinding.ActivityRegisterBinding;
import com.example.appbansach.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String fullName = binding.etFullName.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getUid();
                        saveUserToFirestore(uid, fullName, email);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String fullName, String email) {
        // Tạo đối tượng User mới với role mặc định là "customer"
        User newUser = new User(uid, fullName, email, "", "", "customer", Timestamp.now());
        
        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
