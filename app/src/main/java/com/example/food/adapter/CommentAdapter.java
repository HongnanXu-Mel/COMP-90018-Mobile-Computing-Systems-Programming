package com.example.food.adapter;

/**
 * CommentAdapter - RecyclerView adapter for displaying comment list
 * 
 * This adapter:
 * - Binds Comment data to comment item views
 * - Displays username, timestamp, and comment text
 * - Formats timestamp as relative time (e.g., "2h ago")
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    
    // List of comments to display
    private List<Comment> comments;

    /**
     * Constructor - initializes with empty list
     */
    public CommentAdapter() {
        this.comments = new ArrayList<>();
    }

    /**
     * Update the comment list and refresh the view
     */
    public void updateComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder for comment items
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        // UI components
        private TextView tvUsername; // Comment author name
        private TextView tvTime; // Comment timestamp
        private TextView tvContent; // Comment text

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_comment_username);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
        }

        /**
         * Bind comment data to view elements
         */
        public void bind(Comment comment) {
            tvUsername.setText(comment.getUserName());
            tvContent.setText(comment.getText());

            // Format timestamp to relative time
            if (comment.getCreatedAt() != null) {
                tvTime.setText(getTimeAgo(comment.getCreatedAt()));
            }
        }

        /**
         * Convert timestamp to relative time string (e.g., "2h ago")
         */
        private String getTimeAgo(Date date) {
            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + "d ago";
            } else if (hours > 0) {
                return hours + "h ago";
            } else if (minutes > 0) {
                return minutes + "m ago";
            } else {
                return "Just now";
            }
        }
    }
}

