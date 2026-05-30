package com.example.appbansach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
        String imageUrl = banner.getImageUrl();

        // Xử lý load ảnh: hỗ trợ cả URL internet và ảnh nội bộ trong drawable
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            // Xử lý nếu imageUrl là tên file drawable (không có phần mở rộng)
            String resourceName = imageUrl;
            if (resourceName.contains(".")) {
                resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
            }
            
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    resourceName, "drawable", holder.itemView.getContext().getPackageName());
            
            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .fitCenter() // Chuyển sang fitCenter để hiện đầy đủ ảnh, không bị cắt
                        .into(holder.binding.ivBanner);
            } else {
                // Fallback nếu không tìm thấy resource
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .fitCenter()
                        .into(holder.binding.ivBanner);
            }
        } else {
            // Load từ URL internet
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .fitCenter() // Chuyển sang fitCenter để hiện đầy đủ ảnh, không bị cắt
                    .into(holder.binding.ivBanner);
        }
        
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
