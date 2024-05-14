package com.sipl.egs.webservice

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object UploadManager {
    fun startUploadTask(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequestBuilder<LaborUploadWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(uploadRequest)
    }
}
