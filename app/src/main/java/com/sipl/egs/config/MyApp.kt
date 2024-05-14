package com.sipl.egs.config

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.utils.UnhandledExceptionHandler

class MyApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate()
        try {
            Thread.setDefaultUncaughtExceptionHandler(UnhandledExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()))
        } catch (e: Exception) {
            Log.d("mytag","MyApp: ${e.message}",e)
        }
    }
}