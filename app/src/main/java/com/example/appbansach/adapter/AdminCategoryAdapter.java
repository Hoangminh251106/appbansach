package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
