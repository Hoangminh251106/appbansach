package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbansach.databinding.ItemChatSessionBinding;
import com.example.appbansach.model.ChatSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.ViewHolder> {
    private List<ChatSession> sessionList;
    private List<ChatSession> sessionListFull;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
        void onSessionDelete(ChatSession session);
    }

    public ChatSessionAdapter(List<ChatSession> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.sessionListFull = new ArrayList<>(sessionList);
        this.listener = listener;
    }

    public void updateList(List<ChatSession> newList) {
        this.sessionList = new ArrayList<>(newList);
        this.sessionListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        List<ChatSession> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(sessionListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (ChatSession item : sessionListFull) {
                if (item.getTitle().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        this.sessionList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemChatSessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSession session = sessionList.get(position);
        holder.binding.tvSessionTitle.setText(session.getTitle());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        holder.binding.tvSessionTime.setText(sdf.format(new Date(session.getLastTimestamp())));

        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
        holder.binding.btnDeleteSession.setOnClickListener(v -> listener.onSessionDelete(session));
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemChatSessionBinding binding;
        ViewHolder(ItemChatSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
