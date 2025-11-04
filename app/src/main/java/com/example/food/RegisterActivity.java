package com.example.food;

/**
 * RegisterActivity - User registration screen
 * 
 * Features:
 * - User registration with email, password, and name
 * - Password strength validation (8+ chars, uppercase, lowercase, number, special char)
 * - Password confirmation matching
 * - Email format validation
 * - Password visibility toggle
 * - Save user data to Firebase Auth and Firestore
 * - Navigate to login after successful registration
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword; // Input fields
    private Button btnRegister; // Register button
    private TextView tabLogin, tabRegister, tvNameError, tvEmailError, tvPasswordMismatchError, tvPasswordStrengthError; // Tabs and error messages
    private ImageView ivPasswordToggle, ivConfirmPasswordToggle; // Password visibility toggles
    
    // Firebase
    private FirebaseAuth mAuth; // Firebase Authentication
    private FirebaseFirestore db; // Firestore database
    
    // State
    private boolean isPasswordVisible = false; // Password visibility state
    private boolean isConfirmPasswordVisible = false; // Confirm password visibility state

    /**
     * Initialize activity and setup UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        initViews();
        setupPasswordToggle();

        // Set click listeners
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Tab click listeners
        tabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        tabRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on register page, do nothing
            }
        });
    }

    /**
     * Initialize all UI component references
     */
    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tabLogin = findViewById(R.id.tabLogin);
        tabRegister = findViewById(R.id.tabRegister);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle);
        tvNameError = findViewById(R.id.tvNameError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordMismatchError = findViewById(R.id.tvPasswordMismatchError);
        tvPasswordStrengthError = findViewById(R.id.tvPasswordStrengthError);
    }

    /**
     * Setup password visibility toggles for both password fields
     */
    private void setupPasswordToggle() {
        ivPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_visible);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_hidden);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        ivConfirmPasswordToggle.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_visible);
                isConfirmPasswordVisible = false;
            } else {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_hidden);
                isConfirmPasswordVisible = true;
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
    }
    /**
     * Register new user with Firebase
     * Validates all inputs and creates account
     */
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear all error messages
        tvNameError.setVisibility(View.GONE);
        tvEmailError.setVisibility(View.GONE);
        tvPasswordMismatchError.setVisibility(View.GONE);
        tvPasswordStrengthError.setVisibility(View.GONE);

        // Show loading state on button
        setLoadingState(true);

        // Validate name is not empty

        if (TextUtils.isEmpty(name)) {
            setLoadingState(false);
            tvNameError.setText("Name is required");
            tvNameError.setVisibility(View.VISIBLE);
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            setLoadingState(false);
            tvEmailError.setText("Email is required");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            setLoadingState(false);
            tvEmailError.setText("Please enter a valid email address");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            setLoadingState(false);
            tvPasswordStrengthError.setText("Password is required");
            tvPasswordStrengthError.setVisibility(View.VISIBLE);
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            setLoadingState(false);
            tvPasswordMismatchError.setText("Passwords don't match");
            tvPasswordMismatchError.setVisibility(View.VISIBLE);
            return;
        }

        String passwordError = validatePasswordStrength(password);
        if (!passwordError.isEmpty()) {
            setLoadingState(false);
            tvPasswordStrengthError.setText(passwordError);
            tvPasswordStrengthError.setVisibility(View.VISIBLE);
            return;
        }

        // Create Firebase Auth account with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Account created successfully - now save user info to Firestore
                            saveUserInfo(name, email);
                        } else {
                            setLoadingState(false);
                            Exception e = task.getException();
                            String code = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : "N/A";
                            String message = e != null ? e.getMessage() : "Unknown error";
                            Log.e("RegisterActivity", "createUserWithEmail failed: code=" + code, e);
                            Toast.makeText(RegisterActivity.this, "Registration failed (" + code + "): " + message,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Save user information to Firestore after Firebase Auth account creation
     */
    private void saveUserInfo(String name, String email) {
        // Create user data map
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);

        // Save to Firestore users collection
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoadingState(false);
                        if (task.isSuccessful()) {
                            // Registration successful, redirect to login page
                            Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Exception e = task.getException();
                            String message = e != null ? e.getMessage() : "Unknown error";
                            Log.e("RegisterActivity", "saveUserInfo failed", e);
                            Toast.makeText(RegisterActivity.this, "Failed to save user info: " + message,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Validate password strength according to requirements
     * @return Error message string, or empty string if valid
     */
    private String validatePasswordStrength(String password) {
        StringBuilder errors = new StringBuilder();
        // Check each requirement and build error list
        if (password.length() < 8) {
            errors.append("• At least 8 characters\n");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.append("• At least one uppercase letter\n");
        }
        
        if (!password.matches(".*[a-z].*")) {
            errors.append("• At least one lowercase letter\n");
        }
        
        if (!password.matches(".*\\d.*")) {
            errors.append("• At least one number\n");
        }
        
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            errors.append("• At least one special character");
        }
        
        String errorList = errors.toString().trim();
        if (!errorList.isEmpty()) {
            return "Passwords must be:\n" + errorList;
        }
        
        return errorList;
    }

    /**
     * Validate email address format
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Set button loading state
     */
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            btnRegister.setEnabled(false);
            btnRegister.setText("Creating account...");
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setText("Register");
        }
    }
}