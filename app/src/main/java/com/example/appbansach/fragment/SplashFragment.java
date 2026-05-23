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
        
        // Đợi 3 giây rồi chuyển sang màn hình Đăng nhập
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
            }
        }, 3000);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
