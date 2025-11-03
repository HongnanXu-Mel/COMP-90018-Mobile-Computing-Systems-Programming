package com.example.food.service;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.food.Config;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GooglePlacesRestaurantSyncService {

    private static final String TAG = "GooglePlacesSync";
    private static final int MAX_RESULTS = 200;
    private static final long PAGE_DELAY_MS = 2000;

    private final Context context;
    private final String apiKey;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public GooglePlacesRestaurantSyncService(@NonNull Context context) {
        this.context = context.getApplicationContext();
        Config.INSTANCE.initialize(this.context);
        this.apiKey = Config.INSTANCE.getGooglePlacesKey();
        this.firestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface SyncCallback {
        void onSuccess(@NonNull SyncSummary summary);

        void onError(@NonNull Exception exception);
    }

    public static class SyncSummary {
        private final int processedCount;
        private final int newCount;

        public SyncSummary(int processedCount, int newCount) {
            this.processedCount = processedCount;
            this.newCount = newCount;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getNewCount() {
            return newCount;
        }
    }

    public void syncRestaurants(@NonNull String regionName, @NonNull String placeType,
                                double centerLat, double centerLng, int radiusMeters,
                                @NonNull SyncCallback callback) {
        executor.execute(() -> {
            if (TextUtils.isEmpty(apiKey)) {
                postError(callback, new IllegalStateException("Missing Places API key"));
                return;
            }

            try {
                // get existing restaurants
                QuerySnapshot snapshot = Tasks.await(firestore.collection("restaurants").get());
                Set<String> existingKeys = new HashSet<>();
                for (DocumentSnapshot doc : snapshot) {
                    String key = buildRestaurantKey(doc.getString("name"), doc.getString("address"));
                    if (!key.isEmpty()) existingKeys.add(key);
                }

                // fetch from google
                Uri uri = buildNearbySearchUri(centerLat, centerLng, radiusMeters, placeType);
                List<PlacePayload> places = fetchPlaces(uri);

                int newCount = 0;
                int limit = 0;

                // add new restaurants only
                for (PlacePayload place : places) {
                    if (limit >= MAX_RESULTS) break;
                    
                    String key = buildRestaurantKey(place.name, place.address);
                    if (!key.isEmpty() && existingKeys.contains(key)) continue;

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", place.name);
                    data.put("address", place.address);
                    data.put("latitude", place.latitude);
                    data.put("longitude", place.longitude);
                    data.put("category", place.category);
                    data.put("region", regionName);

                    Tasks.await(firestore.collection("restaurants").document(place.placeId).set(data, SetOptions.merge()));
                    existingKeys.add(key);
                    newCount++;
                    limit++;
                }

                postSuccess(callback, new SyncSummary(limit, newCount));
            } catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                Log.e(TAG, "Sync failed", e);
                postError(callback, e);
            }
        });
    }

    private void postSuccess(@NonNull SyncCallback callback, @NonNull SyncSummary summary) {
        mainHandler.post(() -> callback.onSuccess(summary));
    }

    private void postError(@NonNull SyncCallback callback, @NonNull Exception exception) {
        mainHandler.post(() -> callback.onError(exception));
    }

    private Uri buildNearbySearchUri(double lat, double lng, int radiusMeters, @NonNull String placeType) {
        return Uri.parse("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .buildUpon()
                .appendQueryParameter("location", String.format(Locale.US, "%f,%f", lat, lng))
                .appendQueryParameter("radius", String.valueOf(radiusMeters))
                .appendQueryParameter("type", placeType)
                .appendQueryParameter("key", apiKey)
                .build();
    }

    private List<PlacePayload> fetchPlaces(@NonNull Uri uri) throws IOException, JSONException, InterruptedException {
        List<PlacePayload> places = new ArrayList<>();
        String nextToken = null;
        int page = 0;

        while (page < 10 && places.size() < MAX_RESULTS) {
            Uri.Builder builder = uri.buildUpon();
            if (!TextUtils.isEmpty(nextToken)) builder.appendQueryParameter("pagetoken", nextToken);
            
            JSONObject response = makeRequest(builder.build().toString());
            JSONArray results = response.optJSONArray("results");
            
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    PlacePayload place = parsePlace(results.getJSONObject(i));
                    if (place != null) places.add(place);
                }
            }

            nextToken = response.optString("next_page_token", "");
            if (TextUtils.isEmpty(nextToken)) break;
            
            page++;
            Thread.sleep(PAGE_DELAY_MS);
        }

        return places;
    }

    private JSONObject makeRequest(@NonNull String url) throws IOException, JSONException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            int code = conn.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            String body = readStream(stream);
            
            if (code < 200 || code >= 300) throw new IOException("API error: " + code);

            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "");
            if (!status.isEmpty() && !status.equals("OK") && !status.equals("ZERO_RESULTS")) {
                throw new IOException("API status: " + status);
            }
            return json;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private PlacePayload parsePlace(@NonNull JSONObject json) {
        String placeId = json.optString("place_id", "");
        String name = json.optString("name", "");
        String address = json.optString("formatted_address", json.optString("vicinity", ""));
        
        JSONObject geometry = json.optJSONObject("geometry");
        if (TextUtils.isEmpty(placeId) || TextUtils.isEmpty(name) || geometry == null) return null;
        
        JSONObject location = geometry.optJSONObject("location");
        if (location == null) return null;
        
        double lat = location.optDouble("lat", Double.NaN);
        double lng = location.optDouble("lng", Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng)) return null;
        
        String category = getCategory(json.optJSONArray("types"));
        return new PlacePayload(placeId, name, address, lat, lng, category);
    }

    private String getCategory(JSONArray types) {
        if (types == null) return "Restaurant";
        
        for (int i = 0; i < types.length(); i++) {
            String type = types.optString(i, "");
            if (type.isEmpty() || type.equals("point_of_interest") || type.equals("establishment") 
                || type.equals("food") || type.equals("restaurant")) continue;
            
            return capitalize(type.replace('_', ' '));
        }
        return "Restaurant";
    }

    private String capitalize(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(word.substring(0, 1).toUpperCase(Locale.US));
            if (word.length() > 1) result.append(word.substring(1));
        }
        return result.length() == 0 ? "Restaurant" : result.toString();
    }

    private String buildRestaurantKey(String name, String address) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return "";
        return (name.trim() + "|" + address.trim()).toLowerCase(Locale.US);
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        reader.close();
        return result.toString();
    }

    private static class PlacePayload {
        final String placeId;
        final String name;
        final String address;
        final double latitude;
        final double longitude;
        final String category;

        PlacePayload(String placeId, String name, String address, double latitude, double longitude, String category) {
            this.placeId = placeId;
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category;
        }
    }
}
