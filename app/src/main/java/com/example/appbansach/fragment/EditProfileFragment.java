package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.appbansach.databinding.FragmentEditProfileBinding;
import com.example.appbansach.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            loadUserData();
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnSave.setOnClickListener(v -> saveChanges());

        return binding.getRoot();
    }

    private void loadUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        // Sửa từ "Users" thành "users" để đồng bộ
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        binding.etFullName.setText(user.getFullName());
                        binding.etPhone.setText(user.getPhone());
                        binding.etAddress.setText(user.getAddress());
                    }
                }
            }
        });
    }

    private void saveChanges() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.etFullName.setError("Vui lòng nhập họ tên");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        updates.put("address", address);

        // Sửa từ "Users" thành "users" để đồng bộ
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
