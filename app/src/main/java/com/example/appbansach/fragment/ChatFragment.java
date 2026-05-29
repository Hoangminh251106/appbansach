package com.example.appbansach.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.ChatAdapter;
import com.example.appbansach.databinding.FragmentChatBinding;
import com.example.appbansach.model.ChatMessage;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatAdapter adapter;
    private final List<ChatMessage> chatList = new ArrayList<>();
    private CollectionReference messageRef;
    private String currentUserId;
    private ListenerRegistration messageListener;

    private GenerativeModelFutures model;
    // API Key của Google AI Studio
    private static final String API_KEY = "AIzaSyCe0Ut1KaLwx65mtDBbWj4F-422uLHGIqY";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initGemini();

        if (currentUserId != null) {
            messageRef = db.collection("chats").document(currentUserId).collection("messages");
            setupRecyclerView();
            listenForMessages();
            sendWelcomeMessageIfEmpty();
        }

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void initGemini() {
        try {
            GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
            configBuilder.temperature = 0.7f;
            GenerationConfig config = configBuilder.build();

            // Đảm bảo tên model chính xác. Thử dùng "gemini-1.5-flash"
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY, config);
            model = GenerativeModelFutures.from(gm);
        } catch (Exception e) {
            Log.e("GeminiInitError", "Lỗi khởi tạo Gemini: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        if (binding == null) return;
        adapter = new ChatAdapter(chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(adapter);
    }

    private void listenForMessages() {
        if (messageRef == null) return;
        messageListener = messageRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Lỗi: " + error.getMessage());
                        return;
                    }
                    if (value == null) return;
                    
                    if (isAdded() && binding != null) {
                        chatList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) chatList.add(msg);
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            if (!chatList.isEmpty()) {
                                binding.rvChat.scrollToPosition(chatList.size() - 1);
                            }
                        }
                    }
                });
    }

    private void sendWelcomeMessageIfEmpty() {
        if (messageRef == null) return;
        messageRef.limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isAdded() && binding != null && queryDocumentSnapshots != null && queryDocumentSnapshots.isEmpty()) {
                ChatMessage welcomeMsg = new ChatMessage("gemini", "Xin chào! Tôi là trợ lý ảo Gemini. Tôi có thể giúp gì cho bạn?", System.currentTimeMillis());
                messageRef.add(welcomeMsg);
            }
        });
    }

    private void sendMessage() {
        if (binding == null) return;
        String msgText = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msgText)) return;

        if (messageRef != null) {
            ChatMessage userMsg = new ChatMessage(currentUserId, msgText, System.currentTimeMillis());
            messageRef.add(userMsg);
            binding.etMessage.setText("");
            getGeminiResponse(msgText);
        }
    }

    private void getGeminiResponse(String userMessage) {
        if (model == null || getContext() == null) return;

        Content content = new Content.Builder()
                .addText(userMessage)
                .build();

        try {
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());
            
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    if (isAdded() && binding != null && result != null) {
                        String aiResponse = result.getText();
                        if (aiResponse != null && messageRef != null) {
                            ChatMessage aiMsg = new ChatMessage("gemini", aiResponse, System.currentTimeMillis());
                            messageRef.add(aiMsg);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Log.e("GeminiError", "Lỗi AI: " + t.getMessage());
                }
            }, mainExecutor);
        } catch (Exception e) {
            Log.e("GeminiError", "Lỗi: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messageListener != null) {
            messageListener.remove();
        }
        binding = null; // Chốt chặn quan trọng để tránh crash NullPointerException
    }
}
