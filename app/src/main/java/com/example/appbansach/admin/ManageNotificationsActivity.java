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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageNotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationListener {
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
        adapter = new NotificationAdapter(notificationList, true, this);
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
        NotificationModel notification = new NotificationModel(title, content, "all", Timestamp.now());

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.etTitle.setText("");
                    binding.etContent.setText("");
                    Toast.makeText(this, "Đã lưu thông báo", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadNotifications() {
        db.collection("notifications")
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationModel item = doc.toObject(NotificationModel.class);
                            item.setId(doc.getId());
                            notificationList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        // Có thể thêm chức năng xem chi tiết hoặc xóa
    }

    @Override
    public void onPushClick(NotificationModel notification) {
        // Chức năng đẩy tin: Cập nhật vào một document đặc biệt để User nhận Marquee
        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("title", notification.getTitle());
        broadcast.put("content", notification.getContent());
        broadcast.put("timestamp", Timestamp.now());
        broadcast.put("active", true);

        db.collection("system").document("broadcast").set(broadcast)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã đẩy tin nhắn chạy màn hình!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
