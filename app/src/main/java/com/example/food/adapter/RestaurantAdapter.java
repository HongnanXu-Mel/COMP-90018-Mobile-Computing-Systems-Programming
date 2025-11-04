package com.example.food.adapter;

/**
 * RestaurantAdapter - RecyclerView adapter for displaying restaurant list
 * 
 * This adapter:
 * - Displays restaurant name, address, and category
 * - Handles click events on restaurant items
 * - Used in restaurant selection screens
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    
    // Data and listener
    private List<Restaurant> restaurants; // List of restaurants to display
    private OnRestaurantClickListener listener; // Click event listener

    /**
     * Interface for handling restaurant item clicks
     */
    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    /**
     * Constructor - initializes with empty list
     */
    public RestaurantAdapter() {
        this.restaurants = new ArrayList<>();
    }

    /**
     * Set click listener for restaurant items
     */
    public void setOnRestaurantClickListener(OnRestaurantClickListener listener) {
        this.listener = listener;
    }

    /**
     * Update restaurant list and refresh view
     */
    public void updateRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.bind(restaurant);
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    /**
     * ViewHolder for restaurant items
     */
    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        // UI components
        private TextView tvRestaurantName; // Restaurant name
        private TextView tvRestaurantAddress; // Restaurant address
        private TextView tvRestaurantCategory; // Restaurant cuisine category

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            tvRestaurantAddress = itemView.findViewById(R.id.tv_restaurant_address);
            tvRestaurantCategory = itemView.findViewById(R.id.tv_restaurant_category);

            // Setup click listener for entire item
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRestaurantClick(restaurants.get(getAdapterPosition()));
                }
            });
        }

        /**
         * Bind restaurant data to UI components
         */
        public void bind(Restaurant restaurant) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantAddress.setText(restaurant.getAddress());
            tvRestaurantCategory.setText(restaurant.getCategory());
        }
    }
}

