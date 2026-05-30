package com.example.appbansach.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.example.appbansach.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        
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
    }

    private void setupClickListeners() {
        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(getContext(), (isChecked ? "Đã bật" : "Đã tắt") + " nhận thông báo", Toast.LENGTH_SHORT).show();
        });

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.switchMarquee.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("marquee_enabled", isChecked).apply();
            Toast.makeText(getContext(), (isChecked ? "Đã bật" : "Đã tắt") + " thông báo chạy ngang", Toast.LENGTH_SHORT).show();
        });

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnTwoFactor.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng xác thực 2 lớp đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnClearCache.setOnClickListener(v -> clearAppCache());

        binding.btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Về BookStore")
                    .setMessage("Ứng dụng quản lý và mua bán sách trực tuyến.\nPhiên bản: 1.0.2\nChúc bạn đọc sách vui vẻ!")
                    .setPositiveButton("Đóng", null)
                    .show();
        });
    }

    private void clearAppCache() {
        try {
            File cacheDir = requireContext().getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
                Toast.makeText(getContext(), "Đã xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void showChangePasswordDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setMessage("Hệ thống sẽ gửi một liên kết đổi mật khẩu vào email của bạn. Bạn có muốn tiếp tục?")
                .setPositiveButton("Gửi Email", (dialog, which) -> {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    if (email != null) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Vui lòng kiểm tra Email để đổi mật khẩu", Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
