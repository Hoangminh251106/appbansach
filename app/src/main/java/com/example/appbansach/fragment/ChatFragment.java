package com.example.appbansach.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.ChatAdapter;
import com.example.appbansach.databinding.FragmentChatBinding;
import com.example.appbansach.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList = new ArrayList<>();
    private DatabaseReference chatRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        currentUserId = FirebaseAuth.getInstance().getUid();
        
        chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(currentUserId);

        setupRecyclerView();
        listenForMessages();
        sendWelcomeMessageIfEmpty();

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

    private void listenForMessages() {
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    chatList.add(msg);
                    adapter.notifyItemInserted(chatList.size() - 1);
                    binding.rvChat.scrollToPosition(chatList.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendWelcomeMessageIfEmpty() {
        chatRef.get().addOnSuccessListener(dataSnapshot -> {
            if (!dataSnapshot.exists()) {
                ChatMessage welcome = new ChatMessage("admin", "Chào mừng bạn đến với READ & CHILL! Tôi là Chatbot hỗ trợ, bạn cần giúp gì không?", System.currentTimeMillis());
                chatRef.push().setValue(welcome);
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage message = new ChatMessage(currentUserId, text, System.currentTimeMillis());
        chatRef.push().setValue(message).addOnSuccessListener(v -> {
            binding.etMessage.setText("");
            // Tạo phản hồi tự động sau 1.5 giây
            new Handler(Looper.getMainLooper()).postDelayed(() -> handleBotResponse(text), 1500);
        });
    }

    private void handleBotResponse(String userText) {
        if (!isAdded()) return;
        
        String response;
        String lowerText = userText.toLowerCase();
        
        if (lowerText.contains("chào") || lowerText.contains("hello") || lowerText.contains("hi")) {
            response = "Xin chào! Chúc bạn một ngày tốt lành. Tôi có thể giúp gì cho bạn về sách không?";
        } else if (lowerText.contains("giá") || lowerText.contains("tiền") || lowerText.contains("bao nhiêu")) {
            response = "Giá mỗi cuốn sách đều được hiển thị rõ trong phần chi tiết. Bạn hãy nhấn vào ảnh sách để xem nhé!";
        } else if (lowerText.contains("ship") || lowerText.contains("giao hàng") || lowerText.contains("vận chuyển")) {
            response = "Chúng tôi giao hàng toàn quốc. Miễn phí vận chuyển cho đơn hàng từ 300.000đ!";
        } else if (lowerText.contains("khuyến mãi") || lowerText.contains("giảm giá") || lowerText.contains("mã")) {
            response = "Hiện tại app đang có mã 'HELLO' giảm 10% cho đơn hàng đầu tiên đó!";
        } else if (lowerText.contains("địa chỉ") || lowerText.contains("ở đâu") || lowerText.contains("cửa hàng")) {
            response = "Chúng tôi là hiệu sách trực tuyến, kho hàng chính đặt tại TP. Hồ Chí Minh và Hà Nội.";
        } else if (lowerText.contains("thanh toán")) {
            response = "Bạn có thể chọn thanh toán khi nhận hàng (COD) hoặc qua ví điện tử MoMo.";
        } else {
            response = "Cảm ơn bạn đã nhắn tin! Yêu cầu của bạn đã được chuyển đến nhân viên tư vấn, chúng tôi sẽ sớm phản hồi.";
        }

        ChatMessage botMessage = new ChatMessage("admin", response, System.currentTimeMillis());
        chatRef.push().setValue(botMessage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
