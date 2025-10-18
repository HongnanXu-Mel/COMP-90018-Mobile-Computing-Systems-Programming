package com.example.food

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ChangeProfilePictureDialogFragment : DialogFragment() {
    
    private val TAG = "ChangeProfilePictureDialog"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser
    
    interface OnProfilePictureChangedListener {
        fun onProfilePictureChanged()
    }
    
    private var listener: OnProfilePictureChangedListener? = null
    
    fun setOnProfilePictureChangedListener(listener: OnProfilePictureChangedListener) {
        this.listener = listener
    }
    
    private lateinit var ivCurrentProfilePicture: CircleImageView
    private lateinit var btnTakePhoto: com.google.android.material.button.MaterialButton
    private lateinit var btnChooseFromGallery: com.google.android.material.button.MaterialButton
    private lateinit var btnRemovePhoto: com.google.android.material.button.MaterialButton
    private lateinit var btnCancel: com.google.android.material.button.MaterialButton
    
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Camera permission result: $isGranted")
        if (isGranted) {
            Log.d(TAG, "Camera permission granted, opening camera")
            openCamera()
        } else {
            Log.d(TAG, "Camera permission denied")
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    // activity result launchers
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && cameraImageUri != null) {
            // Check file size to debug quality issues
            try {
                cameraImageUri?.path?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val fileSize = file.length()
                        Log.d(TAG, "Camera captured file size: $fileSize bytes")
                        if (fileSize < 10000) { // Less than 10KB is probably a thumbnail
                            Log.w(TAG, "WARNING: File size is very small, likely a thumbnail: $fileSize bytes")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking file size", e)
            }

            selectedImageUri = cameraImageUri
            updateProfilePicturePreview()
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Gallery result received, resultCode: ${result.resultCode}")
        Log.d(TAG, "Gallery data: ${result.data}")
        Log.d(TAG, "Gallery data URI: ${result.data?.data}")

        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            Log.d(TAG, "Gallery selected URI: $selectedImageUri")

            if (selectedImageUri != null) {
                // Check file size for gallery selection too
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    Log.d(TAG, "Gallery selected file size: ${bytes?.size ?: 0} bytes")
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking gallery file size", e)
                }

                updateProfilePicturePreview()
            } else {
                Log.e(TAG, "Gallery selection failed - URI is null")
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Gallery selection cancelled or failed")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_profile_picture, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDialog()
        initializeViews(view)
        setupClickListeners()
        loadCurrentProfilePicture()
    }
    
    private fun setupDialog() {
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    private fun initializeViews(view: View) {
        ivCurrentProfilePicture = view.findViewById(R.id.ivCurrentProfilePicture)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnChooseFromGallery = view.findViewById(R.id.btnChooseFromGallery)
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto)
        btnCancel = view.findViewById(R.id.btnCancel)
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener { dismiss() }
        btnTakePhoto.setOnClickListener { checkCameraPermissionAndOpenCamera() }
        btnChooseFromGallery.setOnClickListener { openGallery() }
        btnRemovePhoto.setOnClickListener { showRemoveConfirmationDialog() }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        Log.d(TAG, "Checking camera permission")
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted, opening camera")
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d(TAG, "Camera permission denied before, showing rationale")
                Toast.makeText(requireContext(), "Camera permission is needed to take photos", Toast.LENGTH_SHORT).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                Log.d(TAG, "Requesting camera permission for first time")
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun loadCurrentProfilePicture() {
        if (currentUser == null) {
            ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
            return
        }
        
        // first try to load from user profile avatarUrl
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val avatarUrl = document.getString("avatarUrl")
                if (!avatarUrl.isNullOrEmpty()) {
                    // show border when image exists
                    ivCurrentProfilePicture.borderWidth = 4
                    Glide.with(requireContext())
                        .load(avatarUrl)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .centerCrop()
                        .override(200, 200)
                        .into(ivCurrentProfilePicture)
                } else {
                    // no border for placeholder
                    ivCurrentProfilePicture.borderWidth = 0
                    ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
                }
            }
            .addOnFailureListener { exception ->
                // no border for placeholder
                ivCurrentProfilePicture.borderWidth = 0
                ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
            }
    }
    
    private fun openCamera() {
        val photoFile = File(requireContext().getExternalFilesDir(null), "profile_picture_${System.currentTimeMillis()}.jpg")

        // Ensure the file exists before passing to camera
        try {
            photoFile.createNewFile()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create photo file", e)
        }

        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        
        intent.putExtra("android.intent.extra.videoQuality", 1)
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1) // Front camera, but let camera decide
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        cameraLauncher.launch(intent)
    }
    
    private fun openGallery() {
        Log.d(TAG, "Opening gallery")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        Log.d(TAG, "Gallery intent created: $intent")

        galleryLauncher.launch(intent)
    }
    
    
    private fun updateProfilePicturePreview() {
        selectedImageUri?.let { uri: Uri ->
            Log.d(TAG, "Updating profile picture preview")
            
            // show border when image is selected
            ivCurrentProfilePicture.borderWidth = 4
            Glide.with(requireContext())
                .load(uri)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .centerCrop()
                .override(200, 200)
                .into(ivCurrentProfilePicture)
            
            Log.d(TAG, "Starting upload process")
            uploadProfilePicture(uri)
        } ?: run {
            Log.e(TAG, "selectedImageUri is null, cannot update preview")
        }
    }
    
    private fun uploadProfilePicture(imageUri: Uri) {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot upload profile picture")
            return
        }
        
        Log.d(TAG, "Starting profile picture upload")
        Toast.makeText(requireContext(), getString(R.string.uploading_profile_picture), Toast.LENGTH_SHORT).show()
        
        val supabaseService = SupabaseStorageService(requireContext())
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Starting upload with Supabase authentication")
                val signedUrl = supabaseService.uploadProfilePicture(currentUser.uid, imageUri)
                Log.d(TAG, "Upload completed successfully")
                
                if (signedUrl != null) {
                    Log.d(TAG, "Updating user profile")
                    updateUserProfilePicture(signedUrl)
                } else {
                    Log.e(TAG, "Upload failed - signedUrl is null")
                    Toast.makeText(requireContext(), getString(R.string.failed_to_upload_profile_picture), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during upload", e)
                e.printStackTrace()
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateUserProfilePicture(imageUrl: String) {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot update profile picture")
            return
        }
        
        Log.d(TAG, "Updating Firestore with new image URL")
        
        db.collection("users").document(currentUser.uid)
            .update("avatarUrl", imageUrl)
            .addOnSuccessListener { _ ->
                Log.d(TAG, "Successfully updated avatarUrl in Firestore")
                Toast.makeText(requireContext(), getString(R.string.profile_picture_updated_successfully), Toast.LENGTH_SHORT).show()
                listener?.onProfilePictureChanged()
                dismiss()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating profile picture in database", exception)
                exception.printStackTrace()
                Toast.makeText(requireContext(), "Database update failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun showRemoveConfirmationDialog() {
        val dialog = RemoveProfilePictureDialogFragment()
        dialog.setOnRemoveConfirmedListener(object : RemoveProfilePictureDialogFragment.OnRemoveConfirmedListener {
            override fun onRemoveConfirmed() {
                removeProfilePicture()
            }
        })
        dialog.show(parentFragmentManager, "RemoveProfilePictureDialog")
    }
    
    private fun removeProfilePicture() {
        if (currentUser == null) return
        
        // first update database to remove avatarUrl
        db.collection("users").document(currentUser.uid)
            .update("avatarUrl", "")
            .addOnSuccessListener { _ ->
                // then try to delete from storage
                val supabaseService = SupabaseStorageService(requireContext())
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        supabaseService.deleteProfilePicture(currentUser.uid)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting from storage", e)
                    }
                    
                    Toast.makeText(requireContext(), getString(R.string.profile_picture_removed_successfully), Toast.LENGTH_SHORT).show()
                    ivCurrentProfilePicture.borderWidth = 0
                    ivCurrentProfilePicture.setImageResource(R.drawable.ic_person)
                    listener?.onProfilePictureChanged()
                    dismiss()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error removing profile picture from database", exception)
                Toast.makeText(requireContext(), getString(R.string.failed_to_remove_profile_picture), Toast.LENGTH_SHORT).show()
            }
    }
}
