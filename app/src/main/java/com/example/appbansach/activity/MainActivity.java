package com.example.appbansach.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityMainBinding;
import com.example.appbansach.helper.CartManager;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (binding == null) return;
                // Tự động ẩn/hiện BottomNavigation ở màn hình Auth và Splash
                int id = destination.getId();
                if (id == R.id.loginFragment || 
                    id == R.id.registerFragment || 
                    id == R.id.splashFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                }
            });
        }

        // YÊU CẦU 4: Hiển thị Badge số lượng Giỏ hàng
        setupCartBadge();
    }

    private void setupCartBadge() {
        CartManager.getInstance(this).getCartItems().observe(this, items -> {
            if (binding == null) return;
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment);
            if (items != null && !items.isEmpty()) {
                badge.setVisible(true);
                badge.setNumber(items.size());
                badge.setBackgroundColor(getResources().getColor(R.color.sage_primary_dark));
                badge.setBadgeTextColor(getResources().getColor(R.color.white));
            } else {
                badge.setVisible(false);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
