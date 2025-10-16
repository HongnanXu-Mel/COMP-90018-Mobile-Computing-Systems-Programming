package com.example.food;

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

    private TextInputEditText etEmail;
    private Button btnSendResetEmail, btnBackToLogin;
    private TextView tvError, tvSuccess;
    private ImageView ivBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvError = findViewById(R.id.tvError);
        tvSuccess = findViewById(R.id.tvSuccess);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSendResetEmail.setOnClickListener(v -> sendPasswordResetEmail());

        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();
        
        // Clear previous messages
        hideAllMessages();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.email_required));
            return;
        }

        if (!isValidEmail(email)) {
            showError(getString(R.string.invalid_email_format));
            return;
        }

        // Show loading state
        btnSendResetEmail.setEnabled(false);
        btnSendResetEmail.setText("Sending...");

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Reset button state
                        btnSendResetEmail.setEnabled(true);
                        btnSendResetEmail.setText(getString(R.string.send_reset_email));

                        if (task.isSuccessful()) {
                            showSuccess(String.format(getString(R.string.reset_email_sent), email));
                            Toast.makeText(ForgotPasswordActivity.this, 
                                    getString(R.string.check_email_instructions), 
                                    Toast.LENGTH_LONG).show();
                        } else {
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






    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        tvSuccess.setText(message);
        tvSuccess.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void hideAllMessages() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
    }
}
