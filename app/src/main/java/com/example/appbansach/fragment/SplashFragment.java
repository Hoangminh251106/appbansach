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
import com.google.firebase.firestore.FirebaseFirestore;

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
            // Kiểm tra nhanh qua email
            String email = currentUser.getEmail();
            if (email != null && email.toLowerCase().contains("admin")) {
                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_profileFragment);
                return;
            }

            // Kiểm tra role trong Firestore để chắc chắn
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && binding != null) {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if ("admin".equals(role)) {
                                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_profileFragment);
                            } else {
                                Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_homeFragment);
                            }
                        } else {
                            // Nếu không có doc trong firestore, mặc định vào home hoặc yêu cầu login lại
                            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_homeFragment);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_homeFragment);
                    }
                });
        } else {
            // Nếu chưa đăng nhập, yêu cầu đăng nhập
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
