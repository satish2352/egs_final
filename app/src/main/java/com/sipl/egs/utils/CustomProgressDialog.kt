package com.sipl.egs.utils

import android.app.Activity
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import com.sipl.egs.R

class CustomProgressDialog(private val context: Context) {
    private val dialog: CustomDialog
    private var isDialogVisible: Boolean = false // Track dialog visibility

    init {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.layout_custom_dialog, null)
        // cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))
        // Progress Bar Color
        // Text Color
        // cpTitle.setTextColor(Color.WHITE)
        // Custom Dialog initialization
        dialog = CustomDialog(context)
        dialog.setContentView(view)
        isDialogVisible = false // Initially set to false

    }

    public fun show() {
        dialog.show()
        isDialogVisible = true // U
    }

    public fun dismiss() {
        dialog.dismiss()
        isDialogVisible = false // Update dialog visibility status

    }
    fun isDialogVisible(): Boolean {
        return isDialogVisible
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}
