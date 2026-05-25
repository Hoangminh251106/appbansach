package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentAdminDashboardBinding;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);

        binding.btnManageBooks.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageBooksFragment));

        binding.btnManageCategories.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageCategoriesFragment));

        binding.btnManageOrders.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageOrdersFragment));

        binding.btnManageVouchers.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageVouchersFragment));

        binding.btnStatistics.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_statisticsFragment));

        binding.btnBackToCustomer.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
