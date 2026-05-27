package com.example.appbansach.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbansach.databinding.ActivityManagePaymentsBinding;

public class ManagePaymentsActivity extends AppCompatActivity {
    private ActivityManagePaymentsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagePaymentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
