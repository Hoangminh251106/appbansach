package com.example.appbansach.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appbansach.databinding.ActivityLoginBinding;
import com.example.appbansach.databinding.DialogTwoFactorBinding;
import com.example.appbansach.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkTwoFactorAuthentication(mAuth.getUid());
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnLogin.setEnabled(true);
                    }
                });
    }

    private void checkTwoFactorAuthentication(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            binding.progressBar.setVisibility(View.GONE);
            User user = documentSnapshot.toObject(User.class);
            
            if (user != null && user.isTwoFactorEnabled()) {
                showTwoFactorDialog(user.getSecurityPin());
            } else {
                navigateToMain();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            navigateToMain(); // Nếu lỗi Firestore vẫn cho vào hoặc xử lý tùy bảo mật
        });
    }

    private void showTwoFactorDialog(String correctPin) {
        DialogTwoFactorBinding dialogBinding = DialogTwoFactorBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .create();

        dialogBinding.btnVerify.setOnClickListener(v -> {
            String inputPin = dialogBinding.etPin.getText().toString().trim();
            if (inputPin.equals(correctPin)) {
                dialog.dismiss();
                navigateToMain();
            } else {
                Toast.makeText(this, "Mã PIN không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.btnCancel.setOnClickListener(v -> {
            mAuth.signOut();
            dialog.dismiss();
            binding.btnLogin.setEnabled(true);
        });

        dialog.show();
    }

    private void navigateToMain() {
        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
