package com.sipl.egs.utils

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import com.sipl.egs.R

class NoInternetDialog(private val mContext: Context) {
    private val dialog: Dialog = Dialog(mContext)
    private val ivClose: ImageView

    init {
        dialog.setContentView(R.layout.layout_no_internet_dialog)
        dialog.setCancelable(false)
        ivClose = dialog.findViewById(R.id.ivClose)
        ivClose.setOnClickListener { dialog.dismiss() }
    }

    fun hideDialog() {
        dialog.dismiss()
    }

    fun showDialog() {
        dialog.show()
    }
}

