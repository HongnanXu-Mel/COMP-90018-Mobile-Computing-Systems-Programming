package com.example.food;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp, tvLoginError;
    private ImageView ivPasswordToggle;
    private CheckBox cbRememberMe;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_REMEMBER_ME = "REMEMBER_ME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_LOGIN_TIME = "LOGIN_TIME";
    private static final long THIRTY_DAYS_IN_MILLIS = 30L * 24L * 60L * 60L * 1000L; // 30 days in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        
        setupPasswordToggle();
        loadRememberMeState();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

    }

    private void initViews() {
        etEmail = findViewById(R.id.etLoginEmail); 
        etPassword = findViewById(R.id.etLoginPassword); 
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tabRegister);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvLoginError = findViewById(R.id.tvLoginError);
        cbRememberMe = findViewById(R.id.cbRememberMe);
    }

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
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tvLoginError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide loading state
                        setLoadingState(false);
                        
                        if (task.isSuccessful()) {
                            // Save remember me state
                            saveRememberMeState(email);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            tvLoginError.setText("Email or password is incorrect");
                            tvLoginError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
        }
    }


    private void loadRememberMeState() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        cbRememberMe.setChecked(rememberMe);
        
        if (rememberMe) {
            // Check if login is still valid (within 30 days)
            long loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - loginTime < THIRTY_DAYS_IN_MILLIS) {
                String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");
                if (!TextUtils.isEmpty(savedEmail)) {
                    etEmail.setText(savedEmail);
                    // Auto-login if user is already authenticated
                    if (mAuth.getCurrentUser() != null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }
            } else {
                // Login expired, clear remember me state
                clearRememberMeState(sharedPreferences);
            }
        }
    }

    private void saveRememberMeState(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, cbRememberMe.isChecked());
        
        if (cbRememberMe.isChecked()) {
            editor.putString(KEY_USER_EMAIL, email);
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        } else {
            editor.remove(KEY_USER_EMAIL);
            editor.remove(KEY_LOGIN_TIME);
        }
        editor.apply();
    }

    // Method to clear remember me state on logout
    public static void clearRememberMeState(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_LOGIN_TIME);
        editor.apply();
    }
}