package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.admin.ManageNotificationsActivity;
import com.example.appbansach.admin.ManagePaymentsActivity;
import com.example.appbansach.admin.ManageReviewsActivity;
import com.example.appbansach.admin.ManageSettingsActivity;
import com.example.appbansach.admin.ManageShippingActivity;
import com.example.appbansach.admin.ManageUsersActivity;
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
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // --- Click cho Menu Người dùng ---
        binding.btnPersonalInfo.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        binding.btnWishlist.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_wishlistFragment));

        binding.btnOrderHistory.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_ordersFragment));

        binding.btnChatSupport.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_chatFragment));

        binding.btnAdminMode.setOnClickListener(v -> {
            binding.layoutUserMenu.setVisibility(View.GONE);
            binding.layoutAdminMenu.setVisibility(View.VISIBLE);
        });

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

        binding.cardEditAdminInfo.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        binding.cardManageUsers.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManageUsersActivity.class)));

        binding.cardShipping.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManageShippingActivity.class)));

        binding.cardReviews.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManageReviewsActivity.class)));

        binding.cardNotifications.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManageNotificationsActivity.class)));

        binding.cardPayments.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManagePaymentsActivity.class)));

        binding.cardSettings.setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ManageSettingsActivity.class)));

        // --- Nút Đăng xuất cho Admin ---
        binding.btnAdminLogout.setOnClickListener(v -> performLogout(v));
        binding.btnAdminLogoutLarge.setOnClickListener(v -> performLogout(v));

        // --- Nút Đăng xuất cho User (Nút đỏ dưới cùng) ---
        binding.btnLogout.setOnClickListener(v -> performLogout(v));
    }

    private void performLogout(View v) {
        mAuth.signOut();
        showToast("Đã đăng xuất");
        Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_loginFragment);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
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
                        binding.btnAdminMode.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvProfileName.setText(user.getFullName());
                        binding.tvProfileEmail.setText(user.getEmail());
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(this).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_myplaces).into(binding.ivAvatar);
                        }
                        binding.layoutUserMenu.setVisibility(View.VISIBLE);
                        binding.layoutAdminMenu.setVisibility(View.GONE);
                        binding.btnAdminMode.setVisibility(View.GONE);
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
