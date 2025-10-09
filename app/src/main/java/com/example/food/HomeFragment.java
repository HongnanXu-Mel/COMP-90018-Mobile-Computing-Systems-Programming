package com.example.food;

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

import com.example.food.adapter.PostAdapter;
import com.example.food.model.Post;
import com.example.food.service.PostService;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;
    
    private PostService postService;
    private List<Post> allPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        
        postService = new PostService();
        allPosts = new ArrayList<>();
        
        loadPosts();
        
        return view;
    }

    private void initViews(View view) {
        rvPosts = view.findViewById(R.id.rv_posts);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        etSearch = view.findViewById(R.id.et_search);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(getContext());
        
        // Set up staggered grid layout for Xiaohongshu-like appearance
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvPosts.setLayoutManager(layoutManager);
        rvPosts.setAdapter(postAdapter);
        
        // Set post click listener
        postAdapter.setOnPostClickListener(post -> {
            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_gradient_start);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterPosts(query);
            }
        });
    }


    private void loadPosts() {
        showLoading(true);
        
        postService.loadPosts(new PostService.PostsLoadCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    allPosts.clear();
                    allPosts.addAll(posts);
                    updateUI();
                    showLoading(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to load posts: " + e.getMessage());
                    Log.e(TAG, "Error loading posts", e);
                });
            }
        });
    }

    private void refreshPosts() {
        postService.loadPosts(new PostService.PostsLoadCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    allPosts.clear();
                    allPosts.addAll(posts);
                    updateUI();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Failed to refresh posts");
                    Log.e(TAG, "Error refreshing posts", e);
                });
            }
        });
    }

    private void filterPosts(String query) {
        if (postAdapter != null) {
            postAdapter.filterPosts(allPosts, query);
            updateEmptyState();
        }
    }

    private void updateUI() {
        if (postAdapter != null) {
            postAdapter.updatePosts(allPosts);
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (postAdapter != null && layoutEmptyState != null && rvPosts != null) {
            if (postAdapter.getItemCount() == 0) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvPosts.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvPosts.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}

