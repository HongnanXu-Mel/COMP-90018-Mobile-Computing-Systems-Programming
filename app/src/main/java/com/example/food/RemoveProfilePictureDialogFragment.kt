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
        
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelRemove)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btnConfirmRemove)
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnConfirm.setOnClickListener {
            listener?.onRemoveConfirmed()
            dismiss()
        }
        
        return dialog
    }
}
