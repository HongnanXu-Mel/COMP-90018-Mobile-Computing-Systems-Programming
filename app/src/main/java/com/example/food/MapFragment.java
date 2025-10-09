package com.example.food;

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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.adapter.SimplePostAdapter;
import com.example.food.model.Post;
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

    private static final String TAG = "MapFragment";
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private FirebaseFirestore db;

    // 高分餐厅数据
    private List<Restaurant> highRatedRestaurants;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (fine || coarse) {
                    enableMyLocationAndLoad();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());
        
        // 初始化Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // 简化地图初始化
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // 设置标记点击监听器
        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = (Restaurant) marker.getTag();
            if (restaurant != null) {
                showRestaurantPostsBottomSheet(restaurant);
            }
            return true;
        });
        
        enableMyLocationAndLoad();
    }

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
        LatLng melbourne = new LatLng(-37.810272, 144.962646);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 10f));
    }
    
    // 从Firebase加载餐厅数据并添加标记
    private void addRestaurantMarkers() {
        if (googleMap == null) {
            return;
        }
        
        // 显示加载提示
        Toast.makeText(requireContext(), "正在从Firebase加载餐厅数据...", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "开始从Firebase加载餐厅数据...");
        
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firebase连接成功，文档数量: " + queryDocumentSnapshots.size());
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Firebase中没有数据
                        Log.d(TAG, "Firebase中无餐厅数据");
                        Toast.makeText(requireContext(), "Firebase中暂无餐厅数据，请等待数据上传完成", Toast.LENGTH_LONG).show();
                    } else {
                        // Firebase中有数据，加载并显示
                        List<Restaurant> restaurants = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Restaurant restaurant = document.toObject(Restaurant.class);
                                restaurant.setId(document.getId());
                                restaurants.add(restaurant);
                                
                                // 添加地图标记
                                LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(restaurant.getName())
                                        .snippet(restaurant.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                
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
     * Show bottom sheet with posts related to the restaurant
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
        SimplePostAdapter adapter = new SimplePostAdapter(requireContext());
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPosts.setAdapter(adapter);

        // Load posts for this restaurant
        loadRestaurantPosts(restaurant.getId(), adapter, rvPosts, tvNoPosts, tvPostsCount);

        // Handle post click
        adapter.setOnPostClickListener(post -> {
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
            startActivity(intent);
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    /**
     * Load posts for a specific restaurant
     */
    private void loadRestaurantPosts(String restaurantId, SimplePostAdapter adapter, 
                                     RecyclerView rvPosts, TextView tvNoPosts, TextView tvPostsCount) {
        db.collection("posts")
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Post> posts = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Post post = document.toObject(Post.class);
                    post.setPostId(document.getId());
                    posts.add(post);
                }

                // Sort by timestamp (newest first)
                posts.sort((p1, p2) -> {
                    if (p1.getTimestamp() == null) return 1;
                    if (p2.getTimestamp() == null) return -1;
                    return p2.getTimestamp().compareTo(p1.getTimestamp());
                });

                adapter.updatePosts(posts);
                tvPostsCount.setText(String.valueOf(posts.size()));

                if (posts.isEmpty()) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                    rvPosts.setVisibility(View.GONE);
                } else {
                    tvNoPosts.setVisibility(View.GONE);
                    rvPosts.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading restaurant posts", e);
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            });
    }
    
    // 上传功能已删除，现在只从Firebase加载数据
}