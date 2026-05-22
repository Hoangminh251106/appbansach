package com.example.bookstore.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.appbansach.databinding.FragmentForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnResetPassword.setOnClickListener(v -> {
            String email = binding.etForgotEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnResetPassword.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnResetPassword.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(requireView()).navigateUp();
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        binding.tvBackToLogin.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}