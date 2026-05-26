package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ItemCategoryBinding;
import com.example.appbansach.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());

        // UI/UX: Gán icon dựa trên tên danh mục để tránh nhàm chán
        int iconRes = android.R.drawable.ic_menu_gallery; // Mặc định
        String name = category.getName().toLowerCase();
        
        if (name.contains("kỹ năng")) {
            iconRes = android.R.drawable.ic_menu_info_details; // Hình bộ não/thông tin
        } else if (name.contains("kinh tế")) {
            iconRes = android.R.drawable.ic_menu_sort_by_size; // Hình biểu đồ/sắp xếp
        } else if (name.contains("thiếu nhi")) {
            iconRes = android.R.drawable.btn_star_big_on; // Hình gấu bông/ngôi sao
        } else if (name.contains("văn học")) {
            iconRes = android.R.drawable.ic_menu_edit; // Hình bút/văn học
        }

        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(category.getIconUrl()).into(holder.binding.ivCategory);
        } else {
            holder.binding.ivCategory.setImageResource(iconRes);
            holder.binding.ivCategory.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.sage_primary_dark));
        }
        
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public ItemCategoryBinding binding;
        public CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
