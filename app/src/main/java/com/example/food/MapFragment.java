package com.example.food;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingViewportTask;
    private boolean isFetching = false;

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

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("map");
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction tx = fm.beginTransaction();
            tx.replace(R.id.map_container, mapFragment, "map");
            tx.commitNowAllowingStateLoss();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // 当地图停止移动时，刷新当前视图范围内的餐厅
        googleMap.setOnCameraIdleListener(() -> {
            if (pendingViewportTask != null) {
                handler.removeCallbacks(pendingViewportTask);
            }
            pendingViewportTask = this::refreshViewportRestaurants;
            handler.postDelayed(pendingViewportTask, 600); // 防抖
        });
        enableMyLocationAndLoad();
    }

    private void enableMyLocationAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                try { googleMap.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
            }
            requestCurrentLocation();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void requestCurrentLocation() {
        // Prefer a fresh high-accuracy fix
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        onLocation(loc);
                    } else {
                        // Fallback to last known
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(last -> {
                                    if (last != null) {
                                        if (isLikelyEmulatorDefault(last)) {
                                            // Try one-shot updates to get a real fix
                                            requestSingleUpdate();
                                        } else {
                                            onLocation(last);
                                        }
                                    } else {
                                        requestSingleUpdate();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // 位置获取失败，静默处理
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // 位置获取失败，静默处理
                });
    }

    private void requestSingleUpdate() {
        LocationRequest req = new LocationRequest.Builder(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(true)
                .setMaxUpdates(1)
                .build();
        fusedLocationClient.requestLocationUpdates(req, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    onLocation(loc);
                } else {
                    // As a user-friendly fallback for Melbourne users
                    LatLng mel = new LatLng(-37.8136, 144.9631);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mel, 13f));
                }
            }
        }, requireActivity().getMainLooper());
    }

    private boolean isLikelyEmulatorDefault(@NonNull Location loc) {
        // Android 模拟器默认坐标（Googleplex）
        double dLat = Math.abs(loc.getLatitude() - 37.4219999);
        double dLng = Math.abs(loc.getLongitude() + 122.0840575);
        return dLat < 0.01 && dLng < 0.01; // ~1km 以内视为默认点
    }

    private void onLocation(@NonNull Location location) {
        LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 15f));
        // 触发一次根据视图范围的加载
        refreshViewportRestaurants();
    }

    private void refreshViewportRestaurants() {
        if (googleMap == null || isFetching) return;
        Projection projection = googleMap.getProjection();
        if (projection == null) return;
        com.google.android.gms.maps.model.VisibleRegion vr = projection.getVisibleRegion();
        LatLng center = new LatLng(
                (vr.latLngBounds.northeast.latitude + vr.latLngBounds.southwest.latitude) / 2.0,
                (vr.latLngBounds.northeast.longitude + vr.latLngBounds.southwest.longitude) / 2.0
        );
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                vr.latLngBounds.northeast.latitude, vr.latLngBounds.northeast.longitude, result);
        int radius = (int) Math.min(Math.max(result[0], 1200), 5000); // 1.2km - 5km
        fetchNearbyRestaurants(center, radius);
    }

    private void fetchNearbyRestaurants(@NonNull LatLng center, int radiusMeters) {
        isFetching = true;
        requireActivity().runOnUiThread(() -> googleMap.clear());
        new Thread(() -> {
            try {
                int webKeyId = getResources().getIdentifier("google_places_key", "string", requireContext().getPackageName());
                String apiKey = webKeyId != 0 ? getString(webKeyId) : getString(R.string.google_maps_key);
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + center.latitude + "," + center.longitude
                        + "&radius=" + radiusMeters
                        + "&type=restaurant"
                        + "&key=" + apiKey;

                String nextPageToken = null;
                int pagesFetched = 0;
                do {
                    String pageUrl = nextPageToken == null ? url : (url + "&pagetoken=" + nextPageToken);
                    HttpURLConnection conn = (HttpURLConnection) new URL(pageUrl).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    conn.disconnect();

                    JSONObject root = new JSONObject(sb.toString());
                    String status = root.optString("status", "");
                    if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                        // API调用失败，静默处理
                        final String err = root.optString("error_message", status);
                    }
                    JSONArray results = root.optJSONArray("results");
                    nextPageToken = root.optString("next_page_token", null);
                    if (results != null) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject r = results.getJSONObject(i);
                            JSONObject geom = r.getJSONObject("geometry").getJSONObject("location");
                            double lat = geom.getDouble("lat");
                            double lng = geom.getDouble("lng");
                            String name = r.optString("name");
                            String vicinity = r.optString("vicinity");
                            LatLng pos = new LatLng(lat, lng);
                            requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(name)
                                    .snippet(vicinity)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                        }
                    }

                    pagesFetched++;
                    if (nextPageToken != null && pagesFetched < 2) {
                        // next_page_token 需要短暂等待才可用
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    } else {
                        nextPageToken = null;
                    }
                } while (nextPageToken != null);
            } catch (Exception e) {
                // 搜索失败，静默处理
            } finally {
                isFetching = false;
            }
        }).start();
    }
}
