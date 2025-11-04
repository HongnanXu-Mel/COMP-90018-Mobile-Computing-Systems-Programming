package com.example.food;

/**
 * AddFragment - Fragment for creating and submitting new restaurant reviews
 * 
 * This fragment allows users to:
 * - Select a restaurant from a dropdown
 * - Choose a visit date
 * - Upload up to 3 images
 * - Provide rating (1-5 stars)
 * - Add caption and description
 * - Submit review to Firebase Firestore and Firebase Storage
 */

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food.data.Review;
import com.example.food.model.Restaurant;
import com.example.food.service.ReviewService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddFragment extends Fragment {
    // Constants for request codes and tag logging
    private static final String TAG = "AddFragment";
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picker
    
    // UI Components
    
    private EditText etCaption, etDescription; // Input fields for caption and description
    private RatingBar ratingBar; // Star rating selector
    private Spinner spinnerRestaurant; // Dropdown to select restaurant
    private TextView tvSelectedDate; // Display selected visit date
    private Button btnSelectDate, btnSelectImages, btnSubmit; // Action buttons
    private ImageView ivPreview1, ivPreview2, ivPreview3; // Preview images
    
    // Data
    
    private List<Restaurant> restaurants; // List of all restaurants
    private Restaurant selectedRestaurant; // Currently selected restaurant
    private Date selectedDate; // Visit date chosen by user
    private List<Uri> selectedImageUris; // Local URIs of selected images
    private List<String> uploadedImageUrls; // URLs of images uploaded to Firebase Storage
    
    // Firebase instances
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ReviewService reviewService;

    /**
     * Create and initialize the fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        
        initViews(view);
        setupListeners();
        initializeFirebase();
        loadRestaurants();
        
        return view;
    }
    
    /**
     * Initialize all UI components and data structures
     */
    private void initViews(View view) {
        etCaption = view.findViewById(R.id.et_caption);
        etDescription = view.findViewById(R.id.et_description);
        ratingBar = view.findViewById(R.id.rating_bar);
        spinnerRestaurant = view.findViewById(R.id.spinner_restaurant);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSelectImages = view.findViewById(R.id.btn_select_images);
        btnSubmit = view.findViewById(R.id.btn_submit);
        ivPreview1 = view.findViewById(R.id.iv_preview_1);
        ivPreview2 = view.findViewById(R.id.iv_preview_2);
        ivPreview3 = view.findViewById(R.id.iv_preview_3);
        
        restaurants = new ArrayList<>();
        selectedImageUris = new ArrayList<>();
        uploadedImageUrls = new ArrayList<>();
        
        // Set default date to current date
        selectedDate = new Date();
        updateDateDisplay();
    }
    
    /**
     * Setup click listeners for all interactive UI elements
     */
    
    private void setupListeners() {
        // Date picker button
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        // Image selection button
        btnSelectImages.setOnClickListener(v -> selectImages());
        // Submit review button
        btnSubmit.setOnClickListener(v -> submitReview());
        
        // Restaurant spinner selection listener
        spinnerRestaurant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip the first "Select Restaurant" item
                if (position > 0) {
                    selectedRestaurant = restaurants.get(position - 1);
                } else {
                    selectedRestaurant = null;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRestaurant = null;
            }
        });
    }
    
    /**
     * Initialize Firebase services (Auth, Firestore, Storage)
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reviewService = new ReviewService();
    }
    
    /**
     * Load all restaurants from Firestore for the dropdown selector
     */
    private void loadRestaurants() {
        db.collection("restaurants")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                restaurants.clear();
                for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Restaurant restaurant = document.toObject(Restaurant.class);
                    if (restaurant != null) {
                        restaurant.setId(document.getId());
                        restaurants.add(restaurant);
                    }
                }
                setupRestaurantSpinner();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading restaurants", e);
                Toast.makeText(getContext(), "Failed to load restaurants", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Populate the restaurant spinner with loaded data
     */
    private void setupRestaurantSpinner() {
        List<String> restaurantNames = new ArrayList<>();
        restaurantNames.add("Select Restaurant");
        
        for (Restaurant restaurant : restaurants) {
            restaurantNames.add(restaurant.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, restaurantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestaurant.setAdapter(adapter);
    }
    
    /**
     * Display date picker dialog for selecting visit date
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
            (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                selectedDate = selectedCalendar.getTime();
                updateDateDisplay();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    /**
     * Update the date display text with selected date
     */
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate));
    }
    
    /**
     * Launch system image picker to select up to 3 images
     */
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }
    
    /**
     * Handle result from image picker activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUris.clear();
            
            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < Math.min(count, 3); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Single image selected
                selectedImageUris.add(data.getData());
            }
            
            updateImagePreviews();
        }
    }
    
    /**
     * Update image preview displays with selected images
     */
    private void updateImagePreviews() {
        // Clear all previews first
        ivPreview1.setImageDrawable(null);
        ivPreview2.setImageDrawable(null);
        ivPreview3.setImageDrawable(null);
        
        // Set previews for selected images
        ImageView[] previews = {ivPreview1, ivPreview2, ivPreview3};
        for (int i = 0; i < Math.min(selectedImageUris.size(), 3); i++) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUris.get(i));
                previews[i].setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image preview", e);
            }
        }
    }
    
    /**
     * Validate input and start review submission process
     */
    private void submitReview() {
        if (!validateInput()) {
            return;
        }
        
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");
        
        // Upload images first, then create review
        uploadImagesAndCreateReview();
    }
    
    /**
     * Validate all required input fields
     * @return true if all inputs are valid, false otherwise
     */
    
    private boolean validateInput() {
        // Check caption
        if (TextUtils.isEmpty(etCaption.getText().toString().trim())) {
            Toast.makeText(getContext(), "Please enter a caption", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            Toast.makeText(getContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedRestaurant == null) {
            Toast.makeText(getContext(), "Please select a restaurant", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (ratingBar.getRating() == 0) {
            Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    /**
     * Upload selected images to Firebase Storage, then create review
     */
    private void uploadImagesAndCreateReview() {
        if (selectedImageUris.isEmpty()) {
            createReview(new ArrayList<>());
            return;
        }
        
        uploadedImageUrls.clear();
        uploadNextImage(0);
    }
    
    /**
     * Recursively upload images one by one
     * @param index Current image index to upload
     */
    private void uploadNextImage(int index) {
        if (index >= selectedImageUris.size()) {
            createReview(uploadedImageUrls);
            return;
        }
        
        Uri imageUri = selectedImageUris.get(index);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child("review_images").child(fileName);
        
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrls.add(uri.toString());
                    uploadNextImage(index + 1);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
                Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                resetSubmitButton();
            });
        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
        }
    }
    
    /**
     * Create review object and save to Firestore
     * @param imageUrls List of uploaded image URLs from Firebase Storage
     */
    private void createReview(List<String> imageUrls) {
        String userId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = mAuth.getCurrentUser().getEmail();
        }
        
        String caption = etCaption.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        float rating = ratingBar.getRating();
        
        // Calculate accuracy percent - default to 100% for new reviews
        double accuracyPercent = 100.0;
        
        // Determine first image type (SQUARE/PORTRAIT/HORIZONTAL)
        String firstImageType = "SQUARE"; // Default type
        if (!imageUrls.isEmpty()) {
            // You can implement logic to determine image orientation
            // For now, defaulting to SQUARE
            firstImageType = "SQUARE";
        }
        
        // Create votes map (empty initially)
        Map<String, Boolean> votes = new HashMap<>();
        
        // Create comments list (empty initially)
        List<com.example.food.data.Comment> comments = new ArrayList<>();
        
        Review review = new Review();
        review.setUserId(userId);
        // userName and restaurantName are not stored - they will be fetched dynamically
        review.setRestaurantId(selectedRestaurant.getId());
        review.setCaption(caption);
        review.setDescription(description);
        review.setRating(rating);
        review.setAccuracyPercent(accuracyPercent);
        review.setImageUrls(imageUrls);
        review.setFirstImageType(firstImageType);
        review.setCreatedAt(selectedDate);
        review.setVotes(votes);
        review.setComments(comments);
        
        // Save to Firebase
        reviewService.saveReview(review, new ReviewService.ReviewSaveCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                        resetSubmitButton();
                    });
                }
            }
            
            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error saving review", e);
                        resetSubmitButton();
                    });
                }
            }
        });
    }
    
    /**
     * Clear all form inputs and reset to default state
     */
    private void clearForm() {
        etCaption.setText("");
        etDescription.setText("");
        ratingBar.setRating(0);
        spinnerRestaurant.setSelection(0);
        selectedDate = new Date();
        updateDateDisplay();
        selectedImageUris.clear();
        uploadedImageUrls.clear();
        updateImagePreviews();
    }
    
    /**
     * Reset submit button to enabled state with original text
     */
    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Review");
    }
}
