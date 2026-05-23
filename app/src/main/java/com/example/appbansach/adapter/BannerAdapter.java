package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.ItemBannerBinding;
import com.example.appbansach.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Banner> bannerList;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }

    public BannerAdapter(List<Banner> bannerList, OnBannerClickListener listener) {
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(banner.getImageUrl())
                .into(holder.binding.ivBanner);
        
        holder.itemView.setOnClickListener(v -> listener.onBannerClick(banner));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ItemBannerBinding binding;
        public BannerViewHolder(ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
