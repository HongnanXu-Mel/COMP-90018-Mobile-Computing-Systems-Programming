package com.example.food.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.food.data.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProfileCacheManager {
    private static final String PREFS_NAME = "profile_cache";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    private static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes

    private static ProfileCacheManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private ProfileCacheManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized ProfileCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileCacheManager(context.getApplicationContext());
        }
        return instance;
    }

    public void cacheUserProfile(UserProfile userProfile) {
        if (userProfile == null) return;

        String profileJson = gson.toJson(userProfile);
        prefs.edit()
                .putString(KEY_USER_PROFILE, profileJson)
                .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public UserProfile getCachedUserProfile() {
        long cacheTime = prefs.getLong(KEY_CACHE_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        // Check if cache has expired
        if (currentTime - cacheTime > CACHE_EXPIRY_TIME) {
            clearCache();
            return null;
        }

        String profileJson = prefs.getString(KEY_USER_PROFILE, null);
        if (profileJson == null) {
            return null;
        }

        try {
            return gson.fromJson(profileJson, UserProfile.class);
        } catch (Exception e) {
            // If parsing fails, clear the cache
            clearCache();
            return null;
        }
    }

    public void clearCache() {
        prefs.edit()
                .remove(KEY_USER_PROFILE)
                .remove(KEY_CACHE_TIMESTAMP)
                .apply();
    }

    public boolean hasCachedProfile() {
        return prefs.contains(KEY_USER_PROFILE) && 
               (System.currentTimeMillis() - prefs.getLong(KEY_CACHE_TIMESTAMP, 0)) <= CACHE_EXPIRY_TIME;
    }
}
