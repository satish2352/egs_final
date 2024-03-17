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
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDropDownDao
import com.sumagoinfotech.digicopy.database.dao.GenderDao
import com.sumagoinfotech.digicopy.database.dao.MaritalStatusDao
import com.sumagoinfotech.digicopy.database.dao.RelationDao
import com.sumagoinfotech.digicopy.database.dao.SkillsDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.DocumentTypeDropDown
import com.sumagoinfotech.digicopy.database.entity.MaritalStatus
import com.sumagoinfotech.digicopy.database.entity.Skills
import com.sumagoinfotech.digicopy.databinding.ActivitySplashBinding
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusData
import com.sumagoinfotech.digicopy.model.apis.masters.Documenttype
import com.sumagoinfotech.digicopy.model.apis.masters.Gender
import com.sumagoinfotech.digicopy.model.apis.masters.Maritalstatu
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.model.apis.masters.Relation
import com.sumagoinfotech.digicopy.model.apis.masters.Skill
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsData
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import com.sumagoinfotech.digicopy.utils.MySharedPref
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var userDao: UserDao
    private lateinit var documentTypeDao: DocumentTypeDao
    private lateinit var areaDao: AreaDao
    private lateinit var skillsDao: SkillsDao
    private lateinit var genderDao: GenderDao
    private lateinit var relationDao: RelationDao
    private lateinit var documentTypeDropDownDao: DocumentTypeDropDownDao
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
        genderDao=appDatabase.genderDao()
        relationDao=appDatabase.relationDao()
        documentTypeDropDownDao=appDatabase.documentDropDownDao()
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
               fetchMastersFromServer()
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

    private fun fetchMastersFromServer(){
        val apiService= ApiClient.create(this@SplashActivity)
        apiService.getAllMasters().enqueue(object :
            Callback<MastersModel> {
            override fun onResponse(
                call: Call<MastersModel>,
                response: Response<MastersModel>
            ) {
                if(response.isSuccessful){
                    if(response.body()?.status.equals("success")) {
                        val skillsConverted=mapToSkills(response?.body()?.data?.skills!!)
                        val maritalStatusConverted=mapToMaritalStatus(response?.body()?.data?.maritalstatus!!)
                        val genderConverted=mapToMaritalGender(response?.body()?.data?.gender!!)
                        val relationConverted=mapToRelation(response?.body()?.data?.relation!!)
                        val documentTypeConverted=mapToDocumentType(response?.body()?.data?.documenttype!!)
                        CoroutineScope(Dispatchers.IO).launch {
                            skillsDao.insertInitialRecords(skillsConverted)
                            maritalStatusDao.insertInitialRecords(maritalStatusConverted)
                            genderDao.insertInitialRecords(genderConverted)
                            relationDao.insertInitialRecords(relationConverted)
                            documentTypeDropDownDao.insertInitialRecords(documentTypeConverted)
                        }
                    }else {
                        Log.d("mytag","fetchMastersFromServer:Response Not success")
                        Toast.makeText(this@SplashActivity, "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Log.d("mytag","fetchMastersFromServer:Response unsuccessful")
                    Toast.makeText(this@SplashActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<MastersModel>, t: Throwable) {
                Log.d("mytag","fetchMastersFromServer:onFailure ${t.message}")
                Toast.makeText(this@SplashActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun mapToSkills(apiResponseList: List<Skill>): List<Skills> {
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
    fun mapToMaritalStatus(apiResponseList: List<Maritalstatu>): List<MaritalStatus> {
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
    fun mapToMaritalGender(apiResponseList: List<Gender>): List<com.sumagoinfotech.digicopy.database.entity.Gender> {
        return apiResponseList.map { apiResponse ->
            com.sumagoinfotech.digicopy.database.entity.Gender(
                id = apiResponse.id,
                gender_name = apiResponse.gender_name,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }

    fun mapToRelation(apiResponseList: List<Relation>): List<com.sumagoinfotech.digicopy.database.entity.Relation> {
        return apiResponseList.map { apiResponse ->
            com.sumagoinfotech.digicopy.database.entity.Relation(
                id = apiResponse.id,
                relation_title = apiResponse.relation_title,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }

    fun mapToDocumentType(apiResponseList: List<Documenttype>): List<com.sumagoinfotech.digicopy.database.entity.DocumentTypeDropDown> {
        return apiResponseList.map { apiResponse ->
            com.sumagoinfotech.digicopy.database.entity.DocumentTypeDropDown(
                id = apiResponse.id,
                documenttype = apiResponse.documenttype,
                is_deleted =apiResponse.is_deleted,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }


}