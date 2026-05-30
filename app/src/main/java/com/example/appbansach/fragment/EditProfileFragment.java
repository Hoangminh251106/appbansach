package com.example.appbansach.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
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

        loadUserData();

        binding.btnChangeAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        binding.ivUserAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> updateProfile());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void loadUserData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && isAdded() && binding != null) {
                binding.etFullName.setText(documentSnapshot.getString("fullName"));
                binding.etPhone.setText(documentSnapshot.getString("phone"));
                binding.etAddress.setText(documentSnapshot.getString("address"));
                
                String avatarData = documentSnapshot.getString("avatarUrl");
                if (avatarData != null && !avatarData.isEmpty()) {
                    displayAvatar(avatarData);
                }
            }
        });
    }

    private void displayAvatar(String data) {
        try {
            if (data.length() > 200) {
                byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
                Glide.with(this).asBitmap().load(decodedString).circleCrop().into(binding.ivUserAvatar);
            } else {
                Glide.with(this).load(data).circleCrop().placeholder(R.drawable.ic_profile).into(binding.ivUserAvatar);
            }
        } catch (Exception e) {
            binding.ivUserAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        if (imageUri != null) {
            String base64Image = encodeImageToBase64(imageUri);
            saveToFirestore(base64Image);
        } else {
            saveToFirestore(null);
        }
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int maxSize = 400;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = (float) width / (float) height;
            if (ratio > 1) {
                width = maxSize;
                height = (int) (width / ratio);
            } else {
                height = maxSize;
                width = (int) (height * ratio);
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("Base64Error", "Error encoding image", e);
            return null;
        }
    }

    private void saveToFirestore(String avatarBase64) {
        String uid = mAuth.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", binding.etFullName.getText().toString().trim());
        updates.put("phone", binding.etPhone.getText().toString().trim());
        updates.put("address", binding.etAddress.getText().toString().trim());
        if (avatarBase64 != null) {
            updates.put("avatarUrl", avatarBase64);
        }

        db.collection("users").document(uid).update(updates).addOnSuccessListener(aVoid -> {
            if (isAdded() && binding != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
