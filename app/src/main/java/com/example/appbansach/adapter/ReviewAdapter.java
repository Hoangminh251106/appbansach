package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbansach.databinding.ItemReviewBinding;
import com.example.appbansach.model.ReviewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<ReviewModel> list;
    private List<ReviewModel> listFull;
    private OnReviewListener listener;

    public interface OnReviewListener {
        void onDelete(ReviewModel review);
        void onReply(ReviewModel review);
    }

    public ReviewAdapter(List<ReviewModel> list, OnReviewListener listener) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.listener = listener;
    }

    public ReviewAdapter(List<ReviewModel> list) {
        this(list, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel item = list.get(position);
        holder.binding.tvReviewUser.setText(item.getUserName());
        
        if (item.getBookTitle() != null && !item.getBookTitle().isEmpty()) {
            holder.binding.tvReviewBook.setText("Sách: " + item.getBookTitle());
            holder.binding.tvReviewBook.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvReviewBook.setVisibility(View.GONE);
        }

        holder.binding.tvReviewContent.setText(item.getContent());
        holder.binding.ratingBar.setRating(item.getRating());

        if (item.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.binding.tvReviewDate.setText(sdf.format(item.getCreatedAt().toDate()));
        }

        // Admin Reply UI
        if (item.getAdminReply() != null && !item.getAdminReply().isEmpty()) {
            holder.binding.layoutAdminReply.setVisibility(View.VISIBLE);
            holder.binding.tvAdminReplyContent.setText(item.getAdminReply());
            holder.binding.btnReplyReview.setText("Sửa phản hồi");
        } else {
            holder.binding.layoutAdminReply.setVisibility(View.GONE);
            holder.binding.btnReplyReview.setText("Phản hồi");
        }

        if (listener != null) {
            holder.binding.btnDeleteReview.setVisibility(View.VISIBLE);
            holder.binding.btnReplyReview.setVisibility(View.VISIBLE);
            
            holder.binding.btnDeleteReview.setOnClickListener(v -> listener.onDelete(item));
            holder.binding.btnReplyReview.setOnClickListener(v -> listener.onReply(item));
        } else {
            holder.binding.btnDeleteReview.setVisibility(View.GONE);
            holder.binding.btnReplyReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filter(float stars) {
        list = new ArrayList<>();
        if (stars == 0) {
            list.addAll(listFull);
        } else {
            for (ReviewModel r : listFull) {
                if (r.getRating() == stars) {
                    list.add(r);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<ReviewModel> newList) {
        this.list = newList;
        this.listFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemReviewBinding binding;
        ViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
