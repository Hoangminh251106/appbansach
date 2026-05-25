package com.example.appbansach.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.FragmentEditProfileBinding;
import com.example.appbansach.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private User currentUser;
    private Uri imageUri;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivEditAvatar.setImageURI(uri);
                    binding.ivEditAvatar.setPadding(0,0,0,0);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            loadUserData();
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.cardAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        
        binding.btnSave.setOnClickListener(v -> saveChanges());

        return binding.getRoot();
    }

    private void loadUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        binding.etFullName.setText(currentUser.getFullName());
                        binding.etPhone.setText(currentUser.getPhone());
                        binding.etAddress.setText(currentUser.getAddress());
                        
                        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                            Glide.with(this).load(currentUser.getAvatarUrl()).into(binding.ivEditAvatar);
                            binding.ivEditAvatar.setPadding(0,0,0,0);
                        }
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

        if (imageUri != null) {
            uploadAvatarAndSave(fullName, phone, address);
        } else {
            updateFirestore(null, fullName, phone, address);
        }
    }

    private void uploadAvatarAndSave(String fullName, String phone, String address) {
        String fileName = "avatars/" + userId + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            updateFirestore(uri.toString(), fullName, phone, address);
        })).addOnFailureListener(e -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFirestore(String avatarUrl, String fullName, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        updates.put("address", address);
        if (avatarUrl != null) updates.put("avatarUrl", avatarUrl);

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
