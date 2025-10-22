package com.example.food;

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
    private static final String TAG = "AddFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private EditText etCaption, etDescription;
    private RatingBar ratingBar;
    private Spinner spinnerRestaurant;
    private TextView tvSelectedDate;
    private Button btnSelectDate, btnSelectImages, btnSubmit;
    private ImageView ivPreview1, ivPreview2, ivPreview3;
    
    private List<Restaurant> restaurants;
    private Restaurant selectedRestaurant;
    private Date selectedDate;
    private List<Uri> selectedImageUris;
    private List<String> uploadedImageUrls;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ReviewService reviewService;

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
    
    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectImages.setOnClickListener(v -> selectImages());
        btnSubmit.setOnClickListener(v -> submitReview());
        
        spinnerRestaurant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip the first "Select Restaurant" item
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
    
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reviewService = new ReviewService();
    }
    
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
    
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate));
    }
    
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }
    
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
    
    private void submitReview() {
        if (!validateInput()) {
            return;
        }
        
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");
        
        // Upload images first
        uploadImagesAndCreateReview();
    }
    
    private boolean validateInput() {
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
    
    private void uploadImagesAndCreateReview() {
        if (selectedImageUris.isEmpty()) {
            createReview(new ArrayList<>());
            return;
        }
        
        uploadedImageUrls.clear();
        uploadNextImage(0);
    }
    
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
    
    private void createReview(List<String> imageUrls) {
        String userId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = mAuth.getCurrentUser().getEmail();
        }
        
        String caption = etCaption.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        float rating = ratingBar.getRating();
        
        // Calculate accuracy percent (you can modify this logic as needed)
        double accuracyPercent = 100.0; // Default to 100%
        
        // Determine first image type
        String firstImageType = "SQUARE"; // Default
        if (!imageUrls.isEmpty()) {
            // You can implement logic to determine image orientation
            // For now, defaulting to SQUARE
            firstImageType = "SQUARE";
        }
        
        // Create votes map (empty initially)
        Map<String, Map<String, Object>> votes = new HashMap<>();
        
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
    
    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Review");
    }
}
