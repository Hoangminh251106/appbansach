package com.example.appbansach.adapter;

import android.content.Context;
import android.view.LayoutInflater;
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
    private Context context;

    public NotificationAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<NotificationModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
