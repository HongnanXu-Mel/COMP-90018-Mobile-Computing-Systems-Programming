package com.example.food.adapters;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
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
import com.example.food.model.Restaurant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GroupedActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ACTIVITY = 1;
    
    private List<Object> items; 
    private Map<String, Review> reviewMap;
    private Map<String, Restaurant> restaurantMap;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(ActivityItem activity, Review review, Restaurant restaurant);
    }

    public GroupedActivityAdapter(List<ActivityItem> activities, OnActivityClickListener listener) {
        this.listener = listener;
        this.items = new ArrayList<>();
        groupActivities(activities);
    }

    private void groupActivities(List<ActivityItem> activities) {
        items.clear();
        
        if (activities == null || activities.isEmpty()) {
            return;
        }
        
        // Group activities by date
        String currentGroup = null;
        for (ActivityItem activity : activities) {
            String group = getDateGroup(activity.getTimestamp());
            if (!group.equals(currentGroup)) {
                items.add(group);
                currentGroup = group;
            }
            items.add(activity);
        }
    }

    private String getDateGroup(Date timestamp) {
        if (timestamp == null) return "Unknown";
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp.getTime();
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return "This Week";
        } else if (days < 30) {
            return "This Month";
        } else {
            return "Older";
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        return item instanceof String ? VIEW_TYPE_HEADER : VIEW_TYPE_ACTIVITY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity, parent, false);
            return new ActivityViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String header = (String) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ActivityViewHolder) {
            ActivityItem activity = (ActivityItem) items.get(position);
            ((ActivityViewHolder) holder).bind(activity);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setActivities(List<ActivityItem> activities) {
        groupActivities(activities);
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

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }

        public void bind(String header) {
            tvHeader.setText(header);
        }
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private de.hdodenhof.circleimageview.CircleImageView ivUserAvatar;
        private TextView tvUserName;
        private TextView tvActivityText;
        private TextView tvRestaurantName;
        private TextView tvTimestamp;
        private ImageView ivActivityIcon;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvActivityText = itemView.findViewById(R.id.tvActivityText);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivActivityIcon = itemView.findViewById(R.id.ivActivityIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Object item = items.get(position);
                    if (item instanceof ActivityItem) {
                        ActivityItem activity = (ActivityItem) item;
                        Review review = reviewMap != null ? reviewMap.get(activity.getReviewId()) : null;
                        Restaurant restaurant = restaurantMap != null ? restaurantMap.get(activity.getReviewId()) : null;
                        listener.onActivityClick(activity, review, restaurant);
                    }
                }
            });
        }

        public void bind(ActivityItem activity) {
            // Set user avatar. same implementation as profile page
            if (activity.getUserAvatarUrl() != null && !activity.getUserAvatarUrl().trim().isEmpty()) {
                ivUserAvatar.setBorderWidth(2);
                ivUserAvatar.setBorderColor(itemView.getContext().getColor(R.color.logo_primary));
                Glide.with(itemView.getContext())
                        .load(activity.getUserAvatarUrl())
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .centerCrop()
                        .override(96, 96)
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setBorderWidth(0);
                ivUserAvatar.setImageResource(R.drawable.ic_person);
            }

            // Set user name
            String userName = activity.getUserName() != null ? activity.getUserName() : "User";
            tvUserName.setText(userName);

            // Set restaurant name
            String restaurantName = activity.getRestaurantName() != null ? activity.getRestaurantName() : "Restaurant";
            tvRestaurantName.setText(restaurantName);

            // Set activity text with colored action words
            if (activity.getType() == ActivityItem.ActivityType.VOTE) {
                // Only show accurate votes
                if (activity.getVoteType() != null && activity.getVoteType()) {
                    String fullText = "voted accurate on your review";
                    SpannableString spannableString = new SpannableString(fullText);
                    spannableString.setSpan(new ForegroundColorSpan(itemView.getContext().getColor(R.color.modern_success)), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvActivityText.setText(spannableString);
                }
            } else if (activity.getType() == ActivityItem.ActivityType.COMMENT) {
                String fullText = "commented on your review";
                SpannableString spannableString = new SpannableString(fullText);
                spannableString.setSpan(new ForegroundColorSpan(itemView.getContext().getColor(R.color.logo_primary)), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvActivityText.setText(spannableString);
            }

            // Set review image instead of icon
            if (activity.getReviewFirstImageUrl() != null && !activity.getReviewFirstImageUrl().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(activity.getReviewFirstImageUrl())
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .centerCrop()
                        .override(60, 60)
                        .into(ivActivityIcon);
            } else {
                // Fallback to a placeholder if no image is available
                ivActivityIcon.setImageResource(R.drawable.ic_restaurant);
                ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.profile_text_tertiary));
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
                return months + "m";
            } else if (weeks > 0) {
                return weeks + "w";
            } else if (days > 0) {
                return days + "d";
            } else if (hours > 0) {
                return hours + "h";
            } else if (minutes > 0) {
                return minutes + "m";
            } else {
                return "now";
            }
        }
    }
}



