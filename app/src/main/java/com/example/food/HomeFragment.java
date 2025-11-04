package com.example.food;

/**
 * HomeFragment - Main feed fragment displaying all restaurant reviews
 * 
 * Features:
 * - Display reviews in a staggered grid layout (Pinterest/Xiaohongshu style)
 * - Search functionality to filter reviews by text
 * - Pull-to-refresh to reload content
 * - Load restaurant data for each review
 * - Click reviews to open detail dialog
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.food.adapters.ReviewWidgetAdapter;
import com.example.food.data.Review;
import com.example.food.dialogs.ReviewDetailsDialog;
import com.example.food.model.Restaurant;
import com.example.food.service.ReviewService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    // Tag for logging
    private static final String TAG = "HomeFragment";
    
    // UI Components
    
    private RecyclerView rvReviews; // Grid of review cards
    private ReviewWidgetAdapter reviewAdapter; // Adapter for review cards
    private SwipeRefreshLayout swipeRefreshLayout; // Pull-to-refresh
    private EditText etSearch; // Search input field
    private LinearLayout layoutEmptyState; // Empty state view
    
    // Services and data
    
    private ReviewService reviewService; // Service for loading reviews
    private List<Review> allReviews; // All loaded reviews
    private Map<String, Restaurant> restaurantMap; // Restaurant data by ID
    private FirebaseFirestore db; // Firestore database instance

    /**
     * Create and initialize the fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        
        reviewService = new ReviewService();
        allReviews = new ArrayList<>();
        restaurantMap = new HashMap<>();
        db = FirebaseFirestore.getInstance();
        
        loadReviews();
        
        return view;
    }

    /**
     * Initialize UI component references
     */
    private void initViews(View view) {
        rvReviews = view.findViewById(R.id.rv_posts);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        etSearch = view.findViewById(R.id.et_search);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    /**
     * Configure RecyclerView with staggered grid layout
     */
    private void setupRecyclerView() {
        reviewAdapter = new ReviewWidgetAdapter(allReviews, (review, restaurant) -> {
            // Open review details dialog
            ReviewDetailsDialog dialog = new ReviewDetailsDialog(getContext(), review, restaurant);
            dialog.show();
        });
        
        // Set up staggered grid layout for Pinterest/Xiaohongshu-like appearance
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvReviews.setLayoutManager(layoutManager);
        rvReviews.setAdapter(reviewAdapter);
        
        // Set restaurant map to adapter
        reviewAdapter.setRestaurantMap(restaurantMap);
    }

    /**
     * Setup pull-to-refresh functionality
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_gradient_start);
        swipeRefreshLayout.setOnRefreshListener(this::refreshReviews);
    }

    /**
     * Setup search functionality with real-time filtering
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterReviews(query);
            }
        });
    }
    /**
     * Load all reviews from Firebase and then load associated restaurants
     */
    private void loadReviews() {
        showLoading(true);
        
        reviewService.loadReviews(new ReviewService.ReviewsLoadCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    allReviews.clear();
                    allReviews.addAll(reviews);
                    loadRestaurants();
                    showLoading(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to load reviews: " + e.getMessage());
                    Log.e(TAG, "Error loading reviews", e);
                });
            }
        });
    }

    /**
     * Refresh reviews data (called by pull-to-refresh)
     */
    private void refreshReviews() {
        reviewService.loadReviews(new ReviewService.ReviewsLoadCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    allReviews.clear();
                    allReviews.addAll(reviews);
                    loadRestaurants();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Failed to refresh reviews");
                    Log.e(TAG, "Error refreshing reviews", e);
                });
            }
        });
    }

    /**
     * Load restaurant data for all reviews in the list
     */
    private void loadRestaurants() {
        if (allReviews.isEmpty()) {
            updateUI();
            return;
        }

        List<String> restaurantIds = new ArrayList<>();
        for (Review review : allReviews) {
            if (review.getRestaurantId() != null && !restaurantIds.contains(review.getRestaurantId())) {
                restaurantIds.add(review.getRestaurantId());
            }
        }

        if (restaurantIds.isEmpty()) {
            updateUI();
            return;
        }

        for (String restaurantId : restaurantIds) {
            db.collection("restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                            if (restaurant != null) {
                                restaurant.setId(documentSnapshot.getId());
                                restaurantMap.put(restaurantId, restaurant);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing restaurant document", e);
                        }
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant: " + restaurantId, e);
                    updateUI();
                });
        }
    }

    /**
     * Filter reviews based on search query
     * @param query Search text to filter by
     */
    private void filterReviews(String query) {
        if (reviewAdapter != null) {
            // Simple filtering based on review content
            List<Review> filteredReviews = new ArrayList<>();
            if (query == null || query.trim().isEmpty()) {
                filteredReviews.addAll(allReviews);
            } else {
                String lowerCaseQuery = query.toLowerCase().trim();
                for (Review review : allReviews) {
                    if ((review.getDescription() != null && review.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                        (review.getCaption() != null && review.getCaption().toLowerCase().contains(lowerCaseQuery)) ||
                        (review.getRestaurantName() != null && review.getRestaurantName().toLowerCase().contains(lowerCaseQuery))) {
                        filteredReviews.add(review);
                    }
                }
            }
            reviewAdapter.setReviews(filteredReviews);
            updateEmptyState();
        }
    }

    /**
     * Update adapter with current data and restaurant mappings
     */
    private void updateUI() {
        if (reviewAdapter != null) {
            reviewAdapter.setReviews(allReviews);
            reviewAdapter.setRestaurantMap(restaurantMap);
            updateEmptyState();
        }
    }

    /**
     * Show/hide empty state based on review count
     */
    private void updateEmptyState() {
        if (reviewAdapter != null && layoutEmptyState != null && rvReviews != null) {
            if (reviewAdapter.getItemCount() == 0) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvReviews.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvReviews.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    /**
     * Display error message to user
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}

