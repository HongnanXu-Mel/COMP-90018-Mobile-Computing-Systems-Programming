package com.example.food.services;

import android.util.Log;

import com.example.food.utils.ScoreCalculator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserStatsService {
    private static final String TAG = "UserStatsService";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void updateUserScores(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.w(TAG, "Cannot update scores: userId is null or empty");
            return;
        }

        Log.d(TAG, "Updating scores for user: " + userId);

        // Recalculate scores and save to Firestore
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

    public static void updateUserScoresOnReviewChange(String userId) {
        Log.d(TAG, "Review change detected for user: " + userId);
        updateUserScores(userId);
    }

    public static void updateUserScoresOnVoteChange(String reviewId) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            Log.w(TAG, "Cannot update scores: reviewId is null or empty");
            return;
        }

        Log.d(TAG, "Vote change detected for review: " + reviewId);

        // Find the review author and update their scores
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

    public interface OnScoresRetrievedListener {
        void onScoresRetrieved(double credibilityScore, double experienceScore);
    }
}
