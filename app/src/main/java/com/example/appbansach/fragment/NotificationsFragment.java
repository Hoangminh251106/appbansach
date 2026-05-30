package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.NotificationAdapter;
import com.example.appbansach.databinding.FragmentNotificationsBinding;
import com.example.appbansach.model.NotificationModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private boolean isAdmin = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        setupRecyclerView();
        checkUserRoleAndLoad();

        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, isAdmin, new NotificationAdapter.OnNotificationListener() {
            @Override
            public void onNotificationClick(NotificationModel notification) {
                markAsRead(notification);
            }

            @Override
            public void onPushClick(NotificationModel notification) {
                // Logic gửi lại tin nhắn cho Admin
                sendNotification(notification.getUserId(), notification.getTitle(), notification.getContent());
                Toast.makeText(getContext(), "Đã đẩy lại thông báo!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void checkUserRoleAndLoad() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && binding != null) {
                String role = doc.getString("role");
                isAdmin = "admin".equals(role);
                adapter.setAdmin(isAdmin);
                
                if (isAdmin) {
                    binding.fabAddNotification.setVisibility(View.VISIBLE);
                    binding.fabAddNotification.setOnClickListener(v -> showAddNotificationDialog());
                    loadAllNotificationsForAdmin();
                } else {
                    binding.fabAddNotification.setVisibility(View.GONE);
                    loadNotificationsForUser(uid);
                }
            }
        });
    }

    private void loadAllNotificationsForAdmin() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("notifications")
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (value != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationModel notification = doc.toObject(NotificationModel.class);
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                        binding.tvEmpty.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadNotificationsForUser(String uid) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("notifications")
                .whereIn("userId", Arrays.asList(uid, "all"))
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (value != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationModel notification = doc.toObject(NotificationModel.class);
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                        binding.tvEmpty.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showAddNotificationDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_notification, null);
        EditText etTitle = view.findViewById(R.id.etNotiTitle);
        EditText etContent = view.findViewById(R.id.etNotiContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo thông báo mới")
                .setView(view)
                .setPositiveButton("Gửi cho tất cả", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (!title.isEmpty() && !content.isEmpty()) {
                        sendNotification("all", title, content);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendNotification(String targetUserId, String title, String content) {
        NotificationModel notification = new NotificationModel(title, content, targetUserId, Timestamp.now());
        db.collection("notifications").add(notification)
                .addOnSuccessListener(ref -> Toast.makeText(getContext(), "Đã gửi thông báo", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void markAsRead(NotificationModel notification) {
        if (!notification.isRead()) {
            db.collection("notifications").document(notification.getId()).update("isRead", true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
