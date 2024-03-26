package com.sumagoinfotech.digicopy.utils

import android.content.Context
import android.provider.Settings

object DeviceUtils {

    // Method to get the unique device ID
    fun getDeviceId(context: Context): String {
        // Getting the device ID
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return deviceId
    }
}