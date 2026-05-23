package com.example.appbansach.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentRegisterBinding;
import com.example.appbansach.utils.Resource;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Sử dụng chung ViewModel của gói appbansach gắn với Activity
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || 
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(email, password, fullName);
        });

        binding.tvLogin.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        observeViewModel();
    }

    private void observeViewModel() {
        // Quan sát registerStatus từ AuthViewModel của appbansach
        viewModel.getRegisterStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
