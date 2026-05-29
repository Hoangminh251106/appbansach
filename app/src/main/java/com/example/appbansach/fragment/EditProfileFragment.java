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
import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentEditProfileBinding;
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
    private Uri imageUri;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivUserAvatar.setImageURI(uri);
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

        loadUserData();

        binding.ivUserAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> updateProfile());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void loadUserData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && isAdded()) {
                binding.etFullName.setText(documentSnapshot.getString("fullName"));
                binding.etPhone.setText(documentSnapshot.getString("phone"));
                binding.etAddress.setText(documentSnapshot.getString("address"));
                String avatarUrl = documentSnapshot.getString("avatarUrl");
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this).load(avatarUrl).circleCrop().placeholder(R.drawable.ic_profile).into(binding.ivUserAvatar);
                }
            }
        });
    }

    private void updateProfile() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Đã sửa: Sử dụng progressBar thay vì toolbar để tránh làm mất thanh tiêu đề khi đang lưu
        binding.progressBar.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            StorageReference ref = storage.getReference().child("avatars/" + uid + ".jpg");
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                saveToFirestore(uri.toString());
            })).addOnFailureListener(e -> {
                if (isAdded()) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (e.getMessage() != null && !e.getMessage().contains("Object does not exist")) {
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            saveToFirestore(null);
        }
    }

    private void saveToFirestore(String avatarUrl) {
        String uid = mAuth.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", binding.etFullName.getText().toString());
        user.put("phone", binding.etPhone.getText().toString());
        user.put("address", binding.etAddress.getText().toString());
        if (avatarUrl != null) {
            user.put("avatarUrl", avatarUrl);
        }

        db.collection("users").document(uid).update(user).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
