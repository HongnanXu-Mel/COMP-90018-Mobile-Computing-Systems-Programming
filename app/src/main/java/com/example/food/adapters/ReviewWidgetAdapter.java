package com.example.food.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.example.food.data.Review;
import com.example.food.model.Restaurant;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewWidgetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_REVIEW = 0;
    private static final int VIEW_TYPE_SKELETON = 1;
    
    private List<Review> reviews;
    private Map<String, Restaurant> restaurantMap;
    private OnReviewClickListener listener;
    private boolean isLoading = false;

    public interface OnReviewClickListener {
        void onReviewClick(Review review, Restaurant restaurant);
    }

    public ReviewWidgetAdapter(List<Review> reviews, OnReviewClickListener listener) {
        this.reviews = reviews;
        this.listener = listener;
        this.restaurantMap = new HashMap<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading && position < 8) { // Show 8 skeleton cards so it looks better
            return VIEW_TYPE_SKELETON;
        }
        return VIEW_TYPE_REVIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SKELETON) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review_card_skeleton, parent, false);
            return new SkeletonViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review_card, parent, false);
            return new ReviewViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SkeletonViewHolder) {
           
            return;
        } else if (holder instanceof ReviewViewHolder) {
            ReviewViewHolder reviewHolder = (ReviewViewHolder) holder;
            int reviewPosition = isLoading ? position - 8 : position;
            if (reviewPosition >= 0 && reviewPosition < reviews.size()) {
                Review review = reviews.get(reviewPosition);
                reviewHolder.bind(review);
            }
        }
    }

    @Override
    public int getItemCount() {
        return isLoading ? (reviews != null ? reviews.size() + 8 : 8) : (reviews != null ? reviews.size() : 0);
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged();
    }

    public void setRestaurantMap(Map<String, Restaurant> restaurantMap) {
        this.restaurantMap = restaurantMap != null ? restaurantMap : new HashMap<>();
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRestaurantImage;
        private TextView tvCaption;
        private TextView tvRating;
        private TextView tvAccuracy;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    int reviewPosition = isLoading ? position - 8 : position;
                    if (reviewPosition >= 0 && reviewPosition < reviews.size()) {
                        Review review = reviews.get(reviewPosition);
                        Restaurant restaurant = restaurantMap.get(review.getRestaurantId());
                        if (restaurant == null) {
                           
                            restaurant = new Restaurant(review.getRestaurantId(), 
                                review.getRestaurantName() != null ? review.getRestaurantName() : "Unknown Restaurant", 
                                "", 0.0, 0.0, "Restaurant", "Melbourne");
                        }
                        listener.onReviewClick(review, restaurant);
                    }
                }
            });
        }

        public void bind(Review review) {
            if (tvCaption != null && review.getCaption() != null) {
                tvCaption.setText(review.getCaption());
            }
            if (tvRating != null) {
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));
            }
            if (tvAccuracy != null) {
                tvAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", review.getAccuracyPercent()));
            }
            if (ivRestaurantImage != null) {
                // Clear any previous state
                ivRestaurantImage.clearColorFilter();
                ivRestaurantImage.setPadding(0, 0, 0, 0);
                
                if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(review.getImageUrls().get(0))
                            .placeholder(R.drawable.ic_restaurant)
                            .error(R.drawable.ic_restaurant)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    // Check if image is portrait (height > width)
                                    int width = resource.getIntrinsicWidth();
                                    int height = resource.getIntrinsicHeight();
                                    if (height > width) {
                                        // Portrait image - use centerCrop to fill the view
                                        ivRestaurantImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    } else {
                                        // Square or landscape - use centerInside to maintain aspect ratio
                                        ivRestaurantImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                    }
                                    return false;
                                }
                            })
                            .into(ivRestaurantImage);
                } else {
                    ivRestaurantImage.setImageResource(R.drawable.ic_restaurant);
                    ivRestaurantImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        }
    }

    class SkeletonViewHolder extends RecyclerView.ViewHolder {
        public SkeletonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
