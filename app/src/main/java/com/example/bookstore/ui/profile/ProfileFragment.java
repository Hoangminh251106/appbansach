package com.example.bookstore.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.appbansach.R;
import com.example.bookstore.data.model.User;
import com.example.appbansach.databinding.FragmentProfileBinding;
import com.example.appbansach.utils.Resource;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        observeViewModel();

        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
        });

        binding.btnOrderHistory.setOnClickListener(v -> {
            // Navigation.findNavController(requireView()).navigate(R.id.orderHistoryFragment);
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.userProfile.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    // Show small loading if needed
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        User user = resource.data;
                        binding.tvProfileName.setText(user.getFullName());
                        binding.tvProfileEmail.setText(user.getEmail());
                        
                        if ("admin".equals(user.getRole())) {
                            binding.btnAdminPanel.setVisibility(View.VISIBLE);
                            binding.btnAdminPanel.setOnClickListener(v -> {
                                // Navigate to Admin Dashboard
                                Toast.makeText(getContext(), "Admin Panel", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}