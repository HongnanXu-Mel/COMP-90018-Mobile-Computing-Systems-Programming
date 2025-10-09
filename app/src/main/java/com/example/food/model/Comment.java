package com.example.food.model;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String content;
    private Timestamp timestamp;

    // Default constructor required for Firebase
    public Comment() {
    }

    public Comment(String postId, String userId, String username, String content) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", postId='" + postId + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

