package com.example.food.data;

import java.util.Date;

public class ActivityItem {
    public enum ActivityType {
        VOTE, COMMENT
    }
    
    private String id;
    private ActivityType type;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private String reviewId;
    private String reviewCaption;
    private String restaurantName;
    private Date timestamp;
    private Boolean voteType; // true for accurate, false for inaccurate, null for comments
    private String commentText;
    private String reviewFirstImageUrl;

    public ActivityItem() {
        // Default constructor required for Firestore
    }

    public ActivityItem(ActivityType type, String userId, String userName, String userAvatarUrl, 
                       String reviewId, String reviewCaption, String restaurantName, Date timestamp) {
        this.type = type;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.reviewId = reviewId;
        this.reviewCaption = reviewCaption;
        this.restaurantName = restaurantName;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getReviewCaption() { return reviewCaption; }
    public void setReviewCaption(String reviewCaption) { this.reviewCaption = reviewCaption; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public Boolean getVoteType() { return voteType; }
    public void setVoteType(Boolean voteType) { this.voteType = voteType; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getReviewFirstImageUrl() { return reviewFirstImageUrl; }
    public void setReviewFirstImageUrl(String reviewFirstImageUrl) { this.reviewFirstImageUrl = reviewFirstImageUrl; }
}

