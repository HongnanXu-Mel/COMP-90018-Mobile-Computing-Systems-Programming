package com.example.food;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etUsername, etEmail;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
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
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
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
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateProfile() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Always update username in Firestore first
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            
            // Check if email actually changed
            if (email.equals(currentUser.getEmail())) {
                // Only username changed, update Firestore only
                userDoc.update("name", username)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 用户名更新成功
                            } else {
                                Toast.makeText(requireContext(), "Failed to update username: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // Email changed, update both username and email
                userDoc.update("name", username, "email", email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 个人资料更新成功
                            } else {
                                Toast.makeText(requireContext(), "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            changePassword(currentPassword, newPassword, dialog);
        });

        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
            
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                        if (passwordTask.isSuccessful()) {
                            // 密码修改成功
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to change password: " + passwordTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void logout() {
        mAuth.signOut();
                            // 登出成功
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}


