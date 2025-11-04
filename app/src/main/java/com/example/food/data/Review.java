package com.example.food.data;

/**
 * Review - Data model representing a restaurant review
 * 
 * This class stores all review information:
 * - User and restaurant identifiers
 * - Review content (caption, description, rating)
 * - Images and image metadata
 * - Accuracy voting data
 * - Comments from other users
 * - Timestamps
 * 
 * Note: userName and restaurantName are excluded from Firestore storage
 * and fetched dynamically when needed
 */

import com.google.firebase.firestore.Exclude;
import java.util.Date;
import java.util.List;

public class Review {
    // Basic identifiers
    private String id; // Review document ID
    private String userId; // Author's user ID
    @Exclude
    private String userName; // Author name (not stored - fetched dynamically)
    private String restaurantId; // Restaurant document ID
    @Exclude
    private String restaurantName; // Restaurant name (not stored - fetched dynamically)
    
    // Review content
    private String description; // Detailed review text
    private String caption; // Short title/summary
    private float rating; // Star rating (1-5)
    
    // Accuracy metrics
    private int accuracy; // Numeric accuracy score
    private double accuracyPercent; // Percentage accuracy from votes
    
    // Images
    private List<String> imageUrls; // List of image URLs
    private String firstImageType; // First image orientation (SQUARE/PORTRAIT/HORIZONTAL)
    
    // Timestamps
    private Date createdAt; // When review was created
    private Date updatedAt; // Last update time
    
    // Engagement
    private int helpfulCount; // Count of helpful votes
    private java.util.Map<String, Boolean> votes; // User ID -> accurate/inaccurate vote
    private List<Comment> comments; // List of comments on this review

    /**
     * Default constructor required for Firebase Firestore
     */
    public Review() {
    }

    /**
     * Full constructor with all fields
     */
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
        this.accuracyPercent = 100.0; // New reviews start at 100% accuracy
        this.firstImageType = "SQUARE"; // Default to square image type
        this.votes = new java.util.HashMap<>(); // Initialize empty votes map
        this.comments = new java.util.ArrayList<>(); // Initialize empty comments list
    }

    // Getters and Setters for all fields
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

    public String getFirstImageType() { return firstImageType; }
    public void setFirstImageType(String firstImageType) { this.firstImageType = firstImageType; }

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

    /**
     * Check if the first image is portrait orientation
     * @return true if portrait, false otherwise
     */
    public boolean isFirstImagePortrait() {
        return "PORTRAIT".equals(firstImageType);
    }

    /**
     * Check if the first image is square
     * @return true if square, false otherwise
     */
    public boolean isFirstImageSquare() {
        return "SQUARE".equals(firstImageType);
    }

    /**
     * Check if the first image is horizontal orientation
     * @return true if horizontal, false otherwise
     */
    public boolean isFirstImageHorizontal() {
        return "HORIZONTAL".equals(firstImageType);
    }
}