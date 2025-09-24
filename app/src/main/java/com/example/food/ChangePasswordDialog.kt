package com.example.food

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ChangePasswordDialog(context: Context) : Dialog(context) {
    
    private val TAG = "ChangePasswordDialog"
    private val auth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = auth.currentUser
    
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var currentPasswordInputLayout: TextInputLayout
    private lateinit var newPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var btnCancel: android.widget.Button
    private lateinit var btnConfirm: androidx.appcompat.widget.AppCompatButton
    private lateinit var ivCurrentPasswordToggle: ImageView
    private lateinit var ivNewPasswordToggle: ImageView
    private lateinit var ivConfirmPasswordToggle: ImageView
    
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_password)
        
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
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        currentPasswordInputLayout = findViewById(R.id.currentPasswordInputLayout)
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        btnCancel = findViewById(R.id.btnCancel)
        btnConfirm = findViewById(R.id.btnConfirm)
        ivCurrentPasswordToggle = findViewById(R.id.ivCurrentPasswordToggle)
        ivNewPasswordToggle = findViewById(R.id.ivNewPasswordToggle)
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle)
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener { changePassword() }
        setupPasswordToggles()
    }
    
    private fun setupPasswordToggles() {
        ivCurrentPasswordToggle.setOnClickListener {
            if (isCurrentPasswordVisible) {
                etCurrentPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivCurrentPasswordToggle.setImageResource(R.drawable.ic_eye_visible)
                isCurrentPasswordVisible = false
            } else {
                etCurrentPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivCurrentPasswordToggle.setImageResource(R.drawable.ic_eye_hidden)
                isCurrentPasswordVisible = true
            }
            etCurrentPassword.setSelection(etCurrentPassword.text?.length ?: 0)
        }
        
        ivNewPasswordToggle.setOnClickListener {
            if (isNewPasswordVisible) {
                etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivNewPasswordToggle.setImageResource(R.drawable.ic_eye_visible)
                isNewPasswordVisible = false
            } else {
                etNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivNewPasswordToggle.setImageResource(R.drawable.ic_eye_hidden)
                isNewPasswordVisible = true
            }
            etNewPassword.setSelection(etNewPassword.text?.length ?: 0)
        }
        
        ivConfirmPasswordToggle.setOnClickListener {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_visible)
                isConfirmPasswordVisible = false
            } else {
                etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_hidden)
                isConfirmPasswordVisible = true
            }
            etConfirmPassword.setSelection(etConfirmPassword.text?.length ?: 0)
        }
    }
    
    private fun changePassword() {
        val currentPassword = etCurrentPassword.text?.toString()?.trim() ?: ""
        val newPassword = etNewPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = etConfirmPassword.text?.toString()?.trim() ?: ""
        
        clearErrors()
        
        if (!validateInputs(currentPassword, newPassword, confirmPassword)) {
            return
        }
        
        if (currentUser == null) {
            showError(R.id.tvCurrentPasswordError, context.getString(R.string.user_not_authenticated))
            return
        }
        
        setLoadingState(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential).await()
                currentUser.updatePassword(newPassword).await()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password", e)
                
                withContext(Dispatchers.Main) {
                    handlePasswordChangeError(e)
                    setLoadingState(false)
                }
            }
        }
    }
    
    private fun handlePasswordChangeError(e: Exception) {
        when {
            e.message?.contains("wrong-password") == true || 
            e.message?.contains("invalid-credential") == true -> {
                showError(R.id.tvCurrentPasswordError, "Current password is incorrect")
            }
            e.message?.contains("weak-password") == true -> {
                showError(R.id.tvNewPasswordError, context.getString(R.string.password_too_weak))
            }
            else -> {
                showError(R.id.tvNewPasswordError, "Current password is incorrect")
            }
        }
    }
    
    private fun validateInputs(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = true
        
        if (currentPassword.isEmpty()) {
            showError(R.id.tvCurrentPasswordError, context.getString(R.string.current_password_required))
            isValid = false
        }
        
        if (newPassword.isEmpty()) {
            showError(R.id.tvNewPasswordError, context.getString(R.string.new_password_required))
            isValid = false
        } else {
            val passwordError = validatePasswordStrength(newPassword)
            if (passwordError.isNotEmpty()) {
                showError(R.id.tvNewPasswordError, passwordError)
                isValid = false
            }
        }
        
        if (confirmPassword.isEmpty()) {
            showError(R.id.tvConfirmPasswordError, context.getString(R.string.confirm_password_required))
            isValid = false
        } else if (newPassword != confirmPassword) {
            showError(R.id.tvConfirmPasswordError, context.getString(R.string.passwords_do_not_match))
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        findViewById<TextView>(R.id.tvCurrentPasswordError).visibility = View.GONE
        findViewById<TextView>(R.id.tvNewPasswordError).visibility = View.GONE
        findViewById<TextView>(R.id.tvConfirmPasswordError).visibility = View.GONE
    }
    
    private fun showError(errorTextViewId: Int, message: String) {
        val errorTextView = findViewById<TextView>(errorTextViewId)
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }
    
    private fun setLoadingState(loading: Boolean) {
        btnConfirm.isEnabled = !loading
        btnConfirm.text = if (loading) context.getString(R.string.changing) else context.getString(R.string.confirm)
    }
    
    private fun validatePasswordStrength(password: String): String {
        val errors = StringBuilder()
        
        if (password.length < 8) {
            errors.append("• At least 8 characters\n")
        }
        
        if (!password.matches(".*[A-Z].*".toRegex())) {
            errors.append("• At least one uppercase letter\n")
        }
        
        if (!password.matches(".*[a-z].*".toRegex())) {
            errors.append("• At least one lowercase letter\n")
        }
        
        if (!password.matches(".*\\d.*".toRegex())) {
            errors.append("• At least one number\n")
        }
        
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*".toRegex())) {
            errors.append("• At least one special character")
        }
        
        val errorList = errors.toString().trim()
        return if (errorList.isNotEmpty()) {
            "Passwords must be:\n$errorList"
        } else {
            ""
        }
    }
}