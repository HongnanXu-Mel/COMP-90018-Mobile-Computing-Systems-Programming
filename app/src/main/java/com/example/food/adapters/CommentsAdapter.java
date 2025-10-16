package com.example.food.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.Comment;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
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
        if (comment != null) {
            holder.bind(comment);
        }
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvCommentDate;
        private TextView tvCommentText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_comment_username);
            tvCommentDate = itemView.findViewById(R.id.tv_comment_time);
            tvCommentText = itemView.findViewById(R.id.tv_comment_content);
        }

        public void bind(Comment comment) {
            if (tvUserName != null) {
                tvUserName.setText(comment.getUserName());
            }
            if (tvCommentDate != null) {
                tvCommentDate.setText(comment.getFormattedDate());
            }
            if (tvCommentText != null) {
                tvCommentText.setText(comment.getText());
            }
        }
    }
}