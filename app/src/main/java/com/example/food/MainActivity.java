package com.example.food;

/**
 * MainActivity - Main container activity with bottom navigation
 * 
 * This is the main entry point after login. Features:
 * - Bottom navigation bar with 5 tabs (Home, Map, Add, Profile, Settings)
 * - Fragment container to display selected tab content
 * - Icon state changes (outline/filled) based on selection
 * - System bar theming (white status bar and navigation bar)
 * - User authentication check on startup
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private TextView tvWelcome;
    private Button btnLogout;
    private FrameLayout addButtonOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure white status bar and navigation bar
        setupSystemBars();
        
        setContentView(R.layout.activity_main);

        // Initialize Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Check user authentication status
        if (mAuth.getCurrentUser() == null) {
            // Not logged in - redirect to login page
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Setup bottom navigation bar and fragment switching
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Update navigation icons to show selected state
            updateNavIcons(bottomNav, id);
            
            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_map) {
                switchFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_add) {
                switchFragment(new AddFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                switchFragment(new SettingsFragment());
                return true;
            }
            return false;
        });

        // Set default selected tab to Home
        bottomNav.setSelectedItemId(R.id.nav_home);
        updateNavIcons(bottomNav, R.id.nav_home);
        
        // Note: Auto-upload functionality removed, now loads data from Firebase only
    }
    
    /**
     * Setup system UI bars (status bar and navigation bar) with light theme
     * Sets white background with black text/icons
     */
        Window window = getWindow();
        
        // Set status bar color to white with black text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        
        // Set navigation bar color to white with black text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        }
        
        // Enable light status bar (black text/icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(
                window.getDecorView().getSystemUiVisibility() | 
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Enable light navigation bar (black text/icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.getDecorView().setSystemUiVisibility(
                window.getDecorView().getSystemUiVisibility() | 
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
    }

    /**
     * Switch the currently displayed fragment
     * @param fragment The fragment to display
     */
    private void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
    
    /**
     * Update bottom navigation icons based on selected tab
     * Changes icons from outline to filled for selected item
     * @param bottomNav The bottom navigation view
     * @param selectedId The ID of the selected menu item
     */
    private void updateNavIcons(BottomNavigationView bottomNav, int selectedId) {
        // Reset all icons to outline (unselected) versions
        bottomNav.getMenu().findItem(R.id.nav_home).setIcon(R.drawable.ic_home_outline);
        bottomNav.getMenu().findItem(R.id.nav_map).setIcon(R.drawable.ic_map_outline);
        bottomNav.getMenu().findItem(R.id.nav_add).setIcon(R.drawable.ic_add_outline);
        bottomNav.getMenu().findItem(R.id.nav_profile).setIcon(R.drawable.ic_profile_outline);
        bottomNav.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_settings_outline);
        
        // Set the selected icon to filled version based on which tab is selected
        if (selectedId == R.id.nav_home) {
            bottomNav.getMenu().findItem(R.id.nav_home).setIcon(R.drawable.ic_home_filled);
        } else if (selectedId == R.id.nav_map) {
            bottomNav.getMenu().findItem(R.id.nav_map).setIcon(R.drawable.ic_map_filled);
        } else if (selectedId == R.id.nav_add) {
            // Add button uses filled icon when selected
            bottomNav.getMenu().findItem(R.id.nav_add).setIcon(R.drawable.ic_add_filled);
        } else if (selectedId == R.id.nav_profile) {
            bottomNav.getMenu().findItem(R.id.nav_profile).setIcon(R.drawable.ic_profile_filled);
        } else if (selectedId == R.id.nav_settings) {
            bottomNav.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_settings_filled);
        }
    }
}