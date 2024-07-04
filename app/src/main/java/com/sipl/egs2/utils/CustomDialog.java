package com.sipl.egs2.utils;

import android.app.Dialog;
import android.content.Context;

import com.sipl.egs2.R;

public class CustomDialog extends Dialog {
    public CustomDialog(Context context) {
        super(context, R.style.CustomDialogTheme);
        getWindow().getDecorView().getRootView().setBackgroundResource(android.R.color.transparent);
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> insets.consumeSystemWindowInsets());
    }
}