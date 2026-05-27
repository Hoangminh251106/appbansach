package com.example.appbansach.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.admin.UserDetailActivity;
import com.example.appbansach.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private List<User> userListFull;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());

        String status = user.getStatus() != null ? user.getStatus() : "active";
        if (status.equals("active")) {
            holder.tvStatus.setText("Hoạt động");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            holder.tvStatus.setText("Bị khóa");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_locked);
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_myplaces).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("userId", user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            text = text.toLowerCase();
            for (User item : userListFull) {
                if (item.getFullName().toLowerCase().contains(text) ||
                        item.getEmail().toLowerCase().contains(text) ||
                        (item.getPhone() != null && item.getPhone().contains(text))) {
                    userList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        this.userListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvEmail, tvPhone, tvStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
        }
    }
}
