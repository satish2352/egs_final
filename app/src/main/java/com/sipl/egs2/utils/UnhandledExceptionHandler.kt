package com.sipl.egs2.utils

import android.util.Log

class UnhandledExceptionHandler(private val defaultExceptionHandler: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {
    private val TAG = "mytag"

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Your custom exception handling logic goes here
        Log.e(TAG, "Unhandled exception occurred!", throwable)
        throwable.printStackTrace()

        // Optionally, you can log the exception details or send them to a crash reporting tool
        // Crashlytics.logException(throwable)

        // You may want to perform cleanup or save any necessary data before the app crashes
        // ...

        // If you want to allow the default uncaught exception handler to handle the exception as well, uncomment the following line
         defaultExceptionHandler?.uncaughtException(thread, throwable)

        // Finish the app to avoid further instability
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }
}
