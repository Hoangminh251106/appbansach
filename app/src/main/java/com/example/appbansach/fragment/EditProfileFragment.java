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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

    // Launcher mở thư viện ảnh
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivUserAvatar.setImageURI(uri);
                    Toast.makeText(getContext(), "Đã chọn ảnh mới", Toast.LENGTH_SHORT).show();
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

        // Nút Cập nhật ảnh đại diện
        binding.btnChangeAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        
        // Cho phép nhấn vào cả cái ảnh để chọn
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
                String role = documentSnapshot.getString("role");
                if ("admin".equals(role)) {
                    binding.toolbar.setTitle("Chỉnh sửa thông tin Admin");
                } else {
                    binding.toolbar.setTitle("Chỉnh sửa thông tin cá nhân");
                }

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
        if (data.length() > 200) { // Nếu là chuỗi Base64
            try {
                byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
                Glide.with(this).asBitmap().load(decodedString).circleCrop().into(binding.ivUserAvatar);
            } catch (Exception e) {
                Glide.with(this).load(data).circleCrop().into(binding.ivUserAvatar);
            }
        } else { // Nếu là URL (cho dữ liệu cũ)
            Glide.with(this).load(data).circleCrop().placeholder(R.drawable.ic_profile).into(binding.ivUserAvatar);
        }
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        // Kiểm tra đổi mật khẩu
        if (!newPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                stopLoading("Vui lòng nhập mật khẩu hiện tại");
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                user.updatePassword(newPassword).addOnSuccessListener(aVoid1 -> {
                    processImageAndSave();
                }).addOnFailureListener(e -> stopLoading("Lỗi đổi mật khẩu: " + e.getMessage()));
            }).addOnFailureListener(e -> stopLoading("Mật khẩu hiện tại không đúng"));
        } else {
            processImageAndSave();
        }
    }

    private void processImageAndSave() {
        if (imageUri != null) {
            // Nén và chuyển ảnh sang Base64 để lưu thẳng vào Firestore (bỏ qua Storage lỗi)
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
            
            // Nén ảnh nhỏ lại để không quá tải Firestore (max 400px)
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
                stopLoading("Cập nhật thành công!");
                Navigation.findNavController(requireView()).navigateUp();
            }
        }).addOnFailureListener(e -> stopLoading("Lỗi lưu dữ liệu: " + e.getMessage()));
    }

    private void stopLoading(String message) {
        if (isAdded() && binding != null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
