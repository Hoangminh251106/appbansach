package com.example.appbansach.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.ChatAdapter;
import com.example.appbansach.databinding.FragmentChatBinding;
import com.example.appbansach.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private String adminId = "admin_uid"; // Trong thực tế, đây là UID của tài khoản admin

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();
        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(adapter);
    }

    private void loadMessages() {
        // Lấy tin nhắn giữa người dùng hiện tại và admin
        db.collection("chats")
                .whereIn("senderId", List.of(currentUserId, adminId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        chatList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            // Lọc tin nhắn thuộc hội thoại này
                            if ((msg.getSenderId().equals(currentUserId) && msg.getReceiverId().equals(adminId)) ||
                                (msg.getSenderId().equals(adminId) && msg.getReceiverId().equals(currentUserId))) {
                                chatList.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.rvChat.scrollToPosition(chatList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage message = new ChatMessage(currentUserId, adminId, text, Timestamp.now());
        db.collection("chats").add(message)
                .addOnSuccessListener(v -> binding.etMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
