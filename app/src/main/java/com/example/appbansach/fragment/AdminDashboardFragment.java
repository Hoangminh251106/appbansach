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
import com.example.appbansach.admin.ManageShippingActivity;
import com.example.appbansach.databinding.FragmentAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.btnManageBooks.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageBooksFragment));

        binding.btnManageCategories.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageCategoriesFragment));

        binding.btnManageOrders.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageOrdersFragment));

        binding.btnManageShipping.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageShippingActivity.class);
            startActivity(intent);
        });

        binding.btnManageVouchers.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageVouchersFragment));

        binding.btnStatistics.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_statisticsFragment));

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
