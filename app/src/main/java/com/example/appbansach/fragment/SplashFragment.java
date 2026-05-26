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
        
        // Nghỉ 800ms để hệ thống kịp vẽ giao diện Splash trước khi chuyển màn hình
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && binding != null) {
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
                } catch (Exception e) {
                    // Nếu lỗi điều hướng, thử lại sau 1s
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
                    }, 1000);
                }
            }
        }, 800);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
