package com.sipl.egs.config

import android.app.Application
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.utils.UnhandledExceptionHandler

class MyApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(UnhandledExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()))
    }
}