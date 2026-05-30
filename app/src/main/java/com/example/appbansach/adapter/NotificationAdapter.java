package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbansach.databinding.ItemNotificationBinding;
import com.example.appbansach.model.NotificationModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> list;
    private OnNotificationListener listener;
    private boolean isAdmin;

    public interface OnNotificationListener {
        void onNotificationClick(NotificationModel notification);
        void onPushClick(NotificationModel notification); // Thêm sự kiện đẩy tin
    }

    public NotificationAdapter(List<NotificationModel> list, boolean isAdmin, OnNotificationListener listener) {
        this.list = list;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel item = list.get(position);
        holder.binding.tvNotiTitle.setText(item.getTitle());
        holder.binding.tvNotiContent.setText(item.getContent());

        if (item.getSentAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.binding.tvNotiDate.setText(sdf.format(item.getSentAt().toDate()));
        }

        // Chỉ hiển thị nút đẩy tin nếu là admin
        holder.binding.btnPush.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        holder.binding.btnPush.setOnClickListener(v -> {
            if (listener != null) listener.onPushClick(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(item);
        });
        
        // Hiển thị trạng thái chưa đọc (tùy chọn: đổi màu nền hoặc thêm dot)
        if (!item.isRead()) {
            holder.binding.getRoot().setAlpha(1.0f);
        } else {
            holder.binding.getRoot().setAlpha(0.6f);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
