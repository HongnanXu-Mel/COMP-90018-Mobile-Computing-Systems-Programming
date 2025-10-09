package com.example.food.data;

import java.util.Date;
import java.util.List;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private String restaurantId;
    private String restaurantName;
    private String description;
    private String caption;
    private float rating;
    private int accuracy;
    private double accuracyPercent;
    private List<String> imageUrls;
    private Date createdAt;
    private Date updatedAt;
    private int helpfulCount;
    private java.util.Map<String, Boolean> votes;
    private List<Comment> comments;

    public Review() {
        // Default constructor required for Firestore
    }

    public Review(String id, String userId, String userName, String restaurantId,
                 String restaurantName, String caption, String description, float rating,
                 int accuracy, List<String> imageUrls, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.caption = caption;
        this.description = description;
        this.rating = rating;
        this.accuracy = accuracy;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.helpfulCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getAccuracy() { return accuracy; }
    public void setAccuracy(int accuracy) { this.accuracy = accuracy; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public double getAccuracyPercent() { return accuracyPercent; }
    public void setAccuracyPercent(double accuracyPercent) { this.accuracyPercent = accuracyPercent; }

    public java.util.Map<String, Boolean> getVotes() { return votes; }
    public void setVotes(java.util.Map<String, Boolean> votes) { this.votes = votes; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}