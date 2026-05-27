package com.example.appbansach.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.UserAdapter;
import com.example.appbansach.databinding.ActivityManageUsersBinding;
import com.example.appbansach.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private ActivityManageUsersBinding binding;
    private FirebaseFirestore db;
    private UserAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList);
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").addSnapshotListener((value, error) -> {
            binding.progressBar.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                userList.clear();
                int total = 0;
                int active = 0;
                int locked = 0;

                for (QueryDocumentSnapshot doc : value) {
                    User user = doc.toObject(User.class);
                    user.setUid(doc.getId());
                    userList.add(user);

                    total++;
                    String status = user.getStatus() != null ? user.getStatus() : "active";
                    if (status.equals("active")) {
                        active++;
                    } else {
                        locked++;
                    }
                }

                binding.tvTotalUsers.setText(String.valueOf(total));
                binding.tvActiveUsers.setText(String.valueOf(active));
                binding.tvLockedUsers.setText(String.valueOf(locked));

                adapter.updateList(userList);
                
                if (userList.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }
        });
    }
}
