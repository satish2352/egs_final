package com.sipl.egs.utils;

import android.app.Dialog;
import android.content.Context;

import com.sipl.egs.R;

public class CustomDialog extends Dialog {
    public CustomDialog(Context context) {
        super(context, R.style.CustomDialogTheme);
        getWindow().getDecorView().getRootView().setBackgroundResource(android.R.color.transparent);
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> insets.consumeSystemWindowInsets());
    }
}