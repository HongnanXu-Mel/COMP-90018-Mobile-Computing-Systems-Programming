package com.example.food

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton

class LogoutConfirmationDialog(
    context: Context,
    private val onLogoutConfirmed: Runnable
) : Dialog(context) {
    
    private lateinit var btnCancel: Button
    private lateinit var btnLogout: AppCompatButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_logout_confirmation)
        
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
        btnCancel = findViewById(R.id.btnCancel)
        btnLogout = findViewById(R.id.btnLogout)
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener { dismiss() }
        btnLogout.setOnClickListener {
            onLogoutConfirmed.run()
            dismiss()
        }
    }
}
