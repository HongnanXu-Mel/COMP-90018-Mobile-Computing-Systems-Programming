package com.example.food.service;

import android.util.Log;

import com.example.food.model.Post;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for loading post data from Firebase Firestore
 * This class only handles data retrieval, no uploading functionality
 */
public class PostService {
    private static final String TAG = "PostService";
    private static final String COLLECTION_POSTS = "posts";
    
    private FirebaseFirestore db;
    private CollectionReference postsRef;

    public PostService() {
        db = FirebaseFirestore.getInstance();
        postsRef = db.collection(COLLECTION_POSTS);
    }

    public interface PostsLoadCallback {
        void onSuccess(List<Post> posts);
        void onError(Exception e);
    }

    /**
     * Load all posts ordered by timestamp (newest first)
     */
    public void loadPosts(PostsLoadCallback callback) {
        postsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Post> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = document.toObject(Post.class);
                                post.setPostId(document.getId());
                                posts.add(post);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing post: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(posts);
                        Log.d(TAG, "Loaded " + posts.size() + " posts from Firebase");
                    } else {
                        Log.w(TAG, "Error getting posts from Firebase", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Load posts with pagination limit
     */
    public void loadPostsWithLimit(int limit, PostsLoadCallback callback) {
        postsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Post> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = document.toObject(Post.class);
                                post.setPostId(document.getId());
                                posts.add(post);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing post: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(posts);
                        Log.d(TAG, "Loaded " + posts.size() + " posts with limit " + limit);
                    } else {
                        Log.w(TAG, "Error getting posts with limit", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Search posts by title, content, or username (client-side filtering)
     */
    public void searchPosts(String query, PostsLoadCallback callback) {
        // Load all posts first, then filter on client side
        loadPosts(new PostsLoadCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                List<Post> filteredPosts = new ArrayList<>();
                String lowerQuery = query.toLowerCase().trim();
                
                for (Post post : posts) {
                    if ((post.getTitle() != null && post.getTitle().toLowerCase().contains(lowerQuery)) ||
                        (post.getContent() != null && post.getContent().toLowerCase().contains(lowerQuery)) ||
                        (post.getUsername() != null && post.getUsername().toLowerCase().contains(lowerQuery))) {
                        filteredPosts.add(post);
                    }
                }
                
                callback.onSuccess(filteredPosts);
                Log.d(TAG, "Search returned " + filteredPosts.size() + " posts for query: " + query);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}