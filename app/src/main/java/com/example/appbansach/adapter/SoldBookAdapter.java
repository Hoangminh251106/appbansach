package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.databinding.ItemSoldBookBinding;

import java.util.List;
import java.util.Map;

public class SoldBookAdapter extends RecyclerView.Adapter<SoldBookAdapter.SoldBookViewHolder> {
    private List<Map.Entry<String, Integer>> soldBooksList;

    public SoldBookAdapter(List<Map.Entry<String, Integer>> soldBooksList) {
        this.soldBooksList = soldBooksList;
    }

    @NonNull
    @Override
    public SoldBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSoldBookBinding binding = ItemSoldBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SoldBookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SoldBookViewHolder holder, int position) {
        Map.Entry<String, Integer> entry = soldBooksList.get(position);
        holder.binding.tvSoldBookTitle.setText(entry.getKey());
        holder.binding.tvSoldQuantity.setText("Số lượng: " + entry.getValue());
        holder.binding.tvRank.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return soldBooksList.size();
    }

    public static class SoldBookViewHolder extends RecyclerView.ViewHolder {
        ItemSoldBookBinding binding;
        public SoldBookViewHolder(ItemSoldBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
