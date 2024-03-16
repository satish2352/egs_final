package com.sumagoinfotech.digicopy.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.AppDatabase.Companion.getDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.MaritalStatusDao
import com.sumagoinfotech.digicopy.database.dao.SkillsDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.MaritalStatus
import com.sumagoinfotech.digicopy.database.entity.Skills
import com.sumagoinfotech.digicopy.databinding.ActivitySplashBinding
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusData
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.LabourData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsData
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import com.sumagoinfotech.digicopy.utils.MySharedPref
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var userDao: UserDao
    private lateinit var documentTypeDao: DocumentTypeDao
    private lateinit var areaDao: AreaDao
    private lateinit var skillsDao: SkillsDao
    private lateinit var maritalStatusDao: MaritalStatusDao
    private lateinit var mySharedPref:MySharedPref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mySharedPref = MySharedPref(this)
        appDatabase=AppDatabase.getDatabase(this)
        documentTypeDao=appDatabase.documentTypeDao()
        userDao=appDatabase.userDao()
        skillsDao=appDatabase.skillsDao()
        areaDao=appDatabase.areaDao()
        maritalStatusDao=appDatabase.martialStatusDao()
        binding.progressBar.visibility = View.VISIBLE
           CoroutineScope(Dispatchers.IO).launch {
               userDao.insertInitialRecords()
               documentTypeDao.insertInitialRecords()
               if(!mySharedPref.getAllAreaEntries())
                   if(areaDao.getAllArea().isEmpty())
                   {
                       val items = readJsonFromAssets(this@SplashActivity, "address.json")
                       areaDao.insertInitialRecords(items)
                       val size=areaDao.getAllArea().size;
                       Log.d("mytag","Area Entries $size")
                       if(size==44342){
                           mySharedPref.setAllAreaEntries(true)
                       }else{
                           mySharedPref.setAllAreaEntries(false)
                       }
                   }else{
                       Log.d("mytag","Not empty")
                   }
               withContext(Dispatchers.Main) {
                   val mySharedPref=MySharedPref(this@SplashActivity)
                   if(mySharedPref.getIsLoggedIn()){
                       binding.progressBar.visibility = View.GONE
                       val intent= Intent(this@SplashActivity,MainActivity::class.java)
                       intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                       startActivity(intent)
                       finish()
                   }else{
                       binding.progressBar.visibility = View.GONE
                       val intent= Intent(this@SplashActivity,LoginActivity::class.java)
                       intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                       startActivity(intent)
                       finish()
                   }

               }
       }
        CoroutineScope(Dispatchers.IO).launch {
            fetchSkillsFromServer()
            fetchMaritalStatusFromServer()
        }
    }
    private  fun readJsonFromAssets(context: Context, fileName: String): List<AreaItem> {
        val items: MutableList<AreaItem> = mutableListOf()
        try {
            // Step 3: Open and read the JSON file using the AssetManager
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            // Step 4: Parse JSON data using Gson
            val gson = Gson()
            val jsonContent = bufferedReader.use { it.readText() }
            val itemList = gson.fromJson(jsonContent, Array<AreaItem>::class.java)

            // Step 5: Convert JSON data into a list of objects
            items.addAll(itemList)

            // Close the input stream
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("mytag","readJsonFromAssets "+e.message)
        }
        return items
    }

    private fun fetchSkillsFromServer(){
        val apiService= ApiClient.create(this@SplashActivity)
        apiService.getSkills().enqueue(object :
            Callback<SkillsModel> {
            override fun onResponse(
                call: Call<SkillsModel>,
                response: Response<SkillsModel>
            ) {

                if(response.isSuccessful){
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val skillList=response.body()?.data
                        val convertedSkillsList=mapApiResponseListToRoomEntitySkillsList(skillList!!)
                        CoroutineScope(Dispatchers.IO).launch {
                            skillsDao.insertInitialRecords(convertedSkillsList)
                        }
                    }else {
                        Toast.makeText(this@SplashActivity, "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Toast.makeText(this@SplashActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<SkillsModel>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun mapApiResponseListToRoomEntitySkillsList(apiResponseList: List<SkillsData>): List<Skills> {
        return apiResponseList.map { apiResponse ->
            Skills(
                id = apiResponse.id,
                skills = apiResponse.skills,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }
    fun mapToMaritalStatus(apiResponseList: List<MaritalStatusData>): List<MaritalStatus> {
        return apiResponseList.map { apiResponse ->
            MaritalStatus(
                id = apiResponse.id,
                maritalstatus = apiResponse.maritalstatus,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }
    private fun fetchMaritalStatusFromServer(){
        val apiService= ApiClient.create(this@SplashActivity)
        apiService.getMaritalStatus().enqueue(object :
            Callback<MaritalStatusModel> {
            override fun onResponse(
                call: Call<MaritalStatusModel>,
                response: Response<MaritalStatusModel>
            ) {

                if(response.isSuccessful){
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        val convertedList=mapToMaritalStatus(list!!)
                        CoroutineScope(Dispatchers.IO).launch {
                            maritalStatusDao.insertInitialRecords(convertedList)
                        }
                    }else {
                        Toast.makeText(this@SplashActivity, "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Toast.makeText(this@SplashActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<MaritalStatusModel>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
            }
        })

    }

}