package com.example.food;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.adapter.CommentAdapter;
import com.example.food.model.Comment;
import com.example.food.model.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";
    public static final String EXTRA_POST_ID = "post_id";

    private ImageView btnBack;
    private ImageView ivPostImage;
    private TextView tvPostTitle;
    private TextView tvPostUsername;
    private TextView tvPostTime;
    private TextView tvRestaurantName;
    private TextView tvRestaurantAddress;
    private LinearLayout layoutRestaurantInfo;
    private TextView tvPostContent;
    private TextView tvCommentsCountHeader;
    private TextView tvNoComments;
    private RecyclerView rvComments;
    private LinearLayout btnLike;
    private ImageView ivLikeIcon;
    private TextView tvLikeCount;
    private EditText etCommentInput;
    private ImageView btnSendComment;

    private CommentAdapter commentAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String postId;
    private Post currentPost;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get post ID from intent
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        loadPostDetails();
        loadComments();
        checkIfUserLiked();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivPostImage = findViewById(R.id.iv_post_image);
        tvPostTitle = findViewById(R.id.tv_post_title);
        tvPostUsername = findViewById(R.id.tv_post_username);
        tvPostTime = findViewById(R.id.tv_post_time);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvRestaurantAddress = findViewById(R.id.tv_restaurant_address);
        layoutRestaurantInfo = findViewById(R.id.layout_restaurant_info);
        tvPostContent = findViewById(R.id.tv_post_content);
        tvCommentsCountHeader = findViewById(R.id.tv_comments_count_header);
        tvNoComments = findViewById(R.id.tv_no_comments);
        rvComments = findViewById(R.id.rv_comments);
        btnLike = findViewById(R.id.btn_like);
        ivLikeIcon = findViewById(R.id.iv_like_icon);
        tvLikeCount = findViewById(R.id.tv_like_count);
        etCommentInput = findViewById(R.id.et_comment_input);
        btnSendComment = findViewById(R.id.btn_send_comment);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLike.setOnClickListener(v -> toggleLike());
        btnSendComment.setOnClickListener(v -> addComment());
    }

    private void checkIfUserLiked() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> likedBy = (List<String>) documentSnapshot.get("likedBy");
                    isLiked = likedBy != null && likedBy.contains(currentUser.getUid());
                    updateLikeUI();
                }
            });
    }

    private void updateLikeUI() {
        if (isLiked) {
            ivLikeIcon.setColorFilter(getResources().getColor(R.color.primary_gradient_start));
        } else {
            ivLikeIcon.clearColorFilter();
        }
    }

    private void loadPostDetails() {
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentPost = documentSnapshot.toObject(Post.class);
                    if (currentPost != null) {
                        currentPost.setPostId(documentSnapshot.getId());
                        displayPostDetails();
                    }
                } else {
                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading post", e);
                Toast.makeText(this, "Failed to load post", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayPostDetails() {
        tvPostTitle.setText(currentPost.getTitle());
        tvPostUsername.setText(currentPost.getUsername());
        tvPostContent.setText(currentPost.getContent());
        tvLikeCount.setText(String.valueOf(currentPost.getLikesCount()));

        // Load cover image
        if (currentPost.getCoverImageUrl() != null && !currentPost.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                .load(currentPost.getCoverImageUrl())
                .into(ivPostImage);
        }

        // Format timestamp
        if (currentPost.getTimestamp() != null) {
            tvPostTime.setText(getTimeAgo(currentPost.getTimestamp().toDate()));
        }

        // Display restaurant info if available
        if (currentPost.getRestaurantName() != null && !currentPost.getRestaurantName().isEmpty()) {
            layoutRestaurantInfo.setVisibility(View.VISIBLE);
            tvRestaurantName.setText(currentPost.getRestaurantName());
            if (currentPost.getRestaurantAddress() != null) {
                tvRestaurantAddress.setText(currentPost.getRestaurantAddress());
            }
        }
    }

    private void loadComments() {
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> commentsData = 
                        (List<Map<String, Object>>) documentSnapshot.get("comments");
                    
                    List<Comment> comments = new ArrayList<>();
                    if (commentsData != null) {
                        for (Map<String, Object> commentMap : commentsData) {
                            Comment comment = new Comment();
                            comment.setUserId((String) commentMap.get("userId"));
                            comment.setUsername((String) commentMap.get("username"));
                            comment.setContent((String) commentMap.get("content"));
                            comment.setTimestamp((Timestamp) commentMap.get("timestamp"));
                            comments.add(comment);
                        }
                    }

                    // Sort comments by timestamp (newest first)
                    comments.sort((c1, c2) -> {
                        if (c1.getTimestamp() == null) return 1;
                        if (c2.getTimestamp() == null) return -1;
                        return c2.getTimestamp().compareTo(c1.getTimestamp());
                    });

                    commentAdapter.updateComments(comments);
                    tvCommentsCountHeader.setText(String.valueOf(comments.size()));

                    if (comments.isEmpty()) {
                        tvNoComments.setVisibility(View.VISIBLE);
                        rvComments.setVisibility(View.GONE);
                    } else {
                        tvNoComments.setVisibility(View.GONE);
                        rvComments.setVisibility(View.VISIBLE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading comments", e);
                Toast.makeText(this, "Failed to load comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void toggleLike() {
        if (currentPost == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to like", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference postRef = db.collection("posts").document(postId);
        
        if (isLiked) {
            // Unlike - remove userId from likedBy array
            Map<String, Object> updates = new HashMap<>();
            updates.put("likedBy", FieldValue.arrayRemove(currentUser.getUid()));
            updates.put("likesCount", FieldValue.increment(-1));
            
            postRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    isLiked = false;
                    currentPost.setLikesCount(currentPost.getLikesCount() - 1);
                    tvLikeCount.setText(String.valueOf(currentPost.getLikesCount()));
                    updateLikeUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error unliking post", e);
                    Toast.makeText(this, "Failed to unlike", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Like - add userId to likedBy array
            Map<String, Object> updates = new HashMap<>();
            updates.put("likedBy", FieldValue.arrayUnion(currentUser.getUid()));
            updates.put("likesCount", FieldValue.increment(1));
            
            postRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    isLiked = true;
                    currentPost.setLikesCount(currentPost.getLikesCount() + 1);
                    tvLikeCount.setText(String.valueOf(currentPost.getLikesCount()));
                    updateLikeUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error liking post", e);
                    Toast.makeText(this, "Failed to like", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void addComment() {
        String content = etCommentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = currentUser.getDisplayName() != null ? 
            currentUser.getDisplayName() : "Anonymous";

        // Create comment data map
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", currentUser.getUid());
        commentData.put("username", username);
        commentData.put("content", content);
        commentData.put("timestamp", Timestamp.now());

        // Disable send button
        btnSendComment.setEnabled(false);

        // Add comment to posts document's comments array
        Map<String, Object> updates = new HashMap<>();
        updates.put("comments", FieldValue.arrayUnion(commentData));
        updates.put("commentsCount", FieldValue.increment(1));

        db.collection("posts").document(postId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Clear input and reload comments
                etCommentInput.setText("");
                btnSendComment.setEnabled(true);
                loadComments();
                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error adding comment", e);
                btnSendComment.setEnabled(true);
                Toast.makeText(this, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private String getTimeAgo(Date date) {
        long time = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " days ago";
        } else if (hours > 0) {
            return hours + " hours ago";
        } else if (minutes > 0) {
            return minutes + " minutes ago";
        } else {
            return "Just now";
        }
    }
}

