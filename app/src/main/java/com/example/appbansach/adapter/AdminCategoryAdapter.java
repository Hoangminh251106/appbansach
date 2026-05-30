package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ItemAdminCategoryBinding;
import com.example.appbansach.model.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryViewHolder> {
    private List<Category> categoryList;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onDelete(Category category);
    }

    public AdminCategoryAdapter(List<Category> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminCategoryBinding binding = ItemAdminCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvAdminCatName.setText(category.getName());
        
        // Cập nhật Icon chuẩn cho Admin dựa trên thể loại
        int iconRes = R.drawable.ic_categories; 
        String name = category.getName().toLowerCase();
        
        if (name.contains("kỹ năng") || name.contains("tâm lý")) {
            iconRes = R.drawable.ic_cat_skills;
        } else if (name.contains("kinh tế") || name.contains("kinh doanh") || name.contains("tài chính")) {
            iconRes = R.drawable.ic_cat_business;
        } else if (name.contains("thiếu nhi") || name.contains("trẻ em")) {
            iconRes = R.drawable.ic_cat_children;
        } else if (name.contains("văn học") || name.contains("tiểu thuyết") || name.contains("truyện")) {
            iconRes = R.drawable.ic_category_literature;
        }

        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(category.getIconUrl()).into(holder.binding.ivAdminCatIcon);
        } else {
            holder.binding.ivAdminCatIcon.setImageResource(iconRes);
            holder.binding.ivAdminCatIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.sage_primary_dark));
        }

        holder.binding.btnDeleteCat.setOnClickListener(v -> listener.onDelete(category));
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class AdminCategoryViewHolder extends RecyclerView.ViewHolder {
        ItemAdminCategoryBinding binding;
        public AdminCategoryViewHolder(ItemAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
