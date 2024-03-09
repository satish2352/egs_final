package com.sumagoinfotech.digicopy.config

import android.app.Application
import com.sumagoinfotech.digicopy.database.AppDatabase

class MyApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}