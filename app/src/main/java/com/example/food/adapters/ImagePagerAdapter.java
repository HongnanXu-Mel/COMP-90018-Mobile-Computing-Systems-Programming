package com.example.food.adapters;

/**
 * ImagePagerAdapter - RecyclerView adapter for image carousel/gallery
 * 
 * This adapter:
 * - Displays a horizontal scrolling list of images
 * - Loads images from URLs using Glide
 * - Shows placeholders for missing images
 * - Used in ViewPager2 for image galleries in review details
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.R;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    // Data
    private Context context; // Context for Glide image loading
    private List<String> images; // List of image URLs

    /**
     * Constructor
     */
    public ImagePagerAdapter(Context context) {
        this.context = context;
        this.images = new ArrayList<>();
    }

    /**
     * Update image list and refresh display
     */
    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_pager, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = images.get(position);
        holder.bind(imageUrl);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * ViewHolder for image items
     */
    class ImageViewHolder extends RecyclerView.ViewHolder {
        // UI component
        private ImageView imageView; // The image view

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        /**
         * Bind image URL to ImageView using Glide
         */
        public void bind(String imageUrl) {
            // Handle placeholder special case
            if ("placeholder".equals(imageUrl)) {
                imageView.setImageResource(R.drawable.ic_restaurant);
            } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                // Load actual image from URL using Glide
                Glide.with(context)
                        .load(imageUrl)
                        .fitCenter()
                        .placeholder(R.drawable.ic_restaurant)
                        .error(R.drawable.ic_restaurant)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_restaurant);
            }
        }
    }
}

