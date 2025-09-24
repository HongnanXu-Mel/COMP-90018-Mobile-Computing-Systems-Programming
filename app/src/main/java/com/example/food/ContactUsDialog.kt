package com.example.food

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class ContactUsDialog(context: Context) : Dialog(context) {
    
    private lateinit var btnUnderstood: android.widget.Button
    private lateinit var btnCopyEmail: ImageView
    private lateinit var btnCopyPhone: ImageView
    private lateinit var tvEmailText: TextView
    private lateinit var tvPhoneText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_contact_us)
        
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
        btnUnderstood = findViewById(R.id.btnUnderstood)
        btnCopyEmail = findViewById(R.id.btnCopyEmail)
        btnCopyPhone = findViewById(R.id.btnCopyPhone)
        tvEmailText = findViewById(R.id.tvEmailText)
        tvPhoneText = findViewById(R.id.tvPhoneText)
    }
    
    private fun setupClickListeners() {
        btnUnderstood.setOnClickListener { dismiss() }
        
        btnCopyEmail.setOnClickListener {
            copyToClipboard(tvEmailText.text.toString(), "Email copied to clipboard")
        }
        
        btnCopyPhone.setOnClickListener {
            copyToClipboard(tvPhoneText.text.toString(), "Phone number copied to clipboard")
        }
    }
    
    private fun copyToClipboard(text: String, message: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Contact Info", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}