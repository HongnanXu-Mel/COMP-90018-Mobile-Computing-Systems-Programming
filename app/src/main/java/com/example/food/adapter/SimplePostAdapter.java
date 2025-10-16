package com.example.food.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.example.food.model.Post;

import java.util.ArrayList;
import java.util.List;

public class SimplePostAdapter extends RecyclerView.Adapter<SimplePostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public SimplePostAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void updatePosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_simple, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumbnail;
        private TextView tvTitle;
        private TextView tvUsername;
        private TextView tvLikes;
        private TextView tvComments;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_post_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvUsername = itemView.findViewById(R.id.tv_post_username);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvComments = itemView.findViewById(R.id.tv_comments);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPostClick(posts.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Post post) {
            tvTitle.setText(post.getTitle());
            tvUsername.setText(post.getUsername());
            tvLikes.setText(String.valueOf(post.getLikesCount()));
            tvComments.setText(String.valueOf(post.getCommentsCount()));

            // Load thumbnail image
            if (post.getCoverImageUrl() != null && !post.getCoverImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(post.getCoverImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(R.drawable.image_placeholder);
            }
        }
    }
}

