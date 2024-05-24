package com.sipl.egs.utils

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object DownloadUtils {

    fun handleDownloadCompletion(
        context: Context,
        downloadId: Long,
        progressDialog: CustomProgressDialog
    ) {
        Log.d("mytag", "handleDownloadCompletion: ")
        try {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriString =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val uri = Uri.parse(uriString)
                    val file = File(uri.path!!)
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    // Example action: open the downloaded file
                    openPdfFile(context, fileUri)
                } else {
                    val reason =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    showDownloadFailedDialog(context, reason)
                }
            }
            progressDialog.dismiss()
            cursor.close()
        } catch (e: Exception) {
            Log.e("mytag", "Exception: ${e.message}", e)
        }
    }

    private fun openPdfFile(context: Context, fileUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "application/pdf")
            intent.flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            val chooserIntent =
                Intent.createChooser(intent, "Open PDF with")
            context.startActivity(chooserIntent)
        }catch (e:ActivityNotFoundException){
            showToast(context, "No app available to view PDF")
            Log.e("mytag", "Exception while opening PDF: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("mytag", "Exception while opening PDF: ${e.message}", e)
            showToast(context, "Unable to open PDF")
        }
    }

    private fun showDownloadFailedDialog(context: Context, reason: Int) {
        try {
            val message = when (reason) {
                DownloadManager.ERROR_CANNOT_RESUME -> "Download cannot resume."
                DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found."
                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists."
                DownloadManager.ERROR_FILE_ERROR -> "File error."
                DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error."
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space."
                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects."
                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code."
                DownloadManager.ERROR_UNKNOWN -> "Unknown error."
                else -> "Download failed."
            }
            AlertDialog.Builder(context)
                .setTitle("Download Failed")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Log.e("mytag", "Exception while opening PDF: ${e.message}", e)
            //showToast(context, "No app available to view PDF")
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
