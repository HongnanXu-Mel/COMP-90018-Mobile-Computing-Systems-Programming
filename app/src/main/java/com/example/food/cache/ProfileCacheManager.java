package com.example.food.cache;

/**
 * ProfileCacheManager - Singleton manager for caching user profile data
 * 
 * This class:
 * - Caches user profile data in SharedPreferences
 * - Implements cache expiry (5 minutes)
 * - Uses Gson for JSON serialization/deserialization
 * - Provides singleton instance for app-wide access
 * - Improves performance by reducing Firestore reads
 */

import android.content.Context;
import android.content.SharedPreferences;
import com.example.food.data.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProfileCacheManager {
    // Constants for cache configuration
    private static final String PREFS_NAME = "profile_cache"; // SharedPreferences name
    private static final String KEY_USER_PROFILE = "user_profile"; // Key for profile data
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp"; // Key for cache time
    private static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes expiry

    // Singleton instance
    private static ProfileCacheManager instance;
    
    // Dependencies
    private SharedPreferences prefs; // SharedPreferences for storage
    private Gson gson; // Gson for JSON conversion

    /**
     * Private constructor for singleton pattern
     */
    private ProfileCacheManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Get singleton instance (thread-safe)
     */
    public static synchronized ProfileCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileCacheManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save user profile to cache with current timestamp
     */
    public void cacheUserProfile(UserProfile userProfile) {
        if (userProfile == null) return;

        String profileJson = gson.toJson(userProfile);
        prefs.edit()
                .putString(KEY_USER_PROFILE, profileJson)
                .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    /**
     * Retrieve cached user profile if not expired
     * @return Cached profile or null if expired/not found
     */
    public UserProfile getCachedUserProfile() {
        long cacheTime = prefs.getLong(KEY_CACHE_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        // Check if cache has expired (older than 5 minutes)
        if (currentTime - cacheTime > CACHE_EXPIRY_TIME) {
            clearCache();
            return null;
        }

        String profileJson = prefs.getString(KEY_USER_PROFILE, null);
        if (profileJson == null) {
            return null;
        }

        try {
            // Parse JSON string to UserProfile object
            return gson.fromJson(profileJson, UserProfile.class);
        } catch (Exception e) {
            // If parsing fails, clear corrupt cache
            clearCache();
            return null;
        }
    }

    /**
     * Clear all cached data
     */
    public void clearCache() {
        prefs.edit()
                .remove(KEY_USER_PROFILE)
                .remove(KEY_CACHE_TIMESTAMP)
                .apply();
    }

    /**
     * Check if valid cached profile exists
     * @return true if cache exists and not expired
     */
    public boolean hasCachedProfile() {
        return prefs.contains(KEY_USER_PROFILE) && 
               (System.currentTimeMillis() - prefs.getLong(KEY_CACHE_TIMESTAMP, 0)) <= CACHE_EXPIRY_TIME;
    }
}
