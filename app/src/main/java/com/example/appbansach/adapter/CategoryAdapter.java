package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.ItemCategoryHomeBinding;
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
        ItemCategoryHomeBinding binding = ItemCategoryHomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());

        // 1. Xác định icon cục bộ dựa trên tên
        int iconRes = R.drawable.ic_categories; // Mặc định
        String name = category.getName().toLowerCase();
        
        if (name.contains("kỹ năng")) {
            iconRes = R.drawable.ic_cat_skills;
        } else if (name.contains("kinh tế") || name.contains("kinh doanh")) {
            iconRes = R.drawable.ic_cat_business;
        } else if (name.contains("thiếu nhi")) {
            iconRes = R.drawable.ic_cat_children;
        } else if (name.contains("văn học") || name.contains("truyện")) {
            iconRes = R.drawable.ic_category_literature;
        }

        // 2. Luôn set icon cục bộ trước
        holder.binding.ivCategoryIcon.setImageResource(iconRes);
        holder.binding.ivCategoryIcon.setColorFilter(null);

        // 3. Chỉ dùng Glide nếu có URL hợp lệ và KHÔNG phải là danh mục đã có icon fix cứng
        // Nếu bạn muốn ưu tiên icon đẹp từ file xml trong máy, hãy kiểm tra iconRes
        if (iconRes == R.drawable.ic_categories) { 
            if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                     .load(category.getIconUrl())
                     .into(holder.binding.ivCategoryIcon);
            }
        }
        
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public ItemCategoryHomeBinding binding;
        public CategoryViewHolder(ItemCategoryHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
