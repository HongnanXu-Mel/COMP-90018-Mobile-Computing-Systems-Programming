package com.example.food

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class ChangeProfilePictureDialog(context: Context) : Dialog(context) {
    
    private val TAG = "ChangeProfilePictureDialog"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUser = auth.currentUser
    
    private lateinit var ivCurrentProfilePicture: CircleImageView
    private lateinit var btnTakePhoto: android.widget.LinearLayout
    private lateinit var btnChooseFromGallery: android.widget.LinearLayout
    private lateinit var btnRemovePhoto: android.widget.LinearLayout
    private lateinit var btnCancel: android.widget.Button
    
    private var selectedImageUri: Uri? = null
    
    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
        private const val GALLERY_REQUEST_CODE = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_profile_picture)
        
        setupDialog()
        initializeViews()
        setupClickListeners()
        loadCurrentProfilePicture()
    }
    
    private fun setupDialog() {
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    private fun initializeViews() {
        ivCurrentProfilePicture = findViewById(R.id.ivCurrentProfilePicture)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery)
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto)
        btnCancel = findViewById(R.id.btnCancel)
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener { dismiss() }
        btnTakePhoto.setOnClickListener { openCamera() }
        btnChooseFromGallery.setOnClickListener { openGallery() }
        btnRemovePhoto.setOnClickListener { removeProfilePicture() }
    }
    
    private fun loadCurrentProfilePicture() {
        if (currentUser == null) return
        
        val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivCurrentProfilePicture)
        }.addOnFailureListener { exception ->
            ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
        }
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? AppCompatActivity)?.startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(context, context.getString(R.string.camera_not_available), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? AppCompatActivity)?.startActivityForResult(intent, GALLERY_REQUEST_CODE)
        } else {
            Toast.makeText(context, context.getString(R.string.gallery_not_available), Toast.LENGTH_SHORT).show()
        }
    }
    
    fun handleImageResult(requestCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    selectedImageUri = bitmapToUri(bitmap)
                    updateProfilePicturePreview()
                }
            }
            GALLERY_REQUEST_CODE -> {
                selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    updateProfilePicturePreview()
                }
            }
        }
    }
    
    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "temp_profile_picture.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()
        return Uri.fromFile(file)
    }
    
    private fun updateProfilePicturePreview() {
        selectedImageUri?.let { uri: Uri ->
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivCurrentProfilePicture)
            
            uploadProfilePicture(uri)
        }
    }
    
    private fun uploadProfilePicture(imageUri: Uri) {
        if (currentUser == null) return
        
        val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}.jpg")
        
        Toast.makeText(context, context.getString(R.string.uploading_profile_picture), Toast.LENGTH_SHORT).show()
        
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri: Uri ->
                    updateUserProfilePicture(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error uploading profile picture", exception)
                Toast.makeText(context, context.getString(R.string.failed_to_upload_profile_picture), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateUserProfilePicture(imageUrl: String) {
        if (currentUser == null) return
        
        db.collection("users").document(currentUser.uid)
            .update("avatarUrl", imageUrl)
            .addOnSuccessListener { _ ->
                Toast.makeText(context, context.getString(R.string.profile_picture_updated_successfully), Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating profile picture in database", exception)
                Toast.makeText(context, context.getString(R.string.failed_to_update_profile_picture), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun removeProfilePicture() {
        if (currentUser == null) return
        
        val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}.jpg")
        storageRef.delete()
            .addOnSuccessListener { _ ->
                db.collection("users").document(currentUser.uid)
                    .update("avatarUrl", "")
                    .addOnSuccessListener { _ ->
                        Toast.makeText(context, context.getString(R.string.profile_picture_removed_successfully), Toast.LENGTH_SHORT).show()
                        ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
                        dismiss()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error removing profile picture from database", exception)
                        Toast.makeText(context, context.getString(R.string.failed_to_remove_profile_picture), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting profile picture from storage", exception)
                Toast.makeText(context, context.getString(R.string.failed_to_remove_profile_picture), Toast.LENGTH_SHORT).show()
            }
    }
}