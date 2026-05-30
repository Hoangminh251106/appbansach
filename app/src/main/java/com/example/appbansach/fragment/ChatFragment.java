package com.example.appbansach.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.ChatAdapter;
import com.example.appbansach.adapter.ChatSessionAdapter;
import com.example.appbansach.databinding.FragmentChatBinding;
import com.example.appbansach.model.ChatMessage;
import com.example.appbansach.model.ChatSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatSessionAdapter.OnSessionClickListener {
    private FragmentChatBinding binding;
    private ChatAdapter chatAdapter;
    private ChatSessionAdapter sessionAdapter;
    
    private final List<ChatMessage> chatList = new ArrayList<>();
    private final List<ChatSession> sessionList = new ArrayList<>();
    
    private FirebaseFirestore db;
    private CollectionReference sessionsRef;
    private String currentUserId;
    private String currentSessionId;
    
    private ListenerRegistration messageListener;
    private ListenerRegistration sessionListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            sessionsRef = db.collection("chats").document(currentUserId).collection("sessions");
            setupChatRecycler();
            setupHistoryRecycler();
            setupDrawerAndSearch();
            listenForSessions();
        }

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.toolbar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.btnNewSession.setOnClickListener(v -> startNewChat());

        return binding.getRoot();
    }

    private void setupChatRecycler() {
        chatAdapter = new ChatAdapter(chatList, this::showRecallDialog);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupHistoryRecycler() {
        sessionAdapter = new ChatSessionAdapter(sessionList, this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistory.setAdapter(sessionAdapter);
    }

    private void setupDrawerAndSearch() {
        binding.searchHistory.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sessionAdapter.filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                sessionAdapter.filter(newText);
                return false;
            }
        });
    }

    private void listenForSessions() {
        sessionListener = sessionsRef.orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !isAdded()) return;
                    sessionList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatSession session = doc.toObject(ChatSession.class);
                        if (session != null) {
                            session.setId(doc.getId());
                            sessionList.add(session);
                        }
                    }
                    sessionAdapter.updateList(sessionList);
                });
    }

    @Override
    public void onSessionClick(ChatSession session) {
        loadSession(session.getId(), session.getTitle());
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onSessionDelete(ChatSession session) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa đoạn chat")
                .setMessage("Bạn có chắc muốn xóa lịch sử đoạn chat này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteSessionRecursive(session.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSessionRecursive(String sessionId) {
        sessionsRef.document(sessionId).collection("messages").get().addOnSuccessListener(querySnapshot -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : querySnapshot) batch.delete(doc.getReference());
            batch.delete(sessionsRef.document(sessionId));
            batch.commit().addOnSuccessListener(aVoid -> {
                if (sessionId.equals(currentSessionId)) startNewChat();
                Toast.makeText(getContext(), "Đã xóa đoạn chat", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void startNewChat() {
        currentSessionId = null;
        chatList.clear();
        chatAdapter.updateList(chatList);
        binding.toolbar.setTitle("Đoạn chat mới");
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        sendWelcomeMessage();
    }

    private void sendWelcomeMessage() {
        chatList.add(new ChatMessage("BookStore", "Xin chào! Tôi là trợ lý BookStore AI. Hôm nay tôi có thể giúp gì cho bạn?", System.currentTimeMillis()));
        chatAdapter.updateList(chatList);
    }

    private void loadSession(String sessionId, String title) {
        if (messageListener != null) messageListener.remove();
        currentSessionId = sessionId;
        binding.toolbar.setTitle(title);

        messageListener = sessionsRef.document(sessionId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !isAdded()) return;
                    chatList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setMessageId(doc.getId());
                            chatList.add(msg);
                        }
                    }
                    chatAdapter.updateList(chatList);
                    binding.rvChat.scrollToPosition(chatList.size() - 1);
                });
    }

    private void sendMessage() {
        String msgText = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msgText)) return;

        if (currentSessionId == null) {
            // Tạo session mới khi gửi tin đầu tiên
            DocumentReference newSession = sessionsRef.document();
            currentSessionId = newSession.getId();
            ChatSession session = new ChatSession(currentSessionId, msgText, System.currentTimeMillis());
            newSession.set(session);
            loadSession(currentSessionId, msgText);
        }

        ChatMessage userMsg = new ChatMessage(currentUserId, msgText, System.currentTimeMillis());
        sessionsRef.document(currentSessionId).collection("messages").add(userMsg);
        sessionsRef.document(currentSessionId).update("lastTimestamp", System.currentTimeMillis());
        
        binding.etMessage.setText("");
        getBotResponse(msgText);
    }

    private void getBotResponse(String userMessage) {
        String response = "Tôi đã nhận được yêu cầu của bạn về: " + userMessage + ". Bạn cần tôi tư vấn sâu hơn về đầu sách nào không?";
        
        binding.getRoot().postDelayed(() -> {
            if (currentSessionId != null && isAdded()) {
                ChatMessage botMsg = new ChatMessage("BookStore", response, System.currentTimeMillis());
                sessionsRef.document(currentSessionId).collection("messages").add(botMsg);
            }
        }, 1000);
    }

    private void showRecallDialog(ChatMessage message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Thu hồi")
                .setMessage("Bạn muốn thu hồi tin nhắn này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    if (currentSessionId != null && message.getMessageId() != null) {
                        sessionsRef.document(currentSessionId).collection("messages").document(message.getMessageId()).delete();
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        if (messageListener != null) messageListener.remove();
        if (sessionListener != null) sessionListener.remove();
        super.onDestroyView();
        binding = null;
    }
}
