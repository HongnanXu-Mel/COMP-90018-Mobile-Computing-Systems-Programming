package com.example.food.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.widget.AppCompatButton;

import com.example.food.R;

public class DeletePostConfirmationDialog extends Dialog {
    
    private Button btnCancel;
    private AppCompatButton btnDelete;
    private OnDeleteConfirmedListener onDeleteConfirmedListener;
    
    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed();
    }
    
    public DeletePostConfirmationDialog(Context context, OnDeleteConfirmedListener listener) {
        super(context);
        this.onDeleteConfirmedListener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_post_confirmation);
        
        setupDialog();
        initializeViews();
        setupClickListeners();
    }
    
    private void setupDialog() {
        if (getWindow() != null) {
            getWindow().setLayout(
                (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
    
    private void initializeViews() {
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
    }
    
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> {
            if (onDeleteConfirmedListener != null) {
                onDeleteConfirmedListener.onDeleteConfirmed();
            }
            dismiss();
        });
    }
}
