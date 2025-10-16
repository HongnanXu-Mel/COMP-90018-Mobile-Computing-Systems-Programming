package com.example.food;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.adapter.RestaurantAdapter;
import com.example.food.model.Post;
import com.example.food.model.Restaurant;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";

    private EditText etPostTitle;
    private LinearLayout btnAddImage;
    private LinearLayout btnSelectRestaurant;
    private TextView tvSelectedRestaurant;
    private ImageView ivPreview;
    private LinearLayout layoutAddImagePlaceholder;
    private RatingBar ratingAmbiance;
    private RatingBar ratingServingSpeed;
    private RatingBar ratingFoodQuality;
    private ChipGroup chipGroupComments;
    private Button btnCancel;
    private Button btnPost;

    private Uri selectedImageUri;
    private Restaurant selectedRestaurant;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        initViews();
        setupImagePicker();
        setupListeners();
    }

    private void initViews() {
        etPostTitle = findViewById(R.id.et_post_title);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnSelectRestaurant = findViewById(R.id.btn_select_restaurant);
        tvSelectedRestaurant = findViewById(R.id.tv_selected_restaurant);
        ivPreview = findViewById(R.id.iv_preview);
        layoutAddImagePlaceholder = findViewById(R.id.layout_add_image_placeholder);
        ratingAmbiance = findViewById(R.id.rating_ambiance);
        ratingServingSpeed = findViewById(R.id.rating_serving_speed);
        ratingFoodQuality = findViewById(R.id.rating_food_quality);
        chipGroupComments = findViewById(R.id.chip_group_comments);
        btnCancel = findViewById(R.id.btn_cancel);
        btnPost = findViewById(R.id.btn_post);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivPreview.setImageURI(selectedImageUri);
                        ivPreview.setVisibility(View.VISIBLE);
                        layoutAddImagePlaceholder.setVisibility(View.GONE);
                    }
                }
            }
        );
    }

    private void setupListeners() {
        btnAddImage.setOnClickListener(v -> openImagePicker());
        btnSelectRestaurant.setOnClickListener(v -> showRestaurantSearchDialog());
        btnCancel.setOnClickListener(v -> finish());
        btnPost.setOnClickListener(v -> createPost());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void createPost() {
        String title = etPostTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a post title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ratings
        float ambianceRating = ratingAmbiance.getRating();
        float servingSpeedRating = ratingServingSpeed.getRating();
        float foodQualityRating = ratingFoodQuality.getRating();

        // Get selected quick comments
        List<String> selectedComments = getSelectedComments();
        String content = buildContentFromComments(selectedComments, ambianceRating, servingSpeedRating, foodQualityRating);

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // Show loading
        btnPost.setEnabled(false);
        btnPost.setText("Posting...");

        // Fetch username from Firestore users collection
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String username;
                if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {
                    username = documentSnapshot.getString("name");
                } else {
                    // Fallback: use email prefix if name not found
                    username = currentUser.getEmail() != null ?
                        currentUser.getEmail().split("@")[0] : "Anonymous";
                }

                // Create post object
                Post post = new Post();
                post.setTitle(title);
                post.setContent(content);
                post.setUsername(username);
                post.setUserId(userId);
                post.setTimestamp(Timestamp.now());
                post.setLikesCount(0);
                post.setCommentsCount(0);
                post.setCategory("Restaurant Review");

                if (selectedImageUri != null) {
                    uploadImageAndCreatePost(post);
                } else {
                    savePostToFirestore(post, null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching user info", e);
                // Fallback: use email prefix if fetch fails
                String username = currentUser.getEmail() != null ?
                    currentUser.getEmail().split("@")[0] : "Anonymous";

                // Create post object with fallback username
                Post post = new Post();
                post.setTitle(title);
                post.setContent(content);
                post.setUsername(username);
                post.setUserId(userId);
                post.setTimestamp(Timestamp.now());
                post.setLikesCount(0);
                post.setCommentsCount(0);
                post.setCategory("Restaurant Review");

                if (selectedImageUri != null) {
                    uploadImageAndCreatePost(post);
                } else {
                    savePostToFirestore(post, null);
                }
            });
    }

    private List<String> getSelectedComments() {
        List<String> comments = new ArrayList<>();
        
        if (((Chip) findViewById(R.id.chip_great_food)).isChecked()) {
            comments.add("Great food!");
        }
        if (((Chip) findViewById(R.id.chip_friendly_staff)).isChecked()) {
            comments.add("Friendly staff.");
        }
        if (((Chip) findViewById(R.id.chip_cozy_atmosphere)).isChecked()) {
            comments.add("Cozy atmosphere.");
        }
        if (((Chip) findViewById(R.id.chip_would_come_again)).isChecked()) {
            comments.add("Would come again!");
        }
        
        return comments;
    }

    private String buildContentFromComments(List<String> comments, float ambiance, float servingSpeed, float foodQuality) {
        StringBuilder content = new StringBuilder();
        
        // Add ratings summary
        content.append("Restaurant Review:\n\n");
        content.append("⭐ Ambiance: ").append((int)ambiance).append("/5\n");
        content.append("⭐ Serving Speed: ").append((int)servingSpeed).append("/5\n");
        content.append("⭐ Food Quality: ").append((int)foodQuality).append("/5\n\n");
        
        // Add comments
        if (!comments.isEmpty()) {
            content.append("Comments:\n");
            for (String comment : comments) {
                content.append("• ").append(comment).append("\n");
            }
        }
        
        return content.toString();
    }

    private void uploadImageAndCreatePost(Post post) {
        String imageFileName = "posts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imageFileName);

        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePostToFirestore(post, uri.toString());
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true);
                btnPost.setText("Post");
            });
    }

    private void savePostToFirestore(Post post, String imageUrl) {
        if (imageUrl != null) {
            post.setCoverImageUrl(imageUrl);
        }

        // Add restaurant information if selected
        if (selectedRestaurant != null) {
            post.setRestaurantId(selectedRestaurant.getId());
            post.setRestaurantName(selectedRestaurant.getName());
            post.setRestaurantAddress(selectedRestaurant.getAddress());
        }

        db.collection("posts")
            .add(post)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Post created successfully: " + documentReference.getId());
                Toast.makeText(this, "✅ Post created successfully!", Toast.LENGTH_SHORT).show();
                
                // Return to main activity and refresh
                Intent intent = new Intent(CreatePostActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating post", e);
                Toast.makeText(this, "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true);
                btnPost.setText("Post");
            });
    }

    /**
     * Show restaurant search dialog
     */
    private void showRestaurantSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_restaurant_search, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etSearch = dialogView.findViewById(R.id.et_restaurant_search);
        RecyclerView rvResults = dialogView.findViewById(R.id.rv_restaurant_results);
        TextView tvNoResults = dialogView.findViewById(R.id.tv_no_results);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        // Setup RecyclerView
        RestaurantAdapter adapter = new RestaurantAdapter();
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        // Load all restaurants initially
        loadRestaurants(adapter, rvResults, tvNoResults, "");

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                loadRestaurants(adapter, rvResults, tvNoResults, query);
            }
        });

        // Handle restaurant selection
        adapter.setOnRestaurantClickListener(restaurant -> {
            selectedRestaurant = restaurant;
            tvSelectedRestaurant.setText(restaurant.getName());
            tvSelectedRestaurant.setTextColor(getResources().getColor(android.R.color.black));
            dialog.dismiss();
            Toast.makeText(this, "Selected: " + restaurant.getName(), Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Load restaurants from Firebase
     */
    private void loadRestaurants(RestaurantAdapter adapter, RecyclerView rvResults, TextView tvNoResults, String query) {
        db.collection("restaurants")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Restaurant> restaurants = new ArrayList<>();
                queryDocumentSnapshots.forEach(document -> {
                    Restaurant restaurant = document.toObject(Restaurant.class);
                    restaurant.setId(document.getId());
                    
                    // Filter by query
                    if (query.isEmpty() || 
                        restaurant.getName().toLowerCase().contains(query.toLowerCase()) ||
                        restaurant.getAddress().toLowerCase().contains(query.toLowerCase())) {
                        restaurants.add(restaurant);
                    }
                });

                adapter.updateRestaurants(restaurants);

                if (restaurants.isEmpty()) {
                    rvResults.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    rvResults.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading restaurants", e);
                Toast.makeText(this, "Failed to load restaurants", Toast.LENGTH_SHORT).show();
            });
    }
}

