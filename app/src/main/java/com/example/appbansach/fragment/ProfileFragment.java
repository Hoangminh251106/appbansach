package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.activity.LoginActivity;
import com.example.appbansach.databinding.FragmentProfileBinding;
import com.example.appbansach.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
        });

        binding.btnOrderHistory.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_ordersFragment);
        });

        binding.btnWishlist.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_wishlistFragment);
        });

        binding.btnChatSupport.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_chatFragment);
        });

        binding.btnAdminPanel.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_adminDashboardFragment);
        });

        return binding.getRoot();
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).addSnapshotListener((documentSnapshot, error) -> {
            if (isAdded() && documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    binding.tvProfileName.setText(user.getFullName());
                    binding.tvProfileEmail.setText(user.getEmail());
                    
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(this)
                                .load(user.getAvatarUrl())
                                .placeholder(android.R.drawable.ic_menu_myplaces)
                                .into(binding.ivAvatar);
                    }

                    if ("admin".equals(user.getRole())) {
                        binding.btnAdminPanel.setVisibility(View.VISIBLE);
                    } else {
                        binding.btnAdminPanel.setVisibility(View.GONE);
                    }
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
