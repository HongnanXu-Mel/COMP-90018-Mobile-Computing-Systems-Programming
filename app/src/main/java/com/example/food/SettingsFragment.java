package com.example.food;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.food.data.UserProfile;
import com.example.food.cache.ProfileCacheManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private ListenerRegistration profileListener;
    private ExecutorService executorService;
    private ProfileCacheManager cacheManager;
    
    private TextInputEditText etName, etBio;
    private TextInputLayout nameInputLayout, bioInputLayout;
    private TextView tvDisplayName, tvEmail, tvEmailDisplay, tvNameError;
    private CircleImageView ivProfilePicture;
    private androidx.appcompat.widget.AppCompatButton btnUpdateProfile;
    private com.google.android.material.button.MaterialButton btnChangeProfilePicture;
    private android.widget.LinearLayout btnSecurity, btnPrivacyPolicy, btnContactUs, btnLogout;
    
    private UserProfile userProfile;
    private boolean isUpdating = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        initializeFirebase();
        initializeViews(view);
        setupClickListeners();
        
        if (currentUser != null) {
            loadUserData();
        } else {
            showToast(getString(R.string.please_log_in_to_view_settings));
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileListener != null) {
            profileListener.remove();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
        cacheManager = ProfileCacheManager.getInstance(requireContext());
    }

    private void initializeViews(View view) {
        try {
            etName = view.findViewById(R.id.etName);
            etBio = view.findViewById(R.id.etBio);
            nameInputLayout = view.findViewById(R.id.nameInputLayout);
            bioInputLayout = view.findViewById(R.id.bioInputLayout);
            tvDisplayName = view.findViewById(R.id.tvDisplayName);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvEmailDisplay = view.findViewById(R.id.tvEmailDisplay);
            tvNameError = view.findViewById(R.id.tvNameError);
            ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
            btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
            btnChangeProfilePicture = view.findViewById(R.id.btnChangeProfilePicture);
            btnSecurity = view.findViewById(R.id.btnSecurity);
            btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
            btnContactUs = view.findViewById(R.id.btnContactUs);
            btnLogout = view.findViewById(R.id.btnLogout);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            showToast("Error initializing settings");
        }
    }

    private void setupClickListeners() {
        try {
            if (btnUpdateProfile != null) {
                btnUpdateProfile.setOnClickListener(v -> updateProfile());
            }
            if (btnChangeProfilePicture != null) {
                btnChangeProfilePicture.setOnClickListener(v -> showChangeProfilePictureDialog());
            }
            if (btnSecurity != null) {
                btnSecurity.setOnClickListener(v -> showChangePasswordDialog());
            }
            if (btnPrivacyPolicy != null) {
                btnPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
            }
            if (btnContactUs != null) {
                btnContactUs.setOnClickListener(v -> showContactUsDialog());
            }
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> showLogoutConfirmation());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            showToast("Error setting up settings");
        }
    }

    private void loadUserData() {
        if (currentUser == null) return;

        // Load cached data immediately for instant UI update
        loadCachedData();
        
        // Set email immediately (always available from Firebase Auth)
        tvEmail.setText(currentUser.getEmail());
        tvEmailDisplay.setText(currentUser.getEmail());
        loadProfilePicture();

        // Load fresh data from Firebase in background
        executorService.execute(() -> {
            profileListener = db.collection("users").document(currentUser.getUid())
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Listen failed.", e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            userProfile = documentSnapshot.toObject(UserProfile.class);
                            if (userProfile != null) {
                                // Cache the fresh data
                                cacheManager.cacheUserProfile(userProfile);
                                // Update UI on main thread
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(this::updateUI);
                                }
                            }
                        } else {
                            createUserProfile();
                        }
                    });
        });
    }
    
    private void loadCachedData() {
        UserProfile cachedProfile = cacheManager.getCachedUserProfile();
        if (cachedProfile != null) {
            userProfile = cachedProfile;
            updateUI();
        }
    }

    private void loadProfilePicture() {
        if (userProfile == null) {
            ivProfilePicture.setBorderWidth(0);
            ivProfilePicture.setImageResource(R.drawable.ic_person);
            return;
        }

        String avatarUrl = userProfile.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            // show border when image exists
            ivProfilePicture.setBorderWidth(3);
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .centerCrop()
                    .override(96, 96)
                    .into(ivProfilePicture);
        } else {
            // no border for placeholder
            ivProfilePicture.setBorderWidth(0);
            ivProfilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    private void createUserProfile() {
        if (currentUser == null) return;

        String name = currentUser.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            name = currentUser.getEmail().split("@")[0]; // Use email prefix as fallback
        }

        userProfile = new UserProfile(
                currentUser.getUid(),
                name,
                name, // Use same name for displayName field for compatibility
                currentUser.getEmail(),
                "" // Empty bio by default
        );
        
        // Update UI immediately with the new profile
        updateUI();

        db.collection("users").document(currentUser.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created successfully");
                    // Cache the new profile
                    cacheManager.cacheUserProfile(userProfile);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user profile", e);
                    showToast(getString(R.string.error_creating_profile));
                });
    }

    private void updateUI() {
        if (userProfile == null) return;

        // Set name (use name field from Firebase)
        String name = userProfile.getName();
        if (name == null || name.trim().isEmpty()) {
            name = "Food Explorer";
        }
        
        tvDisplayName.setText(name);
        etName.setText(name);
        
        // Set bio
        etBio.setText(userProfile.getBio() != null ? userProfile.getBio() : "");
        
        loadProfilePicture();
        clearErrors();
    }

    private void updateProfile() {
        if (currentUser == null || isUpdating) return;

        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String bio = Objects.requireNonNull(etBio.getText()).toString().trim();

        if (!validateInputs(name, bio)) {
            return;
        }

        if (userProfile != null && 
            name.equals(userProfile.getName()) && 
            bio.equals(userProfile.getBio())) {
            showToast(getString(R.string.no_changes_to_save));
            return;
        }

        setUpdatingState(true);

        DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("updatedAt", System.currentTimeMillis());

        userDoc.update(updates)
                .addOnCompleteListener(task -> {
                    setUpdatingState(false);
                    
                    if (task.isSuccessful()) {
                        // Update local profile and cache
                        if (userProfile != null) {
                            userProfile.setName(name);
                            userProfile.setBio(bio);
                            cacheManager.cacheUserProfile(userProfile);
                        }
                        showToast(getString(R.string.profile_updated_successfully));
                        clearErrors();
                    } else {
                        String errorMessage = getString(R.string.failed_to_update_profile);
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        showToast(errorMessage);
                        Log.e(TAG, "Error updating profile", task.getException());
                    }
                });
    }

    private boolean validateInputs(String name, String bio) {
        boolean isValid = true;

        // Validate name
        if (name.isEmpty()) {
            showNameError("Name should not be empty");
            isValid = false;
        } else if (name.length() < 2) {
            showNameError("Name must be at least 2 characters");
            isValid = false;
        } else if (name.length() > 30) {
            showNameError("Name must be less than 30 characters");
            isValid = false;
        } else {
            clearNameError();
        }

        return isValid;
    }
    
    private void showNameError(String message) {
        // Show error message below the name field
        if (tvNameError != null) {
            tvNameError.setText(message);
            tvNameError.setVisibility(View.VISIBLE);
        }
    }
    
    private void clearNameError() {
        nameInputLayout.setError(null);
        if (tvNameError != null) {
            tvNameError.setVisibility(View.GONE);
        }
    }

    private void clearErrors() {
        nameInputLayout.setError(null);
        bioInputLayout.setError(null);
        if (tvNameError != null) {
            tvNameError.setVisibility(View.GONE);
        }
    }

    private void setUpdatingState(boolean updating) {
        isUpdating = updating;
        btnUpdateProfile.setEnabled(!updating);
        btnUpdateProfile.setText(updating ? getString(R.string.changing) : getString(R.string.save_changes));
    }

    private void showLogoutConfirmation() {
        LogoutConfirmationDialog dialog = new LogoutConfirmationDialog(requireContext(), this::logout);
        dialog.show();
    }

    private void logout() {
        if (currentUser == null) return;

        mAuth.signOut();
        showToast(getString(R.string.logged_out_successfully));
        
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    
    private void showChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(getContext());
        dialog.show();
    }
    
    private void showChangeProfilePictureDialog() {
        ChangeProfilePictureDialogFragment dialog = new ChangeProfilePictureDialogFragment();
        dialog.setOnProfilePictureChangedListener(() -> {
            // refresh the profile picture when dialog closes
            loadProfilePicture();
        });
        dialog.show(getParentFragmentManager(), "ChangeProfilePictureDialog");
    }
    
    private void showPrivacyPolicyDialog() {
        PrivacyPolicyDialog dialog = new PrivacyPolicyDialog(getContext());
        dialog.show();
    }
    
    private void showContactUsDialog() {
        ContactUsDialog dialog = new ContactUsDialog(getContext());
        dialog.show();
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
