package com.example.food;

/**
 * MapFragment - Google Maps view displaying restaurant locations
 * 
 * Features:
 * - Display restaurants as markers on Google Maps
 * - Request location permissions and show user's current location
 * - Zoom in/out controls
 * - Click markers to view restaurant details and reviews in a bottom sheet
 * - Load restaurant data from Firebase Firestore
 * - Center map on Melbourne CBD by default
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.adapters.ReviewWidgetAdapter;
import com.example.food.data.Review;
import com.example.food.dialogs.ReviewDetailsDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
// FragmentManager和FragmentTransaction不再需要，已简化地图初始化

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.food.model.Restaurant;
// FirebaseDataUploader已删除，不再需要上传功能

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    // Tag for logging
    private static final String TAG = "MapFragment";
    
    // Google Maps components
    private GoogleMap googleMap; // Google Map instance
    private FusedLocationProviderClient fusedLocationClient; // Location services
    private PlacesClient placesClient; // Google Places API client
    
    // Firebase
    private FirebaseFirestore db; // Firestore database instance
    
    // UI Controls
    private ImageButton btnZoomIn; // Zoom in button
    private ImageButton btnZoomOut; // Zoom out button

    // Data
    private List<Restaurant> highRatedRestaurants; // List of restaurants to display

    // Permission launcher for location access
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (fine || coarse) {
                    // Permission granted - enable location and load data
                    enableMyLocationAndLoad();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Create fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Initialize views and setup map after view is created
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize zoom control buttons
        btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        
        // Setup button click listeners for zoom controls
        btnZoomIn.setOnClickListener(v -> zoomIn());
        btnZoomOut.setOnClickListener(v -> zoomOut());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());
        
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Setup Google Map - simplified initialization
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    /**
     * Callback when Google Map is ready to use
     * Configure map settings and enable location
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        // Configure map UI settings
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Setup marker click listener to show restaurant details
        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = (Restaurant) marker.getTag();
            if (restaurant != null) {
                showRestaurantPostsBottomSheet(restaurant);
            }
            return true;
        });
        
        enableMyLocationAndLoad();
    }

    /**
     * Enable user's current location on map if permission granted
     * Otherwise request location permission
     */
    private void enableMyLocationAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                try { googleMap.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
            }
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
        
        // 无论是否有位置权限，都显示地图和餐厅标记
        moveToMelbourne();
        addRestaurantMarkers();
    }
    
    // 上传功能已删除，现在只从Firebase加载数据

    private void moveToMelbourne() {
        // 墨尔本市中心坐标（更精确的坐标）
    private void moveToMelbourne() {
        // Melbourne city center coordinates (more precise)
        LatLng melbourne = new LatLng(-37.810272, 144.962646);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 10f));
    }
    
    /**
     * Load restaurant data from Firebase and add markers to the map
     */
    private void addRestaurantMarkers() {
        if (googleMap == null) {
            return;
        }
        
        // Show loading message to user
        Toast.makeText(requireContext(), "正在从Firebase加载餐厅数据...", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "开始从Firebase加载餐厅数据...");
        
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firebase连接成功，文档数量: " + queryDocumentSnapshots.size());
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No data in Firebase - show message
                        Log.d(TAG, "Firebase中无餐厅数据");
                        Toast.makeText(requireContext(), "Firebase中暂无餐厅数据，请等待数据上传完成", Toast.LENGTH_LONG).show();
                    } else {
                        // Data exists - load and display on map
                        List<Restaurant> restaurants = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Parse restaurant object from Firestore document
                                Restaurant restaurant = document.toObject(Restaurant.class);
                                restaurant.setId(document.getId());
                                restaurants.add(restaurant);
                                
                                // Add map marker for this restaurant
                                LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(restaurant.getName())
                                        .snippet(restaurant.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                
                                // Attach restaurant data to marker for later use
                                Marker marker = googleMap.addMarker(markerOptions);
                                if (marker != null) {
                                    marker.setTag(restaurant);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "解析餐厅数据失败: " + document.getId(), e);
                            }
                        }
                        
                        Log.d(TAG, "成功加载 " + restaurants.size() + " 家餐厅");
                        Toast.makeText(requireContext(), "已从Firebase加载 " + restaurants.size() + " 家好评餐厅", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase加载失败", e);
                    Toast.makeText(requireContext(), "Firebase连接失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Display bottom sheet dialog showing posts for a specific restaurant
     */
    private void showRestaurantPostsBottomSheet(Restaurant restaurant) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_restaurant_posts, null);
        bottomSheet.setContentView(view);

        // Initialize views
        TextView tvRestaurantName = view.findViewById(R.id.tv_restaurant_name);
        TextView tvRestaurantAddress = view.findViewById(R.id.tv_restaurant_address);
        TextView tvPostsCount = view.findViewById(R.id.tv_posts_count);
        RecyclerView rvPosts = view.findViewById(R.id.rv_posts);
        TextView tvNoPosts = view.findViewById(R.id.tv_no_posts);

        // Set restaurant info
        tvRestaurantName.setText(restaurant.getName());
        tvRestaurantAddress.setText(restaurant.getAddress());

        // Setup RecyclerView
        List<Review> reviews = new ArrayList<>();
        ReviewWidgetAdapter adapter = new ReviewWidgetAdapter(reviews, (review, restaurantData) -> {
            // Open review details dialog
            ReviewDetailsDialog dialog = new ReviewDetailsDialog(requireContext(), review, restaurantData);
            dialog.show();
            bottomSheet.dismiss();
        });
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPosts.setAdapter(adapter);

        // Load reviews for this restaurant
        loadRestaurantReviews(restaurant.getId(), adapter, rvPosts, tvNoPosts, tvPostsCount);

        bottomSheet.show();
    }

    /**
     * Load reviews from Firestore for a specific restaurant
     */
    private void loadRestaurantReviews(String restaurantId, ReviewWidgetAdapter adapter, 
                                     RecyclerView rvPosts, TextView tvNoPosts, TextView tvPostsCount) {
        db.collection("reviews")
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Review> reviews = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Review review = document.toObject(Review.class);
                    review.setId(document.getId());
                    reviews.add(review);
                }

                // Sort by timestamp (newest first)
                reviews.sort((r1, r2) -> {
                    if (r1.getCreatedAt() == null) return 1;
                    if (r2.getCreatedAt() == null) return -1;
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                });

                adapter.setReviews(reviews);
                tvPostsCount.setText(String.valueOf(reviews.size()));

                if (reviews.isEmpty()) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                    rvPosts.setVisibility(View.GONE);
                } else {
                    tvNoPosts.setVisibility(View.GONE);
                    rvPosts.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading restaurant reviews", e);
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Zoom in the map camera
     */
    private void zoomIn() {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }
    
    /**
     * Zoom out the map camera
     */
    private void zoomOut() {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }
    
    // 上传功能已删除，现在只从Firebase加载数据
}