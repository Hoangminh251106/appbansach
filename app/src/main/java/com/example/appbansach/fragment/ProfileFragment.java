package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.admin.ManageReviewsActivity;
import com.example.appbansach.admin.ManageShippingActivity;
import com.example.appbansach.admin.ManageUsersActivity;
import com.example.appbansach.databinding.FragmentProfileBinding;
import com.example.appbansach.model.User;
import com.example.appbansach.ui.viewmodel.BookViewModel;
import com.example.appbansach.ui.auth.AuthViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        observeUser();
        setupClickListeners();
        loadUserStats();

        return binding.getRoot();
    }

    private void observeUser() {
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == com.example.appbansach.utils.Resource.Status.SUCCESS && resource.data != null) {
                updateUI(resource.data);
            }
        });
    }

    private void updateUI(User user) {
        if (binding == null) return;

        binding.tvUserName.setText(user.getFullName());
        binding.tvUserEmail.setText(user.getEmail());
        displayAvatar(user.getAvatarUrl(), binding.ivUserAvatar);

        boolean isAdmin = "admin".equals(user.getRole());
        binding.adminSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.btnMyOrders.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
    }

    private void loadUserStats() {
        String uid = authViewModel.getCurrentUid();
        if (uid == null) return;
        
        // Load số lượng đơn hàng
        FirebaseFirestore.getInstance().collection("orders")
                .whereEqualTo("userId", uid)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) binding.tvOrderCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
        
        // Load số lượng đánh giá
        FirebaseFirestore.getInstance().collection("reviews")
                .whereEqualTo("userId", uid)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) binding.tvReviewCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    private void setupClickListeners() {
        // User Actions
        binding.btnEditProfile.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_editProfileFragment));
        binding.btnMyOrders.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_ordersFragment));
        
        // Chatbot / Hỗ trợ
        binding.btnChatSupport.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_chatFragment));
        
        // Cài đặt
        binding.btnSettings.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_settingsFragment));

        // Admin Actions
        binding.cardDashboard.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_statisticsFragment));
        binding.cardManageBooks.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_manageBooksFragment));
        binding.cardManageCategories.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_manageCategoriesFragment));
        binding.cardManageOrders.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_manageOrdersFragment));
        binding.cardNotifications.setOnClickListener(v -> navigateTo(R.id.action_profileFragment_to_notificationsFragment));
        
        binding.cardShipping.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageShippingActivity.class)));
        binding.cardReviews.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageReviewsActivity.class)));
        binding.cardManageUsers.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageUsersActivity.class)));

        binding.btnLogout.setOnClickListener(this::showLogoutConfirmation);
    }

    private void navigateTo(int actionId) {
        if (isAdded() && getView() != null) {
            try {
                Navigation.findNavController(requireView()).navigate(actionId);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Tính năng đang được cập nhật", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayAvatar(String url, ImageView imageView) {
        if (url == null || url.isEmpty() || !isAdded()) return;
        try {
            Object source = url.length() > 200 ? Base64.decode(url, Base64.DEFAULT) : url;
            Glide.with(this).load(source).circleCrop().placeholder(R.drawable.ic_profile).into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_profile);
        }
    }

    private void showLogoutConfirmation(View v) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn thoát?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    authViewModel.logout();
                    Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_loginFragment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
