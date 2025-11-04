package com.example.food.data;

/**
 * UserProfile - Data model representing a user's profile information
 * 
 * This class stores:
 * - Basic user info (UID, name, email, bio)
 * - Profile picture URL
 * - Calculated scores (credibility, experience)
 * - User statistics
 * - Timestamps for creation and updates
 */

import java.util.Map;

public class UserProfile {
    // Basic user information
    private String uid; // Firebase Auth UID
    private String name; // User's display name
    private String displayName; // Alternative display name field
    private String email; // User's email address
    private String bio; // User biography/description
    private String avatarUrl; // Profile picture URL
    
    // Timestamps
    private long createdAt; // Account creation timestamp
    private long updatedAt; // Last profile update timestamp
    
    // Calculated scores
    private double credibilityScore; // User's credibility score (0-100+)
    private double experienceScore; // User's experience score (0-100+)
    
    // Statistics
    private Map<String, Object> stats; // Detailed user statistics

    /**
     * Default constructor required for Firebase Firestore
     */
    public UserProfile() {
    }

    /**
     * Constructor with main fields
     * Sets timestamps to current time
     */
    public UserProfile(String uid, String name, String displayName, String email, String bio) {
        this.uid = uid;
        this.name = name;
        this.displayName = displayName;
        this.email = email;
        this.bio = bio;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getCredibilityScore() {
        return credibilityScore;
    }

    public void setCredibilityScore(double credibilityScore) {
        this.credibilityScore = credibilityScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", credibilityScore=" + credibilityScore +
                ", experienceScore=" + experienceScore +
                '}';
    }
}
