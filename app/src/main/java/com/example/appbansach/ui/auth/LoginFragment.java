package com.example.appbansach.ui.auth;

import android.os.Bundle;
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
import com.example.appbansach.databinding.FragmentLoginBinding;
import com.example.appbansach.model.User;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        binding.tvForgotPassword.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_forgotPasswordFragment));

        binding.tvRegister.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment));

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getLoginStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnLogin.setEnabled(false);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        
                        User user = resource.data;
                        if (user != null && "admin".equals(user.getRole())) {
                            // Nếu là admin, chuyển thẳng đến Profile (nơi chứa menu quản lý)
                            Navigation.findNavController(requireView()).navigate(R.id.profileFragment);
                        } else {
                            // Nếu là khách hàng, chuyển đến Home
                            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_homeFragment);
                        }
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
