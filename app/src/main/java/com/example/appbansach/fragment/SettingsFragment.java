package com.example.appbansach.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.appbansach.R;
import com.example.appbansach.databinding.DialogChangePasswordBinding;
import com.example.appbansach.databinding.DialogSetupPinBinding;
import com.example.appbansach.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        setupToolbar();
        loadSettings();
        setupClickListeners();
        
        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadSettings() {
        binding.switchNotification.setChecked(sharedPreferences.getBoolean("notifications_enabled", true));
        binding.switchDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));
        binding.switchMarquee.setChecked(sharedPreferences.getBoolean("marquee_enabled", true));
        
        update2FAStatusUI();
    }

    private void update2FAStatusUI() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded() && binding != null) {
                    boolean is2FA = doc.getBoolean("twoFactorEnabled") != null && doc.getBoolean("twoFactorEnabled");
                    binding.btnTwoFactor.setText("Xác thực 2 lớp: " + (is2FA ? "Đang Bật" : "Đang Tắt"));
                }
            });
        }
    }

    private void setupClickListeners() {
        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
        });

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnTwoFactor.setOnClickListener(v -> toggleTwoFactor());

        binding.btnClearCache.setOnClickListener(v -> clearAppCache());
        
        binding.btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Về BookStore")
                    .setMessage("Phiên bản: 1.0.2\nChúc bạn có trải nghiệm tuyệt vời!")
                    .setPositiveButton("Đóng", null).show();
        });
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialogBinding.btnConfirm.setOnClickListener(v -> {
            String currentPass = dialogBinding.etCurrentPassword.getText().toString().trim();
            String newPass = dialogBinding.etNewPassword.getText().toString().trim();
            String confirmPass = dialogBinding.etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                dialogBinding.progressBar.setVisibility(View.VISIBLE);
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
                
                user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPass).addOnSuccessListener(aVoid1 -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        dialogBinding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    dialogBinding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                });
            }
        });

        dialog.show();
    }

    private void toggleTwoFactor() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            boolean currentStatus = doc.getBoolean("twoFactorEnabled") != null && doc.getBoolean("twoFactorEnabled");
            
            if (!currentStatus) {
                showSetupPinDialog();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Tắt xác thực 2 lớp")
                        .setMessage("Bạn có chắc chắn muốn tắt bảo mật 2 lớp?")
                        .setPositiveButton("Tắt 2FA", (dialog, which) -> {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("twoFactorEnabled", false);
                            updates.put("securityPin", "");
                            db.collection("users").document(uid).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Đã tắt 2FA", Toast.LENGTH_SHORT).show();
                                        update2FAStatusUI();
                                    });
                        })
                        .setNegativeButton("Hủy", null).show();
            }
        });
    }

    private void showSetupPinDialog() {
        DialogSetupPinBinding pinBinding = DialogSetupPinBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(pinBinding.getRoot())
                .create();

        pinBinding.btnSavePin.setOnClickListener(v -> {
            String pin = pinBinding.etNewPin.getText().toString().trim();
            if (pin.length() != 4) {
                Toast.makeText(getContext(), "Mã PIN phải có 4 số", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getUid();
            if (uid != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("twoFactorEnabled", true);
                updates.put("securityPin", pin);
                
                db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Đã bật xác thực 2 lớp!", Toast.LENGTH_SHORT).show();
                            update2FAStatusUI();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void clearAppCache() {
        try {
            File cacheDir = requireContext().getCacheDir();
            deleteDir(cacheDir);
            Toast.makeText(getContext(), "Đã dọn dẹp bộ nhớ đệm", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {}
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            for (String child : dir.list()) {
                if (!deleteDir(new File(dir, child))) return false;
            }
            return dir.delete();
        } else return dir != null && dir.isFile() && dir.delete();
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
