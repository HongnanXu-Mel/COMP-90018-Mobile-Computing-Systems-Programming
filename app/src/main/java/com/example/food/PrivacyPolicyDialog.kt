package com.example.food

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class PrivacyPolicyDialog(context: Context) : Dialog(context) {
    
    private lateinit var btnGotIt: android.widget.Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_privacy_policy)
        
        setupDialog()
        initializeViews()
        setupClickListeners()
    }
    
    private fun setupDialog() {
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    private fun initializeViews() {
        btnGotIt = findViewById(R.id.btnGotIt)
    }
    
    private fun setupClickListeners() {
        btnGotIt.setOnClickListener { dismiss() }
    }
}