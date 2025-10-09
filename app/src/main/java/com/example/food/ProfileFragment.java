package com.example.food;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.View;


import com.bumptech.glide.Glide;
import com.example.food.adapters.ReviewWidgetAdapter;
import com.example.food.cache.ProfileCacheManager;
import com.example.food.data.Review;
import com.example.food.data.UserProfile;
import com.example.food.dialogs.ReviewDetailsDialog;
import com.example.food.model.Restaurant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    
    // Views
    private ImageView ivProfilePicture;
    private TextView tvUsername, tvBio, tvCredibilityScore, tvExperienceScore, tvEngagementScore;
    private RecyclerView rvReviews;
    private LinearLayout emptyState;
    private LinearLayout credibilityCard, experienceCard, engagementCard;
    
    // Adapters and data
    private ReviewWidgetAdapter reviewAdapter;
    private List<Review> reviews;
    private Map<String, Restaurant> restaurantMap;
    private UserProfile userProfile;
    
    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration profileListener;
    private ProfileCacheManager cacheManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initFirebase();
        setupRecyclerView();
        setupCardClickListeners();
        loadUserData();
        setupReviews();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvDisplayName);
        tvBio = view.findViewById(R.id.tvBio);
        tvCredibilityScore = view.findViewById(R.id.tvCredibilityScore);
        tvExperienceScore = view.findViewById(R.id.tvExperienceScore);
        tvEngagementScore = view.findViewById(R.id.tvEngagementScore);
        rvReviews = view.findViewById(R.id.rvReviews);
        emptyState = view.findViewById(R.id.emptyReviewsLayout);
        
        // Metric cards - engagement one removed for now in ui seems unnecessary
        credibilityCard = view.findViewById(R.id.credibilityCard);
        experienceCard = view.findViewById(R.id.experienceCard);
        engagementCard = view.findViewById(R.id.engagementCard);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cacheManager = ProfileCacheManager.getInstance(requireContext());
    }

    private void setupCardClickListeners() {
        if (credibilityCard != null) {
            credibilityCard.setOnClickListener(v -> showMetricInfoBottomSheet(
                getString(R.string.profile_metric_credibility_title),
                getString(R.string.profile_metric_credibility_body),
                getString(R.string.credibility_score_interpretation),
                "credibility"
            ));
        }

        if (experienceCard != null) {
            experienceCard.setOnClickListener(v -> showMetricInfoBottomSheet(
                getString(R.string.profile_metric_experience_title),
                getString(R.string.profile_metric_experience_body),
                getString(R.string.experience_score_interpretation),
                "experience"
            ));
        }

        if (engagementCard != null) {
            engagementCard.setOnClickListener(v -> showMetricInfoBottomSheet(
                getString(R.string.engagement_score_title),
                getString(R.string.engagement_score_body),
                getString(R.string.engagement_score_interpretation),
                "engagement"
            ));
        }
    }

    private void showMetricInfoBottomSheet(String title, String message, String interpretation, String metricType) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View view = View.inflate(requireContext(), R.layout.bottom_sheet_metric_info, null);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvScoreInterpretation = view.findViewById(R.id.tvScoreInterpretation);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        tvTitle.setText(title);
        tvMessage.setText(message);
        tvScoreInterpretation.setText(interpretation);

        
        loadMetricScore(metricType, tvScoreInterpretation, interpretation);

        


        btnClose.setOnClickListener(v -> sheet.dismiss());

        sheet.setContentView(view);
        sheet.show();
    }

    private void loadMetricScore(String metricType, TextView scoreView, String baseInterpretation) {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
            .document(auth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int score = 0;
                    switch (metricType) {
                        case "credibility":
                            score = documentSnapshot.getLong("credibilityScore") != null ? 
                                   documentSnapshot.getLong("credibilityScore").intValue() : 0;
                            break;
                        case "experience":
                            score = documentSnapshot.getLong("experienceScore") != null ? 
                                   documentSnapshot.getLong("experienceScore").intValue() : 0;
                            break;
                        case "engagement":
                            score = documentSnapshot.getLong("engagementScore") != null ? 
                                   documentSnapshot.getLong("engagementScore").intValue() : 0;
                            break;
                    }
                    
                    String dynamicInterpretation = baseInterpretation + "\n\nCurrent Score: " + score + "/100";
                    scoreView.setText(dynamicInterpretation);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading metric score for " + metricType, e);
            
            });
    }

    private void setupRecyclerView() {
        reviews = new ArrayList<>();
        restaurantMap = new HashMap<>();
        
        reviewAdapter = new ReviewWidgetAdapter(new ArrayList<>(), new ReviewWidgetAdapter.OnReviewClickListener() {
            @Override
            public void onReviewClick(Review review, Restaurant restaurant) {
                showReviewDetails(review, restaurant);
            }
        });
        
        // Use StaggeredGridLayoutManager for diff image sizes
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvReviews.setLayoutManager(layoutManager);
        
        // Add spacing between items so it looks better
        int spacing = (int) (4 * getResources().getDisplayMetrics().density);
        rvReviews.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
                outRect.top = spacing;
                outRect.bottom = spacing;
            }
        });
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated");
            return;
        }

        // Load cached data
        loadCachedData();
        
        // Set default scores from strings placeholder
        tvCredibilityScore.setText(getString(R.string.credibility_placeholder));
        tvExperienceScore.setText(getString(R.string.experience_placeholder));
        tvEngagementScore.setText(getString(R.string.engagement_placeholder));
        
        
        loadUserProfileOnce();
    }

    private void loadCachedData() {
        UserProfile cachedProfile = cacheManager.getCachedUserProfile();
        if (cachedProfile != null) {
            userProfile = cachedProfile;
            updateUserUI();
        }
    }

    private void loadUserProfileOnce() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    userProfile = documentSnapshot.toObject(UserProfile.class);
                    if (userProfile != null) {
                        // Cache the fresh data
                        cacheManager.cacheUserProfile(userProfile);
                        updateUserUI();
                    }
                } else {
                    // Create default profile if doesn't exist
                    createDefaultProfile();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user profile", e);
            });
    }

    private void updateUserUI() {
        if (userProfile == null) return;

        // Set username from Firestore
        String name = userProfile.getName();
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.username_placeholder);
        }
        tvUsername.setText(name);

        // Set bio from Firestore
        String bio = userProfile.getBio();
        if (bio == null || bio.trim().isEmpty()) {
            bio = getString(R.string.bio_placeholder);
        }
        tvBio.setText(bio);
    }

    private void createDefaultProfile() {
        if (auth.getCurrentUser() == null) return;

        String name = auth.getCurrentUser().getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            name = auth.getCurrentUser().getEmail().split("@")[0];
        }

        userProfile = new UserProfile(
            auth.getCurrentUser().getUid(),
            name,
            name,
            auth.getCurrentUser().getEmail(),
            getString(R.string.bio_placeholder)
        );

        // Update UI straight away
        updateUserUI();

        // Save to Firestore db
        db.collection("users").document(auth.getCurrentUser().getUid())
            .set(userProfile)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created successfully"))
            .addOnFailureListener(e -> Log.e(TAG, "Error creating user profile", e));
    }

    private void loadProfilePicture() {
        if (auth.getCurrentUser() == null) return;

        // Set default image to avoid errors
        ivProfilePicture.setImageResource(R.drawable.ic_person);
        Log.d(TAG, "Profile picture set to default to avoid 404 errors");
    }

    private void setupReviews() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Show skeleton loading
        reviewAdapter.setLoading(true);

        // Remove previous listener if exists
        if (profileListener != null) {
            profileListener.remove();
        }

        // Listen in real-time for any changes to this user's reviews so the UI always stays up to date
        profileListener = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .limit(200)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error listening to reviews", e);
                    // Hide skeleton loading on error
                
                    reviewAdapter.setLoading(false);
                    showEmptyState();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    reviews.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Review review = document.toObject(Review.class);
                            review.setId(document.getId());
                            reviews.add(review);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing review document", ex);
                        }
                    }

                    // Hide skeleton loading
                    reviewAdapter.setLoading(false);

                    if (reviews.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        loadRestaurants();
                    }
                }
            });
    }

    private void loadRestaurants() {
        if (reviews.isEmpty()) return;

        // Get unique restaurant IDs
        List<String> restaurantIds = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getRestaurantId() != null && !restaurantIds.contains(review.getRestaurantId())) {
                restaurantIds.add(review.getRestaurantId());
            }
        }

        if (restaurantIds.isEmpty()) {
            updateReviews();
            return;
        }

        // Load restaurants
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
                    updateReviews();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant: " + restaurantId, e);
                    updateReviews();
                });
        }
    }

    private void updateReviews() {
        reviewAdapter.setReviews(reviews);
        reviewAdapter.setRestaurantMap(restaurantMap);
    }

    private void showReviewDetails(Review review, Restaurant restaurant) {
        if (getContext() == null) return;
        
        ReviewDetailsDialog dialog = new ReviewDetailsDialog(getContext(), review, restaurant);
        dialog.show();
    }

    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (rvReviews != null) {
            rvReviews.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (rvReviews != null) {
            rvReviews.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            profileListener.remove();
        }
    }
}


