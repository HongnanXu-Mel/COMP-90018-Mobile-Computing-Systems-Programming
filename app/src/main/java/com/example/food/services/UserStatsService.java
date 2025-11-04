package com.example.food.services;

/**
 * UserStatsService - Service for calculating and updating user statistics
 * 
 * This service:
 * - Calculates credibility and experience scores
 * - Updates user scores in Firestore when reviews/votes change
 * - Provides methods to retrieve user scores
 * - Uses ScoreCalculator for score computation logic
 * 
 * All methods are static for easy access throughout the app
 */

import android.util.Log;

import com.example.food.utils.ScoreCalculator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserStatsService {
    // Tag for logging
    private static final String TAG = "UserStatsService";
    // Firestore database instance
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Update all scores for a user (credibility and experience)
     * This is the main entry point for score updates
     * @param userId User ID to update scores for
     */
    public static void updateUserScores(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.w(TAG, "Cannot update scores: userId is null or empty");
            return;
        }

        Log.d(TAG, "Updating scores for user: " + userId);

        // Recalculate scores using ScoreCalculator and save to Firestore
        ScoreCalculator.calculateUserStats(userId, db, new ScoreCalculator.OnStatsCalculatedListener() {
            @Override
            public void onStatsCalculated(Map<String, Object> stats, double credibilityScore, double experienceScore) {
                saveScoresToFirestore(userId, stats, credibilityScore, experienceScore);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error calculating stats for user " + userId + ": " + error);
            }
        });
    }

    /**
     * Update user scores when their review changes
     * @param userId User who owns the review
     */
    public static void updateUserScoresOnReviewChange(String userId) {
        Log.d(TAG, "Review change detected for user: " + userId);
        updateUserScores(userId);
    }

    /**
     * Update review author's scores when their review receives a vote
     * Looks up the review to find the author, then updates their scores
     * @param reviewId ID of the review that received a vote
     */
    public static void updateUserScoresOnVoteChange(String reviewId) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            Log.w(TAG, "Cannot update scores: reviewId is null or empty");
            return;
        }

        Log.d(TAG, "Vote change detected for review: " + reviewId);

        // Find the review to get the author's user ID
        db.collection("reviews")
                .document(reviewId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userId = documentSnapshot.getString("userId");
                        if (userId != null) {
                            Log.d(TAG, "Updating scores for review author: " + userId);
                            updateUserScores(userId);
                        } else {
                            Log.w(TAG, "Review has no userId: " + reviewId);
                        }
                    } else {
                        Log.w(TAG, "Review not found: " + reviewId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching review for vote update: " + reviewId, e);
                });
    }

    /**
     * Save calculated scores to Firestore user document
     * @param userId User ID to update
     * @param stats Calculated statistics map
     * @param credibilityScore Calculated credibility score
     * @param experienceScore Calculated experience score
     */
    private static void saveScoresToFirestore(String userId, Map<String, Object> stats, 
                                            double credibilityScore, double experienceScore) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("credibilityScore", credibilityScore);
        updates.put("experienceScore", experienceScore);
        updates.put("stats", stats);
        updates.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, String.format("Saving scores for user %s: credibility=%.1f, experience=%.1f", 
                userId, credibilityScore, experienceScore));

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully updated scores for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating scores for user: " + userId, e);
                });
    }

    /**
     * Retrieve user scores from Firestore
     * @param userId User ID to get scores for
     * @param listener Callback to receive scores
     */
    public static void getUserScores(String userId, OnScoresRetrievedListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.w(TAG, "Cannot get scores: userId is null or empty");
            if (listener != null) {
                listener.onScoresRetrieved(0.0, 0.0);
            }
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double credibilityScore = 0.0;
                        double experienceScore = 0.0;

                        if (documentSnapshot.get("credibilityScore") != null) {
                            credibilityScore = documentSnapshot.getDouble("credibilityScore");
                        }
                        if (documentSnapshot.get("experienceScore") != null) {
                            experienceScore = documentSnapshot.getDouble("experienceScore");
                        }

                        Log.d(TAG, String.format("Retrieved scores for user %s: credibility=%.1f, experience=%.1f", 
                                userId, credibilityScore, experienceScore));

                        if (listener != null) {
                            listener.onScoresRetrieved(credibilityScore, experienceScore);
                        }
                    } else {
                        Log.w(TAG, "User document not found: " + userId);
                        if (listener != null) {
                            listener.onScoresRetrieved(0.0, 0.0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving scores for user: " + userId, e);
                    if (listener != null) {
                        listener.onScoresRetrieved(0.0, 0.0);
                    }
                });
    }

    /**
     * Callback interface for score retrieval
     */
    public interface OnScoresRetrievedListener {
        void onScoresRetrieved(double credibilityScore, double experienceScore);
    }
}
