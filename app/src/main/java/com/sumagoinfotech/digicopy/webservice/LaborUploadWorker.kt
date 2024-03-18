package com.sumagoinfotech.digicopy.webservice

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.entity.Labour
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.FileOutputStream

class LaborUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val apiService = ApiClient.create(context)

    override suspend fun doWork(): Result {
        Log.d("mytag","Inside dowork")
        return try {
            val laborRegistrations = getLaborRegistrationsFromDatabase()
            laborRegistrations.forEach { laborRegistration ->
                Log.d("mytag","Inside dowork 2")
                val addNameToFile = addNamesToUri(laborRegistration)
                val files = createFileParts(addNameToFile)
              /*  val response = apiService.uploadLaborInfo(
                    full_name = laborRegistration.fullName,
                    gender_id = laborRegistration.fullName,
                    date_of_birth = laborRegistration.fullName,
                    skill_id = laborRegistration.fullName,
                    district_id = laborRegistration.fullName,
                    taluka_id = laborRegistration.fullName,
                    village_id = laborRegistration.fullName,
                    mobile_number = laborRegistration.fullName,
                    mgnrega_card_id = laborRegistration.mgnregaId,
                    landline_number = laborRegistration.landline,
                    family = laborRegistration.familyDetails,
                    longitude = laborRegistration.location,
                    latitude = laborRegistration.location,
                    files = files
                )*/
                /*response.enqueue(object : Callback<String>{
                    override fun onResponse(
                        call: Call<String>,
                        response: retrofit2.Response<String>
                    ) {
                        Log.d("mytag"," Response ===> "+Gson().toJson(response))
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.d("mytag"," onFailure ===> "+Gson().toJson(response))
                    }
                })*/

            }

            Result.success()
        } catch (e: Exception) {
            Log.d("mytag","Exception doWork "+e.message)
            e.printStackTrace()

            Result.failure()
        }
    }

    private suspend fun getLaborRegistrationsFromDatabase(): List<Labour> {
        val list = AppDatabase.getDatabase(applicationContext).labourDao().getAllLabour()
        return list
    }
    private suspend fun createFileParts(fileInfo: List<FileInfo>): List<MultipartBody.Part> {
        val fileParts = mutableListOf<MultipartBody.Part>()
        fileInfo.forEach { fileItem ->
            val file: File? = uriToFile(applicationContext, fileItem.fileUri)
            if (file != null && file.exists()) {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val part = MultipartBody.Part.createFormData(fileItem.fileName, file.name, requestFile)
                fileParts.add(part)
                Log.d("mytag", "Added file: ${fileItem.fileName}, Path: ${file.absolutePath}")
            } else {
                Log.d("mytag", "File not found or inaccessible: ${fileItem.fileName}")
            }
        }
        return fileParts
    }
    private suspend fun addNamesToUri(labour: Labour): List<FileInfo> {
        val fileInfo = mutableListOf<FileInfo>()
        fileInfo.add(FileInfo("aadhar_image", labour.aadharImage))
        fileInfo.add(FileInfo("mgnrega_image", labour.mgnregaIdImage))
        fileInfo.add(FileInfo("profile_image", labour.photo))
        fileInfo.add(FileInfo("voter_image", labour.voterIdImage))
        return fileInfo
    }

    suspend fun uriToFile(context: Context, uri: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache to avoid reading from cache
                    .skipMemoryCache(true) // Skip memory cache
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(requestOptions)
                    .submit()
                    .get()

                // Create a temporary file to store the bitmap
                val file = File(context.cacheDir, "temp_image.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                file // Return the temporary file
            } catch (e: Exception) {
                Log.d("mytag", "Exception uriToFile: ${e.message}")
                null // Return null if there's an error
            }
        }
    }
}
