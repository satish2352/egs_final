package com.sumagoinfotech.digicopy.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ProgressBar
import com.sumagoinfotech.digicopy.R

class CustomProgressDialog(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_custom_dialog)
        setCancelable(false)
    }

    companion object {
        private var progressDialog: CustomProgressDialog? = null

        fun show(context: Context) {
            dismiss()
            progressDialog = CustomProgressDialog(context)
            progressDialog?.show()
        }

        fun dismiss() {
            progressDialog?.dismiss()
            progressDialog = null
        }

        fun isShowing(): Boolean {
            return progressDialog?.isShowing ?: false
        }
    }
}
