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
import com.example.food.data.ActivityItem;
import com.example.food.data.Review;
import com.example.food.dialogs.ReviewDetailsDialog;
import com.example.food.model.Restaurant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    
    private List<ActivityItem> activities;
    private Map<String, Review> reviewMap;
    private Map<String, Restaurant> restaurantMap;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(ActivityItem activity, Review review, Restaurant restaurant);
    }

    public ActivityAdapter(List<ActivityItem> activities, OnActivityClickListener listener) {
        this.activities = activities != null ? activities : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void setActivities(List<ActivityItem> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setReviewMap(Map<String, Review> reviewMap) {
        this.reviewMap = reviewMap;
        notifyDataSetChanged();
    }

    public void setRestaurantMap(Map<String, Restaurant> restaurantMap) {
        this.restaurantMap = restaurantMap;
        notifyDataSetChanged();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private de.hdodenhof.circleimageview.CircleImageView ivUserAvatar;
        private TextView tvUserName;
        private TextView tvRestaurantName;
        private TextView tvActivityText;
        private TextView tvTimestamp;
        private ImageView ivActivityIcon;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvActivityText = itemView.findViewById(R.id.tvActivityText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivActivityIcon = itemView.findViewById(R.id.ivActivityIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ActivityItem activity = activities.get(position);
                    Review review = reviewMap != null ? reviewMap.get(activity.getReviewId()) : null;
                    Restaurant restaurant = restaurantMap != null ? restaurantMap.get(activity.getReviewId()) : null;
                    listener.onActivityClick(activity, review, restaurant);
                }
            });
        }

        public void bind(ActivityItem activity) {
            // Set user avatar. show image if exists, show placeholder if not
            if (activity.getUserAvatarUrl() != null && !activity.getUserAvatarUrl().trim().isEmpty()) {
                ivUserAvatar.setBorderWidth(2);
                ivUserAvatar.setBorderColor(itemView.getContext().getColor(R.color.logo_primary));
                Glide.with(itemView.getContext())
                        .load(activity.getUserAvatarUrl())
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .centerCrop()
                        .override(72, 72)
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setBorderWidth(0);
                ivUserAvatar.setImageResource(R.drawable.ic_person);
            }

            // Set user name
            String userName = activity.getUserName() != null ? activity.getUserName() : "Someone";
            tvUserName.setText(userName);

            // Set restaurant name
            String restaurantName = activity.getRestaurantName() != null ? activity.getRestaurantName() : "Unknown";
            tvRestaurantName.setText(restaurantName);

            // Set activity text and icon
            if (activity.getType() == ActivityItem.ActivityType.VOTE) {
                // Only show accurate votes
                if (activity.getVoteType() != null && activity.getVoteType()) {
                    tvActivityText.setText("voted accurate on your review");
                    ivActivityIcon.setImageResource(R.drawable.ic_thumb_up);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.modern_success));
                }
            } else if (activity.getType() == ActivityItem.ActivityType.COMMENT) {
                tvActivityText.setText("commented on your review");
                ivActivityIcon.setImageResource(R.drawable.ic_comments);
                ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.logo_primary));
            }

            // Set timestamp with better formatting
            if (activity.getTimestamp() != null) {
                tvTimestamp.setText(formatTimestamp(activity.getTimestamp()));
            } else {
                tvTimestamp.setText("");
            }
        }

        private String formatTimestamp(Date timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp.getTime();
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;

            if (months > 0) {
                return months == 1 ? "1 month ago" : months + " months ago";
            } else if (weeks > 0) {
                return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
            } else if (days > 0) {
                if (days == 1) {
                    return "yesterday";
                } else if (days < 7) {
                    return days + " days ago";
                } else {
                    return "this week";
                }
            } else if (hours > 0) {
                if (hours < 24) {
                    return hours == 1 ? "1 hour ago" : hours + " hours ago";
                } else {
                    return "today";
                }
            } else if (minutes > 0) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else {
                return "just now";
            }
        }
    }
}
