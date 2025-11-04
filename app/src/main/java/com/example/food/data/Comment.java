package com.example.food.data;

/**
 * Comment - Data model representing a user comment on a review
 * 
 * This class stores comment data including:
 * - Comment ID and text content
 * - User information (ID and username)
 * - Creation timestamp
 * - Likes/reactions from other users
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Comment {
    // Comment data fields
    private String id; // Unique comment ID
    private String userId; // ID of user who wrote the comment
    private String userName; // Display name of comment author
    private String text; // Comment text content
    private Date createdAt; // When comment was created
    private Map<String, Boolean> likes; // Map of user IDs who liked this comment

    /**
     * Default constructor required for Firebase Firestore
     */
    public Comment() {
    }

    /**
     * Constructor with all fields
     */
    public Comment(String id, String userId, String userName, String text, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.createdAt = createdAt;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Get formatted date string for display (e.g., "Jan 15, 2024")
     */
    public String getFormattedDate() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(createdAt);
    }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }
}