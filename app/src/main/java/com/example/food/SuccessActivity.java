package com.example.food;

/**
 * SuccessActivity - Success confirmation screen after login
 * 
 * Features:
 * - Display successful login message with user email
 * - Provide logout button
 * - Navigate back to login screen after logout
 * 
 * Note: This activity is mainly for testing purposes
 */

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SuccessActivity extends AppCompatActivity {

    // Firebase Authentication
    private FirebaseAuth mAuth;

    /**
     * Initialize activity and display success message
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        mAuth = FirebaseAuth.getInstance();

        // Get UI components
        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Display logged in user's email
        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
        tvMessage.setText("Login successful\n\n" + (email.isEmpty() ? "" : ("Logged in as: " + email)));

        // Logout button - sign out and return to login screen
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Logout successful - navigate to login
            startActivity(new Intent(SuccessActivity.this, LoginActivity.class));
            finish();
        });
    }
}





