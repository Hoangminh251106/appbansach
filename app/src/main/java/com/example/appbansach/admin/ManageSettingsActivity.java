package com.example.appbansach.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbansach.databinding.ActivityManageSettingsBinding;

public class ManageSettingsActivity extends AppCompatActivity {
    private ActivityManageSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
