package com.example.food

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment

class RemoveProfilePictureDialogFragment : DialogFragment() {
    
    interface OnRemoveConfirmedListener {
        fun onRemoveConfirmed()
    }
    
    private var listener: OnRemoveConfirmedListener? = null
    
    fun setOnRemoveConfirmedListener(listener: OnRemoveConfirmedListener) {
        this.listener = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_remove_profile_picture)
        
        setupDialog(dialog)
        initializeViews(dialog)
        setupClickListeners(dialog)
        
        return dialog
    }
    
    private fun setupDialog(dialog: Dialog) {
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    private fun initializeViews(dialog: Dialog) {
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelRemove)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btnConfirmRemove)
    }
    
    private fun setupClickListeners(dialog: Dialog) {
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelRemove)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btnConfirmRemove)
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnConfirm.setOnClickListener {
            listener?.onRemoveConfirmed()
            dismiss()
        }
    }
}
