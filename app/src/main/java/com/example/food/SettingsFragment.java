package com.example.food;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etUsername, etEmail;
    private Button btnUpdateProfile;
    private LinearLayout btnPrivacyPolicy, btnContactUs, btnSecurity, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

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
        }
    }

    private void setupClickListeners() {
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
