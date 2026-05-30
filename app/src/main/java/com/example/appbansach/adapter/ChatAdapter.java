package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemChatLeftBinding;
import com.example.appbansach.databinding.ItemChatRightBinding;
import com.example.appbansach.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatList;
    private List<ChatMessage> chatListFull; // Danh sách gốc để tìm kiếm
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    private OnMessageLongClickListener longClickListener;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage message);
    }

    public ChatAdapter(List<ChatMessage> chatList, OnMessageLongClickListener listener) {
        this.chatList = chatList;
        this.chatListFull = new ArrayList<>(chatList);
        this.longClickListener = listener;
    }

    // Cập nhật danh sách khi có tin nhắn mới từ Firebase
    public void updateList(List<ChatMessage> newList) {
        this.chatList = new ArrayList<>(newList);
        this.chatListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // Logic tìm kiếm tin nhắn
    public void filter(String query) {
        List<ChatMessage> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(chatListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (ChatMessage item : chatListFull) {
                if (item.getMessage().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        this.chatList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatList.get(position).getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            ItemChatRightBinding binding = ItemChatRightBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemChatLeftBinding binding = ItemChatLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = message.getTimestamp() != 0 ? sdf.format(new Date(message.getTimestamp())) : "";

        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.binding.tvMessage.setText(message.getMessage());
            sentHolder.binding.tvTime.setText(time);
            sentHolder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onMessageLongClick(message);
                return true;
            });
        } else {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.binding.tvMessage.setText(message.getMessage());
            receivedHolder.binding.tvTime.setText(time);
            receivedHolder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onMessageLongClick(message);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemChatRightBinding binding;
        SentViewHolder(ItemChatRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ItemChatLeftBinding binding;
        ReceivedViewHolder(ItemChatLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
