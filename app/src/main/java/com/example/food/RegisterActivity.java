package com.example.food;

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

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tabLogin, tabRegister, tvNameError, tvEmailError, tvPasswordMismatchError, tvPasswordStrengthError;
    private ImageView ivPasswordToggle, ivConfirmPasswordToggle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化FirebaseAuth实例
        mAuth = FirebaseAuth.getInstance();
        // 初始化Firestore实例
        db = FirebaseFirestore.getInstance();

        initViews();
        setupPasswordToggle();

        // 设置点击事件
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

        if (TextUtils.isEmpty(name)) {
            tvNameError.setText("Name is required");
            tvNameError.setVisibility(View.VISIBLE);
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tvEmailError.setText("Email is required");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            tvEmailError.setText("Please enter a valid email address");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tvPasswordStrengthError.setText("Password is required");
            tvPasswordStrengthError.setVisibility(View.VISIBLE);
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvPasswordMismatchError.setText("Passwords don't match");
            tvPasswordMismatchError.setVisibility(View.VISIBLE);
            return;
        }

        String passwordError = validatePasswordStrength(password);
        if (!passwordError.isEmpty()) {
            tvPasswordStrengthError.setText(passwordError);
            tvPasswordStrengthError.setVisibility(View.VISIBLE);
            return;
        }

        // 使用Firebase创建用户
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 注册成功，保存用户信息到Firestore
                            saveUserInfo(name, email);
                        } else {
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

    private void saveUserInfo(String name, String email) {
        // 创建用户信息映射
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);

        // 将用户信息保存到Firestore
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // 注册成功
                            // 跳转到主页
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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

    private String validatePasswordStrength(String password) {
        StringBuilder errors = new StringBuilder();
        
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

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}