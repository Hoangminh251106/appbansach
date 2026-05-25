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
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatList;
    private String currentUserId = FirebaseAuth.getInstance().getUid();

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
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
        String time = message.getTimestamp() != null ? sdf.format(message.getTimestamp().toDate()) : "";

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).binding.tvMessage.setText(message.getMessage());
            ((SentViewHolder) holder).binding.tvTime.setText(time);
        } else {
            ((ReceivedViewHolder) holder).binding.tvMessage.setText(message.getMessage());
            ((ReceivedViewHolder) holder).binding.tvTime.setText(time);
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
