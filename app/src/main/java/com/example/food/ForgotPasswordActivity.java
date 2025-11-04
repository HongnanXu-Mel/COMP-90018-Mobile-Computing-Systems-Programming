package com.example.food;

/**
 * ForgotPasswordActivity - Activity for handling password reset functionality
 * 
 * Allows users to:
 * - Enter their email address
 * - Request a password reset email from Firebase Auth
 * - Navigate back to login screen
 * - See success/error messages
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etEmail; // Email input field
    private Button btnSendResetEmail, btnBackToLogin; // Action buttons
    private TextView tvError, tvSuccess; // Message displays
    private ImageView ivBack; // Back arrow button
    
    // Firebase
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();
    }

    /**
     * Initialize all UI component references
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvError = findViewById(R.id.tvError);
        tvSuccess = findViewById(R.id.tvSuccess);
        ivBack = findViewById(R.id.ivBack);
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Back button - close activity
        ivBack.setOnClickListener(v -> finish());

        // Send reset email button
        btnSendResetEmail.setOnClickListener(v -> sendPasswordResetEmail());

        // Navigate to login screen button
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Send password reset email via Firebase Auth
     * Validates email input and handles success/error states
     */
    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();
        
        // Clear previous messages
        hideAllMessages();

        // Validate: email cannot be empty
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.email_required));
            return;
        }

        if (!isValidEmail(email)) {
            showError(getString(R.string.invalid_email_format));
            return;
        }

        // Disable button and show loading state
        btnSendResetEmail.setEnabled(false);
        btnSendResetEmail.setText("Sending...");

        // Call Firebase Auth to send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Re-enable button and restore text
                        btnSendResetEmail.setEnabled(true);
                        btnSendResetEmail.setText(getString(R.string.send_reset_email));

                        if (task.isSuccessful()) {
                            // Email sent successfully
                            showSuccess(String.format(getString(R.string.reset_email_sent), email));
                            Toast.makeText(ForgotPasswordActivity.this, 
                                    getString(R.string.check_email_instructions), 
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Failed to send email - parse error message
                            String errorMessage = getString(R.string.reset_email_failed);
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null && exceptionMessage.contains("user-not-found")) {
                                    errorMessage = getString(R.string.email_not_found);
                                }
                            }
                            showError(errorMessage);
                        }
                    }
                });
    }

    /**
     * Validate email format using Android Patterns
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Display error message and hide success message
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }

    /**
     * Display success message and hide error message
     */
    private void showSuccess(String message) {
        tvSuccess.setText(message);
        tvSuccess.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    /**
     * Hide both error and success messages
     */
    private void hideAllMessages() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
    }
}
