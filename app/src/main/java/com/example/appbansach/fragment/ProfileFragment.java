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

        return binding.getRoot();
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        // Sửa từ "Users" thành "users" để đồng bộ
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    binding.tvProfileName.setText(user.getFullName());
                    binding.tvProfileEmail.setText(user.getEmail());
                    
                    if ("admin".equals(user.getRole())) {
                        binding.btnAdminPanel.setVisibility(View.VISIBLE);
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
