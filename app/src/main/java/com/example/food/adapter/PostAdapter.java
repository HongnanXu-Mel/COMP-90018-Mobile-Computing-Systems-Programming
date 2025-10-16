package com.example.food.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.food.R;
import com.example.food.model.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = "PostAdapter";

    private Context context;
    private List<Post> postList;
    private OnPostClickListener onPostClickListener;
    private SimpleDateFormat dateFormat;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.onPostClickListener = listener;
    }

    public void updatePosts(List<Post> newPosts) {
        this.postList.clear();
        this.postList.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void addPosts(List<Post> newPosts) {
        int startPosition = postList.size();
        this.postList.addAll(newPosts);
        notifyItemRangeInserted(startPosition, newPosts.size());
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCoverImage;
        private TextView tvPostTitle;
        private TextView tvUsername;
        private TextView tvLikesCount;
        private TextView tvCommentsCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCoverImage = itemView.findViewById(R.id.iv_cover_image);
            tvPostTitle = itemView.findViewById(R.id.tv_post_title);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvLikesCount = itemView.findViewById(R.id.tv_likes_count);
            tvCommentsCount = itemView.findViewById(R.id.tv_comments_count);

            itemView.setOnClickListener(v -> {
                if (onPostClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onPostClickListener.onPostClick(postList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Post post) {
            tvPostTitle.setText(post.getTitle() != null ? post.getTitle() : "Untitled Post");
            tvUsername.setText(post.getUsername() != null ? post.getUsername() : "Anonymous");
            tvLikesCount.setText(String.valueOf(post.getLikesCount()));
            tvCommentsCount.setText(String.valueOf(post.getCommentsCount()));
            
            // Load cover image with Glide
            loadCoverImage(post.getCoverImageUrl());
        }
        
        private void loadCoverImage(String imageUrl) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Log.d(TAG, "Loading image: " + imageUrl);
                
                Glide.with(context)
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.image_placeholder)
                                .error(R.drawable.image_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new RoundedCorners(16)))
                        .into(ivCoverImage);
            } else {
                Log.d(TAG, "No image URL provided, showing placeholder");
                // Show placeholder when no image URL
                ivCoverImage.setImageResource(R.drawable.image_placeholder);
            }
        }
    }

    // Method to filter posts based on search query
    public void filterPosts(List<Post> originalPosts, String query) {
        List<Post> filteredPosts = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            filteredPosts.addAll(originalPosts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Post post : originalPosts) {
                if ((post.getTitle() != null && post.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                    (post.getContent() != null && post.getContent().toLowerCase().contains(lowerCaseQuery)) ||
                    (post.getUsername() != null && post.getUsername().toLowerCase().contains(lowerCaseQuery))) {
                    filteredPosts.add(post);
                }
            }
        }
        
        updatePosts(filteredPosts);
    }
}
