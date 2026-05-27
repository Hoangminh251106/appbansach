package com.example.appbansach.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentSplashBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashFragment extends Fragment {
    private FragmentSplashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Luôn chuyển đến màn hình Đăng nhập khi vừa vào App
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && binding != null) {
                // Đăng xuất tài khoản cũ nếu muốn luôn hiện màn hình login (tùy chọn)
                // FirebaseAuth.getInstance().signOut(); 
                
                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
            }
        }, 1200);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
