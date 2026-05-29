package com.example.appbansach.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityMainBinding;
import com.example.appbansach.helper.CartManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (binding == null) return;
                int id = destination.getId();
                if (id == R.id.loginFragment || 
                    id == R.id.registerFragment || 
                    id == R.id.splashFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    checkRoleAndToggleBottomNavItems();
                }
            });
        }

        setupCartBadge();
    }

    private void checkRoleAndToggleBottomNavItems() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        // Kiểm tra nhanh qua email
        if (email != null && email.toLowerCase().contains("admin")) {
            updateBottomNavVisibility("admin");
            return;
        }

        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (binding == null) return;
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    updateBottomNavVisibility(role);
                }
            });
    }

    private void updateBottomNavVisibility(String role) {
        if (binding == null) return;
        Menu menu = binding.bottomNavigation.getMenu();
        boolean isAdmin = "admin".equals(role);
        
        // Xóa/Ẩn các mục Trang chủ, Tìm kiếm (Danh mục), Giỏ hàng cho Admin
        if (menu.findItem(R.id.homeFragment) != null) {
            menu.findItem(R.id.homeFragment).setVisible(!isAdmin);
        }
        if (menu.findItem(R.id.searchFragment) != null) {
            menu.findItem(R.id.searchFragment).setVisible(!isAdmin);
        }
        if (menu.findItem(R.id.cartFragment) != null) {
            menu.findItem(R.id.cartFragment).setVisible(!isAdmin);
        }
    }

    private void setupCartBadge() {
        CartManager.getInstance(this).getCartItems().observe(this, items -> {
            if (binding == null) return;
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment);
            if (items != null && !items.isEmpty()) {
                badge.setVisible(true);
                badge.setNumber(items.size());
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
