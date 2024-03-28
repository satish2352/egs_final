package com.sumagoinfotech.digicopy.config

import android.app.Application
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.utils.UnhandledExceptionHandler

class MyApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(UnhandledExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()))
    }
}