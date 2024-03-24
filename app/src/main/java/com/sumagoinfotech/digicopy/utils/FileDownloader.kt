package com.sumagoinfotech.digicopy.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment


object FileDownloader {
    fun downloadFile(context: Context, fileUrl: String?, fileName: String?) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle(fileName)
        request.setDescription("Downloading")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager?.enqueue(request)
    }
}

