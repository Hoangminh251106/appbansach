package com.example.appbansach.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.appbansach.R;
import com.example.appbansach.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userRole = null;

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
                
                // Ẩn BottomNav ở màn hình Auth/Splash
                if (id == R.id.loginFragment || id == R.id.registerFragment || id == R.id.splashFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    // Kiểm tra Role để ẩn/hiện BottomNav
                    checkRoleAndToggleBottomNav();
                }
            });
        }
    }

    private void checkRoleAndToggleBottomNav() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            binding.bottomNavigation.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (binding == null) return;
                if (documentSnapshot.exists()) {
                    userRole = documentSnapshot.getString("role");
                    if ("admin".equals(userRole)) {
                        // Nếu là admin, ẩn hoàn toàn Bottom Navigation
                        binding.bottomNavigation.setVisibility(View.GONE);
                    } else {
                        // Nếu là user thường, hiện Bottom Navigation
                        binding.bottomNavigation.setVisibility(View.VISIBLE);
                    }
                }
            });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
