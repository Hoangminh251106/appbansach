package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.admin.ManageNotificationsActivity;
import com.example.appbansach.admin.ManagePaymentsActivity;
import com.example.appbansach.admin.ManageReviewsActivity;
import com.example.appbansach.admin.ManageShippingActivity;
import com.example.appbansach.admin.ManageUsersActivity;
import com.example.appbansach.databinding.FragmentProfileBinding;
import com.example.appbansach.model.User;
import com.example.appbansach.ui.auth.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthViewModel authViewModel;
    private ListenerRegistration notiListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        loadUserProfile();
        setupClickListeners();
        setupUnreadNotificationBadge();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // --- User Menu ---
        binding.btnPersonalInfo.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));

        binding.btnWishlist.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_wishlistFragment));

        binding.btnOrderHistory.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_ordersFragment));

        binding.btnNotifications.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_notificationsFragment));

        binding.btnChatSupport.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_chatFragment));

        binding.btnAdminMode.setOnClickListener(v -> {
            binding.layoutUserMenu.setVisibility(View.GONE);
            binding.layoutAdminMenu.setVisibility(View.VISIBLE);
        });

        // --- Admin Dashboard ---
        binding.cardManageBooks.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageBooksFragment));
        binding.cardManageCategories.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageCategoriesFragment));
        binding.cardManageOrders.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageOrdersFragment));
        binding.cardManageVouchers.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_manageVouchersFragment));
        binding.cardStatistics.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_statisticsFragment));
        binding.cardEditAdminInfo.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment));
        
        binding.cardManageUsers.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageUsersActivity.class)));
        binding.cardShipping.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageShippingActivity.class)));
        binding.cardReviews.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageReviewsActivity.class)));
        binding.cardNotifications.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageNotificationsActivity.class)));
        binding.cardPayments.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManagePaymentsActivity.class)));

        binding.btnLogout.setOnClickListener(this::showLogoutConfirmation);
    }

    private void setupUnreadNotificationBadge() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        notiListener = db.collection("notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (binding == null || value == null) return;
                    int count = value.size();
                    if (count > 0) {
                        binding.btnNotifications.setText(getString(R.string.notifications_with_count, count));
                    } else {
                        binding.btnNotifications.setText(R.string.notifications_label);
                    }
                });
    }

    private void showLogoutConfirmation(View v) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_msg)
                .setPositiveButton(R.string.logout, (dialog, which) -> performLogout(v))
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_lock_power_off)
                .show();
    }

    private void performLogout(View v) {
        authViewModel.logout();
        showToast(getString(R.string.logout_success));
        Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_loginFragment);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).addSnapshotListener((documentSnapshot, error) -> {
            if (isAdded() && documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    if ("admin".equals(user.getRole())) {
                        setAdminUI(user);
                    } else {
                        setUserUI(user);
                    }
                }
            }
        });
    }

    private void setAdminUI(User user) {
        binding.tvAdminName.setText(user.getFullName());
        binding.layoutUserMenu.setVisibility(View.GONE);
        binding.layoutAdminMenu.setVisibility(View.VISIBLE);
        binding.btnAdminMode.setVisibility(View.VISIBLE);
        binding.btnLogout.setVisibility(View.VISIBLE);
    }

    private void setUserUI(User user) {
        binding.tvProfileName.setText(user.getFullName());
        binding.tvProfileEmail.setText(user.getEmail());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_myplaces).into(binding.ivAvatar);
        }
        binding.layoutUserMenu.setVisibility(View.VISIBLE);
        binding.layoutAdminMenu.setVisibility(View.GONE);
        binding.btnAdminMode.setVisibility(View.GONE);
        binding.btnLogout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        if (notiListener != null) notiListener.remove();
        super.onDestroyView();
        binding = null;
    }
}
