package com.example.appbansach.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentProfileBinding;
import com.example.appbansach.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // --- Click cho Menu Người dùng ---
        binding.btnPersonalInfo.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        // Lưu ý: Các nút khác cho User có thể được thêm vào layoutUserMenu nếu cần

        // --- Click cho Menu Admin Dashboard ---
        binding.cardManageBooks.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageBooksFragment));

        binding.cardManageCategories.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageCategoriesFragment));

        binding.cardManageOrders.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageOrdersFragment));

        binding.cardManageVouchers.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageVouchersFragment));

        binding.cardStatistics.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_statisticsFragment));

        binding.btnAdminLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        });

        binding.btnCustomerMode.setOnClickListener(v -> {
            // Chuyển sang Trang chủ (Khách hàng)
            Navigation.findNavController(v).navigate(R.id.homeFragment);
        });

        // --- Logout cho User ---
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        });
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).addSnapshotListener((documentSnapshot, error) -> {
            if (isAdded() && documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    if ("admin".equals(user.getRole())) {
                        binding.tvAdminName.setText(user.getFullName());
                        binding.layoutUserMenu.setVisibility(View.GONE);
                        binding.layoutAdminMenu.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvProfileName.setText(user.getFullName());
                        binding.tvProfileEmail.setText(user.getEmail());
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(this).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_myplaces).into(binding.ivAvatar);
                        }
                        binding.layoutUserMenu.setVisibility(View.VISIBLE);
                        binding.layoutAdminMenu.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật mật khẩu");
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText oldPass = new EditText(requireContext());
        oldPass.setHint("Mật khẩu hiện tại");
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPass);

        final EditText newPass = new EditText(requireContext());
        newPass.setHint("Mật khẩu mới");
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPass);

        final EditText confirmPass = new EditText(requireContext());
        confirmPass.setHint("Xác nhận mật khẩu mới");
        confirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPass);

        builder.setView(layout);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String op = oldPass.getText().toString();
            String np = newPass.getText().toString();
            String cp = confirmPass.getText().toString();
            if (np.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!np.equals(cp)) {
                Toast.makeText(getContext(), "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            updatePasswordLogic(op, np);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updatePasswordLogic(String oldPass, String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
