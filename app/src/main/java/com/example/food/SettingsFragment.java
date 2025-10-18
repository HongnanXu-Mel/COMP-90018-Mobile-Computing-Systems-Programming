package com.example.food;

<<<<<<< HEAD
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
=======
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

<<<<<<< HEAD
import com.bumptech.glide.Glide;
import com.example.food.data.UserProfile;
import com.example.food.cache.ProfileCacheManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
=======
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
<<<<<<< HEAD
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
=======

import java.util.Objects;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etUsername, etEmail;
    private Button btnUpdateProfile;
    private LinearLayout btnPrivacyPolicy, btnContactUs, btnSecurity, btnLogout;
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
<<<<<<< HEAD
        initializeFirebase();
        initializeViews(view);
        setupClickListeners();
        
        if (currentUser != null) {
            loadUserData();
        } else {
            showToast(getString(R.string.please_log_in_to_view_settings));
        }
=======
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadUserData();
        setupClickListeners();
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e

        return view;
    }

<<<<<<< HEAD
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
=======
    private void initViews(View view) {
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnContactUs = view.findViewById(R.id.btnContactUs);
        btnSecurity = view.findViewById(R.id.btnSecurity);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            etEmail.setText(currentUser.getEmail());
            
            // Load username from Firestore
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String username = task.getResult().getString("name");
                    if (username != null) {
                        etUsername.setText(username);
                    }
                }
            });
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e
        }
    }

    private void setupClickListeners() {
<<<<<<< HEAD
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
=======
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
        btnContactUs.setOnClickListener(v -> showContactUsDialog());
        btnSecurity.setOnClickListener(v -> showSecurityDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateProfile() {
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();

        if (TextUtils.isEmpty(username)) {
            showUsernameError("You must enter a full name to save changes");
            etUsername.requestFocus();
            return;
        }

        // Clear any existing error
        clearUsernameError();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Update username in Firestore
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            userDoc.update("name", username)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.privacy_policy_title))
                .setMessage("**Data Collection**\n" +
                        "We collect your email address and name for account management purposes. Your restaurant preferences are stored to provide personalized recommendations.\n\n" +
                        "**Data Protection**\n" +
                        "Your personal information is encrypted and securely stored. We do not sell, trade, or share your data with third parties.\n\n" +
                        "**Student Development**\n" +
                        "This application is developed by students as part of an academic project. While we strive for professional standards, please understand this is a learning exercise.\n\n" +
                        "**Contact**\n" +
                        "For privacy concerns or data requests, contact us at: " + getString(R.string.contact_email) + "\n\n" +
                        "**Last Updated**\n" +
                        "December 2024")
                .setPositiveButton(getString(R.string.understood), (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.show();
        
        // Set button text color to logo_accent and make it bold
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.logo_accent, null));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void showContactUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.contact_us_title))
                .setMessage("**Phone:**\n" + getString(R.string.contact_phone) + "\n\n" +
                        "**Email:**\n" + getString(R.string.contact_email) + "\n\n" +
                        getString(R.string.contact_us_message))
                .setPositiveButton(getString(R.string.got_it), (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.show();
        
        // Set button text color to logo_accent and make it bold
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.logo_accent, null));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void showSecurityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        ImageView ivCurrentPasswordToggle = dialogView.findViewById(R.id.ivCurrentPasswordToggle);
        ImageView ivNewPasswordToggle = dialogView.findViewById(R.id.ivNewPasswordToggle);
        ImageView ivConfirmPasswordToggle = dialogView.findViewById(R.id.ivConfirmPasswordToggle);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);

        // Password visibility toggles
        setupPasswordToggle(ivCurrentPasswordToggle, etCurrentPassword);
        setupPasswordToggle(ivNewPasswordToggle, etNewPassword);
        setupPasswordToggle(ivConfirmPasswordToggle, etConfirmPassword);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Clear all errors first
            clearPasswordErrors(dialogView);

            if (TextUtils.isEmpty(currentPassword)) {
                showPasswordError(dialogView, R.id.tvCurrentPasswordError, getString(R.string.current_password) + " is required");
                etCurrentPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                showPasswordError(dialogView, R.id.tvNewPasswordError, getString(R.string.new_password) + " is required");
                etNewPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                showPasswordError(dialogView, R.id.tvNewPasswordError, getString(R.string.password_too_short));
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showPasswordError(dialogView, R.id.tvConfirmPasswordError, getString(R.string.passwords_do_not_match));
                etConfirmPassword.requestFocus();
                return;
            }

            changePassword(currentPassword, newPassword, dialog, dialogView);
        });

        dialog.show();
    }

    private void setupPasswordToggle(ImageView toggleView, TextInputEditText passwordField) {
        toggleView.setOnClickListener(v -> {
            if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
                passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleView.setImageResource(R.drawable.ic_eye_hidden);
            } else {
                passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleView.setImageResource(R.drawable.ic_eye_visible);
            }
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog, View dialogView) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
            
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                        if (passwordTask.isSuccessful()) {
                            Toast.makeText(requireContext(), getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to change password: " + passwordTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Show error under current password input instead of toast
                    showPasswordError(dialogView, R.id.tvCurrentPasswordError, getString(R.string.current_password_incorrect));
                }
            });
        }
    }

    private void showUsernameError(String message) {
        TextView errorText = getView().findViewById(R.id.tvUsernameError);
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private void clearUsernameError() {
        TextView errorText = getView().findViewById(R.id.tvUsernameError);
        if (errorText != null) {
            errorText.setVisibility(View.GONE);
        }
    }

    private void showPasswordError(View dialogView, int errorTextViewId, String message) {
        TextView errorText = dialogView.findViewById(errorTextViewId);
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private void clearPasswordErrors(View dialogView) {
        TextView currentPasswordError = dialogView.findViewById(R.id.tvCurrentPasswordError);
        TextView newPasswordError = dialogView.findViewById(R.id.tvNewPasswordError);
        TextView confirmPasswordError = dialogView.findViewById(R.id.tvConfirmPasswordError);
        
        if (currentPasswordError != null) currentPasswordError.setVisibility(View.GONE);
        if (newPasswordError != null) newPasswordError.setVisibility(View.GONE);
        if (confirmPasswordError != null) confirmPasswordError.setVisibility(View.GONE);
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}
>>>>>>> a5c8d92ccd60cd10072fbbfac66ea2b91863aa3e
