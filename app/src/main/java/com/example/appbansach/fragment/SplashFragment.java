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
import com.google.firebase.auth.FirebaseUser;

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
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && binding != null) {
                checkUserStatus();
            }
        }, 1500);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Nếu đã đăng nhập, vào thẳng màn hình Home
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_homeFragment);
        } else {
            // Nếu chưa, yêu cầu đăng nhập
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
