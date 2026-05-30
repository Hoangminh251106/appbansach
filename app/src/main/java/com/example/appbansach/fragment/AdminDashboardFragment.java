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

import com.example.appbansach.R;
import com.example.appbansach.admin.ManageNotificationsActivity;
import com.example.appbansach.admin.ManageReviewsActivity;
import com.example.appbansach.admin.ManageShippingActivity;
import com.example.appbansach.databinding.FragmentAdminDashboardBinding;
import com.example.appbansach.model.ShippingCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeDefaultShippingCodes();
        setupClickListeners();

        return binding.getRoot();
    }

    private void initializeDefaultShippingCodes() {
        // Tự động tạo mã mẫu nếu chưa có (để người dùng dễ hình dung)
        db.collection("shipping_codes").document("FREESHIP").get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                db.collection("shipping_codes").document("FREESHIP").set(new ShippingCode("FREESHIP", 30000, 150000));
                db.collection("shipping_codes").document("SHIP15K").set(new ShippingCode("SHIP15K", 15000, 50000));
            }
        });
    }

    private void setupClickListeners() {
        binding.btnManageBooks.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageBooksFragment));

        binding.btnManageOrders.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageOrdersFragment));

        binding.btnStatistics.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_statisticsFragment));

        binding.btnManageVouchers.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageVouchersFragment));

        binding.btnManageShippingCodes.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageShippingCodesFragment));

        binding.btnManageReviews.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageReviewsActivity.class);
            startActivity(intent);
        });

        binding.btnBackToCustomer.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        binding.btnAdminLogout.setOnClickListener(this::performLogout);
    }

    private void performLogout(View v) {
        mAuth.signOut();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
