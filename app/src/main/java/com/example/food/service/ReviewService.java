package com.example.food.service;

import android.util.Log;

import com.example.food.data.Review;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewService - Service layer for managing review data operations
 * 
 * This service provides methods for:
 * - Loading reviews from Firebase Firestore
 * - Saving new reviews to Firestore
 * - Searching and filtering reviews
 * - Pagination support
 * 
 * All operations are asynchronous with callback interfaces
 */
public class ReviewService {
    // Tag for logging
    private static final String TAG = "ReviewService";
    // Firestore collection name for reviews
    private static final String COLLECTION_REVIEWS = "reviews";
    
    // Firebase instances
    private FirebaseFirestore db; // Firestore database instance
    private CollectionReference reviewsRef; // Reference to reviews collection

    /**
     * Constructor - initializes Firebase Firestore
     */
    public ReviewService() {
        db = FirebaseFirestore.getInstance();
        reviewsRef = db.collection(COLLECTION_REVIEWS);
    }

    /**
     * Callback interface for review list operations
     */
    public interface ReviewsLoadCallback {
        void onSuccess(List<Review> reviews);
        void onError(Exception e);
    }
    
    /**
     * Callback interface for single review save operations
     */
    public interface ReviewSaveCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Load all reviews from Firestore, ordered by creation date (newest first)
     */
    public void loadReviews(ReviewsLoadCallback callback) {
        reviewsRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Review review = document.toObject(Review.class);
                                review.setId(document.getId());
                                reviews.add(review);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing review: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(reviews);
                        Log.d(TAG, "Loaded " + reviews.size() + " reviews from Firebase");
                    } else {
                        Log.w(TAG, "Error getting reviews from Firebase", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Load reviews with pagination limit
     * @param limit Maximum number of reviews to load
     * @param callback Callback for results
     */
    public void loadReviewsWithLimit(int limit, ReviewsLoadCallback callback) {
        reviewsRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Review review = document.toObject(Review.class);
                                review.setId(document.getId());
                                reviews.add(review);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing review: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(reviews);
                        Log.d(TAG, "Loaded " + reviews.size() + " reviews with limit " + limit);
                    } else {
                        Log.w(TAG, "Error getting reviews with limit", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Search reviews by text query (client-side filtering)
     * Searches in description, caption, and restaurant name
     * @param query Search text
     * @param callback Callback for filtered results
     */
    public void searchReviews(String query, ReviewsLoadCallback callback) {
        // Load all reviews first, then filter on client side
        loadReviews(new ReviewsLoadCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                List<Review> filteredReviews = new ArrayList<>();
                String lowerQuery = query.toLowerCase().trim();
                
                for (Review review : reviews) {
                    if ((review.getDescription() != null && review.getDescription().toLowerCase().contains(lowerQuery)) ||
                        (review.getCaption() != null && review.getCaption().toLowerCase().contains(lowerQuery)) ||
                        (review.getRestaurantName() != null && review.getRestaurantName().toLowerCase().contains(lowerQuery))) {
                        filteredReviews.add(review);
                    }
                }
                
                callback.onSuccess(filteredReviews);
                Log.d(TAG, "Search returned " + filteredReviews.size() + " reviews for query: " + query);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Save a new review to Firebase Firestore
     * Note: userName and restaurantName are excluded (fetched dynamically)
     * @param review The review object to save
     * @param callback Callback for save result
     */
    public void saveReview(Review review, ReviewSaveCallback callback) {
        // Manually create a map to ensure excluded fields are not stored
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", review.getUserId());
        reviewData.put("restaurantId", review.getRestaurantId());
        reviewData.put("caption", review.getCaption());
        reviewData.put("description", review.getDescription());
        reviewData.put("rating", review.getRating());
        reviewData.put("accuracyPercent", review.getAccuracyPercent());
        reviewData.put("imageUrls", review.getImageUrls());
        reviewData.put("firstImageType", review.getFirstImageType());
        reviewData.put("createdAt", review.getCreatedAt());
        reviewData.put("votes", review.getVotes());
        reviewData.put("comments", review.getComments());
        
        // Use Firestore auto-generated document ID
        reviewsRef
                .add(reviewData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Review saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving review", e);
                    callback.onError(e);
                });
    }
}
