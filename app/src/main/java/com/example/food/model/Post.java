package com.example.food.model;

import com.google.firebase.Timestamp;

public class Post {
    private String postId;
    private String title;
    private String content;
    private String username;
    private String userId;
    private String coverImageUrl;
    private Timestamp timestamp;
    private int likesCount;
    private int commentsCount;
    private String category;

    // Default constructor required for Firebase
    public Post() {
    }

    // Constructor with basic fields
    public Post(String title, String content, String username, String userId) {
        this.title = title;
        this.content = content;
        this.username = username;
        this.userId = userId;
        this.timestamp = Timestamp.now();
        this.likesCount = 0;
        this.commentsCount = 0;
        this.category = "Food";
    }

    // Full constructor
    public Post(String postId, String title, String content, String username, String userId, 
                Timestamp timestamp, int likesCount, int commentsCount, String category) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.username = username;
        this.userId = userId;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.category = category;
    }

    // Getters and Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", userId='" + userId + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", timestamp=" + timestamp +
                ", likesCount=" + likesCount +
                ", commentsCount=" + commentsCount +
                ", category='" + category + '\'' +
                '}';
    }
}
