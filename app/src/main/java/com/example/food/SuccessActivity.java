package com.example.food;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SuccessActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        mAuth = FirebaseAuth.getInstance();

        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnLogout = findViewById(R.id.btnLogout);

        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
        tvMessage.setText("Login successful\n\n" + (email.isEmpty() ? "" : ("Logged in as: " + email)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // 登出成功
            startActivity(new Intent(SuccessActivity.this, LoginActivity.class));
            finish();
        });
    }
}





