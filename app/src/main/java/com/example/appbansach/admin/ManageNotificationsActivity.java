package com.example.appbansach.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.NotificationAdapter;
import com.example.appbansach.databinding.ActivityManageNotificationsBinding;
import com.example.appbansach.model.NotificationModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageNotificationsActivity extends AppCompatActivity {
    private ActivityManageNotificationsBinding binding;
    private FirebaseFirestore db;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        setupUI();
        loadNotifications();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        binding.rvNotifications.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendNotification());
    }

    private void sendNotification() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        NotificationModel notification = new NotificationModel(title, content, Timestamp.now());

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.etTitle.setText("");
                    binding.etContent.setText("");
                    Toast.makeText(this, "Đã gửi thông báo thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadNotifications() {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationModel item = doc.toObject(NotificationModel.class);
                            notificationList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
