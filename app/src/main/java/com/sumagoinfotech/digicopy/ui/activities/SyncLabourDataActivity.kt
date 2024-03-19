package com.sumagoinfotech.digicopy.ui.activities

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.model.LabourWithAreaNames
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLabourDataBinding
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.ui.adapters.LabourListAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.FileInfo
import com.sumagoinfotech.digicopy.webservice.UploadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class SyncLabourDataActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySyncLabourDataBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labourList:List<LabourWithAreaNames>
    lateinit var  adapter:LabourListAdapter
    private lateinit var dialog:CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySyncLabourDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_labour_data)
        dialog=CustomProgressDialog(this)
        dialog.show()
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewSyncLabourData.layoutManager=layoutManager
        database= AppDatabase.getDatabase(this)
        labourDao=database.labourDao()
        labourList=ArrayList<LabourWithAreaNames>()
        adapter= LabourListAdapter(labourList)
        adapter.notifyDataSetChanged()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        if(item.itemId==R.id.navigation_sync){

            //UploadManager.startUploadTask(this@SyncLabourDataActivity)
            //syncLabourData()
            CoroutineScope(Dispatchers.IO).launch {
                uploadLabourOnline()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch{
            labourList=labourDao.getLabourWithAreaNames()
            Log.d("mytag","=>"+labourList.size)
            val listWithName=labourDao.getLabourWithAreaNames()
            if(!listWithName.isNullOrEmpty()){

                Log.d("mytag",""+Gson().toJson(listWithName))
            }else{
                Log.d("mytag","Empty Or Nukk ")
            }
            withContext(Dispatchers.Main) {
                dialog.dismiss()
                adapter=LabourListAdapter(labourList)
                binding.recyclerViewSyncLabourData.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+labourList.size)

    }
    override fun onPostResume() {
        super.onPostResume()
    }

    override fun onRestart() {
        super.onRestart()
        dialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            labourList=labourDao.getLabourWithAreaNames()
            Log.d("mytag","=>"+labourList.size)

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                adapter=LabourListAdapter(labourList)
                binding.recyclerViewSyncLabourData.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+labourList.size)
    }

    fun fetchUserList(){
        CoroutineScope(Dispatchers.IO).launch{
            labourList=labourDao.getLabourWithAreaNames()
            Log.d("mytag","=>"+labourList.size)

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                adapter=LabourListAdapter(labourList)
                binding.recyclerViewSyncLabourData.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+labourList.size)
    }




    private suspend fun getLaborRegistrationsFromDatabase(): List<Labour> {
        val list = AppDatabase.getDatabase(applicationContext).labourDao().getAllLabour()
        return list
    }
    private suspend fun createFileParts(fileInfo: List<FileInfo>): List<MultipartBody.Part> {
        val fileParts = mutableListOf<MultipartBody.Part>()
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        fileInfo.forEach { fileItem ->
            val file: File? = uriToFile(applicationContext, fileItem.fileUri)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file!!)
            requestBody.addFormDataPart(fileItem.fileName, file?.name, requestFile)
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
    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
        return file?.let {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
        }
    }

    private suspend fun uploadLabourOnline(){
       runOnUiThread {
           dialog.show()
       }

        val apiService = ApiClient.create(this@SyncLabourDataActivity)
        CoroutineScope(Dispatchers.IO).launch {
            val laborRegistrations = getLaborRegistrationsFromDatabase()
            try {
                laborRegistrations.forEach { laborRegistration ->
                    val aadharCardImage =
                        createFilePart(FileInfo("aadhar_image", laborRegistration.aadharImage))
                    val voterIdImage =
                        createFilePart(FileInfo("voter_image", laborRegistration.aadharImage))
                    val profileImage =
                        createFilePart(FileInfo("profile_image", laborRegistration.aadharImage))
                    val mgnregaIdImage =
                        createFilePart(FileInfo("mgnrega_image", laborRegistration.aadharImage))
                    val response= apiService.uploadLaborInfo(
                        fullName = laborRegistration.fullName,
                        genderId = laborRegistration.gender,
                        dateOfBirth = laborRegistration.dob,
                        skillId = laborRegistration.skill,
                        districtId = laborRegistration.district,
                        talukaId = laborRegistration.taluka,
                        villageId = laborRegistration.village,
                        mobileNumber = laborRegistration.mobile,
                        mgnregaId = laborRegistration.mgnregaId,
                        landLineNumber = laborRegistration.landline,
                        family = laborRegistration.familyDetails,
                        longitude = laborRegistration.latitude,
                        latitude = laborRegistration.longitude,
                        file1 = aadharCardImage!!,
                        file2 = voterIdImage!!,
                        file3 = profileImage!!,
                        file4 = mgnregaIdImage!!)

                    if(response.isSuccessful){
                        if(response.body()?.status.equals("True")){
                            laborRegistration.isSynced=true
                            labourDao.updateLabour(laborRegistration)
                        }else{

                        }
                        Log.d("mytag",""+response.body()?.message)
                        Log.d("mytag",""+response.body()?.status)
                    }else{
                        Log.d("mytag","Labour upload failed  "+laborRegistration.fullName)
                    }
                }
                    runOnUiThread {dialog.dismiss()  }
                fetchUserList()
            } catch (e: Exception) {
              runOnUiThread { dialog.dismiss() }
                Log.d("mytag","uploadLabourOnline "+e.message)
            }
        }

    }

}