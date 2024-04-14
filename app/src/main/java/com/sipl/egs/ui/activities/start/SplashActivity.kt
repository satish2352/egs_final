package com.sipl.egs.ui.activities.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egs.MainActivity
import com.sipl.egs.R
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.AreaDao
import com.sipl.egs.database.dao.DocumentReasonsDao
import com.sipl.egs.database.dao.DocumentTypeDropDownDao
import com.sipl.egs.database.dao.GenderDao
import com.sipl.egs.database.dao.MaritalStatusDao
import com.sipl.egs.database.dao.ReasonsDao
import com.sipl.egs.database.dao.RegistrationStatusDao
import com.sipl.egs.database.dao.RelationDao
import com.sipl.egs.database.dao.SkillsDao
import com.sipl.egs.database.dao.UserDao
import com.sipl.egs.database.entity.AreaItem
import com.sipl.egs.database.entity.MaritalStatus
import com.sipl.egs.database.entity.Skills
import com.sipl.egs.databinding.ActivitySplashBinding
import com.sipl.egs.model.apis.masters.Documenttype
import com.sipl.egs.model.apis.masters.Gender
import com.sipl.egs.model.apis.masters.Maritalstatu
import com.sipl.egs.model.apis.masters.MastersModel
import com.sipl.egs.model.apis.masters.Reasons
import com.sipl.egs.model.apis.masters.RegistrationStatus
import com.sipl.egs.model.apis.masters.Relation
import com.sipl.egs.model.apis.masters.Skill
import com.sipl.egs.model.apis.mastersupdate.AreaMaster
import com.sipl.egs.model.apis.mastersupdate.AreaMastersUpdateModel
import com.sipl.egs.ui.officer.OfficerMainActivity
import com.sipl.egs.utils.DeviceUtils
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var userDao: UserDao
    private lateinit var areaDao: AreaDao
    private lateinit var skillsDao: SkillsDao
    private lateinit var genderDao: GenderDao
    private lateinit var relationDao: RelationDao
    private lateinit var registrationStatusDao: RegistrationStatusDao
    private lateinit var documentTypeDropDownDao: DocumentTypeDropDownDao
    private lateinit var maritalStatusDao: MaritalStatusDao
    private lateinit var reasonsDao: ReasonsDao
    private lateinit var documentReasonsDao: DocumentReasonsDao
    private lateinit var mySharedPref: MySharedPref
    private var allMastersCompleted = false
    private var updateMasterCompleted = false
    private lateinit var noInternetDialog:NoInternetDialog
    private  var isInternetAvailable:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            noInternetDialog=NoInternetDialog(this)
            mySharedPref = MySharedPref(this)
            appDatabase = AppDatabase.getDatabase(this)
            userDao = appDatabase.userDao()
            skillsDao = appDatabase.skillsDao()
            areaDao = appDatabase.areaDao()
            genderDao = appDatabase.genderDao()
            relationDao = appDatabase.relationDao()
            documentReasonsDao = appDatabase.documentsReasonsDao()
            registrationStatusDao = appDatabase.registrationStatusDao()
            documentTypeDropDownDao = appDatabase.documentDropDownDao()
            maritalStatusDao = appDatabase.martialStatusDao()
            reasonsDao = appDatabase.reasonsDao()
            ReactiveNetwork
                .observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectivity: Connectivity ->
                    Log.d("##", "=>" + connectivity.state())
                    if (connectivity.state().toString() == "CONNECTED") {
                        isInternetAvailable = true
                        noInternetDialog.hideDialog()

                    } else {
                        isInternetAvailable = false


                    }
                }) { throwable: Throwable? -> }
            val deviceId = DeviceUtils.getDeviceId(this@SplashActivity)
            mySharedPref.setDeviceId(deviceId)
            binding.progressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                val deviceId = DeviceUtils.getDeviceId(this@SplashActivity)
                mySharedPref.setDeviceId(deviceId)
                userDao.insertInitialRecords()
                checkAllCounts();
                if (!mySharedPref.getAllAreaEntries()) {
                    if (areaDao.getAllArea().size < 44342) {
                        val areaEnteriesJob = async {
                            val items = readJsonFromAssets(this@SplashActivity, "address.json")
                            areaDao.insertInitialRecords(items)
                            val size = areaDao.getAllArea().size;
                            Log.d("mytag", "Area Entries $size")
                            if (size == 44342) {
                                mySharedPref.setAllAreaEntries(true)
                            } else {
                                mySharedPref.setAllAreaEntries(false)
                            }
                        }
                        areaEnteriesJob.await()
                    } else {
                        Log.d("mytag", "Not empty")
                    }
                }


                fetchAndInsertDataFromApi()

                withContext(Dispatchers.Main) {

                    if (mySharedPref.getAtLeastSingleTimeEntriesAdded() == true) {

                    }else{
                        if(!isInternetAvailable){
                            noInternetDialog.showDialog()
                        }
                        binding.buttonRetry.visibility=View.VISIBLE
                        binding.buttonRetry.setOnClickListener {
                            if(isInternetAvailable){
                                CoroutineScope(Dispatchers.IO).launch {
                                    fetchAndInsertDataFromApi()
                                }
                            }else{
                                noInternetDialog.showDialog()
                            }

                        }

                    }


                }
            }
        } catch (e: Exception) {
            Log.d("mytag", "SplashActivity: ${e.message}", e)

        }
    }
    private fun navigateToNextActivity(){
        val mySharedPref = MySharedPref(this@SplashActivity)
        runOnUiThread {
            if (mySharedPref.getIsLoggedIn() && mySharedPref.getRoleId() == 3) {
                binding.progressBar.visibility = View.GONE
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else if (mySharedPref.getIsLoggedIn() && mySharedPref.getRoleId() == 2) {
                binding.progressBar.visibility = View.GONE
                val intent =
                    Intent(this@SplashActivity, OfficerMainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                binding.progressBar.visibility = View.GONE
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private suspend fun fetchAndInsertDataFromApi() {

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("mytag", "Before Await")
            val fetchAllMastersJob = async { fetchMastersFromServer() }
            val fetchAreaMastersJob = async { fetchAreaMastersToUpdateFromServer() }
            allMastersCompleted = fetchAllMastersJob.await()
            updateMasterCompleted = fetchAreaMastersJob.await()
            // Use the results here
            if (allMastersCompleted == true && updateMasterCompleted) {
                Log.d("mytag", "Both Complete ")

                runOnUiThread {
                    mySharedPref.setAtLeastSingleTimeEntriesAdded(true)
                    binding.progressBar.visibility = View.GONE
                    navigateToNextActivity()
                }
            } else {
                Log.d("mytag", "AnyOne Left")
                if (!allMastersCompleted!!) {
                    val fetchAllMastersJob = async { fetchMastersFromServer() }
                    val allMastersResult = fetchAllMastersJob.await()
                    allMastersCompleted = allMastersResult!!

                }
                if (!updateMasterCompleted) {
                    val fetchAreaMastersJob = async { fetchAreaMastersToUpdateFromServer() }
                    val updateMastersResult = fetchAreaMastersJob.await()
                    updateMasterCompleted = updateMastersResult
                }

                if(mySharedPref.getAtLeastSingleTimeEntriesAdded()==true){
                    navigateToNextActivity()
                }
            }
            Log.d("mytag", "After  Await")
        }
    }

    private suspend fun checkAllCounts(): Boolean {
        var list = mutableListOf<Boolean>()
        if (reasonsDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (skillsDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (genderDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (registrationStatusDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (documentReasonsDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (documentTypeDropDownDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (maritalStatusDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (relationDao.getRowCount() < 1) {
            list.add(false)
        } else {
            list.add(true)
        }

        return !list.contains(false)

    }

    private fun readJsonFromAssets(context: Context, fileName: String): List<AreaItem> {
        val items: MutableList<AreaItem> = mutableListOf()
        try {
            // Step : Open and read the JSON file using the AssetManager
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            // Step : Parse JSON data using Gson
            val gson = Gson()
            val jsonContent = bufferedReader.use { it.readText() }
            val itemList = gson.fromJson(jsonContent, Array<AreaItem>::class.java)

            // Step : Convert JSON data into a list of objects
            items.addAll(itemList)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("mytag", "readJsonFromAssets: ${e.message}", e)

        }
        return items
    }

    private suspend fun fetchMastersFromServer(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                val apiService = ApiClient.create(this@SplashActivity)
                apiService.getAllMasters().enqueue(object :
                    Callback<MastersModel> {
                    override fun onResponse(
                        call: Call<MastersModel>,
                        response: Response<MastersModel>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.status.equals("success")) {
                                Log.d("mytag", "fetchMastersFromServer:success")
                                val skillsConverted =
                                    mapToSkills(response?.body()?.data?.skills!!)
                                val maritalStatusConverted =
                                    mapToMaritalStatus(response?.body()?.data?.maritalstatus!!)
                                val genderConverted =
                                    mapToMaritalGender(response?.body()?.data?.gender!!)
                                val relationConverted =
                                    mapToRelation(response?.body()?.data?.relation!!)
                                val documentTypeConverted =
                                    mapToDocumentType(response?.body()?.data?.documenttype!!)
                                val registrationStatusConverted =
                                    mapToRegistrationStatus(response?.body()?.data?.registrationstatus!!)
                                val reasonsConverted =
                                    mapToReasons(response?.body()?.data?.reasons!!)
                                val documentReasonsConverted =
                                    mapToDocumentReasons(response?.body()?.data?.documentreasons!!)
                                CoroutineScope(Dispatchers.IO).launch {
                                    skillsDao.insertInitialRecords(skillsConverted)
                                    maritalStatusDao.insertInitialRecords(maritalStatusConverted)
                                    genderDao.insertInitialRecords(genderConverted)
                                    relationDao.insertInitialRecords(relationConverted)
                                    documentTypeDropDownDao.insertInitialRecords(
                                        documentTypeConverted
                                    )
                                    registrationStatusDao.insertInitialRecords(
                                        registrationStatusConverted
                                    )
                                    registrationStatusDao.insertInitialRecords(
                                        registrationStatusConverted
                                    )
                                    reasonsDao.insertInitialRecords(reasonsConverted)
                                    documentReasonsDao.insertInitialRecords(documentReasonsConverted)

                                    // Once all operations are done, resume the coroutine with the response
                                    continuation.resume(true)
                                }
                            } else {
                                Log.d("mytag", "fetchMastersFromServer:Response Not success")
                                Toast.makeText(
                                    this@SplashActivity,
                                    resources.getString(R.string.no_records_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                continuation.resume(false) // Or you can throw an exception if you want to handle the error differently
                            }
                        } else {
                            Log.d("mytag", "fetchMastersFromServer:Response unsuccessful")
                            Toast.makeText(
                                this@SplashActivity,
                                resources.getString(R.string.response_unsuccessfull),
                                Toast.LENGTH_SHORT
                            ).show()
                            continuation.resume(false) // Or you can throw an exception if you want to handle the error differently
                        }
                    }

                    override fun onFailure(call: Call<MastersModel>, t: Throwable) {
                        Log.d("mytag", "fetchMastersFromServer:onFailure ${t.message}")
                        Toast.makeText(
                            this@SplashActivity,
                            resources.getString(R.string.error_occured_during_api_call),
                            Toast.LENGTH_SHORT
                        ).show()
                        continuation.resume(false) // Or you can throw an exception if you want to handle the error differently
                    }
                })
            } catch (e: Exception) {
                Log.d("mytag", "SplashActivity: ${e.message}", e)
                e.printStackTrace()
                continuation.resume(false) // Or you can throw an exception if you want to handle the error differently
            }
        }
    }


    private suspend fun fetchAreaMastersToUpdateFromServer(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                val apiService = ApiClient.create(this@SplashActivity)
                apiService.getAreaMastersToUpdate().enqueue(object :
                    Callback<AreaMastersUpdateModel> {
                    override fun onResponse(
                        call: Call<AreaMastersUpdateModel>,
                        response: Response<AreaMastersUpdateModel>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.status.equals("true")) {
                                if (response.body()?.data?.size!! > 0) {
                                    Log.d("mytag", "Update Size =>" + response.body()?.data?.size)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val areaDataConverted =
                                            mapDataToArea(response?.body()?.data!!)
                                        areaDataConverted.forEach { entity ->
                                            val existingEntity =
                                                areaDao.getAreaByLocationId(entity.location_id)
                                            if (existingEntity != null) {
                                                // Update existing entity
                                                entity.id = existingEntity.id
                                                areaDao.update(entity)
                                            } else {
                                                // Insert new entity
                                                areaDao.insert(entity)
                                            }
                                        }
                                        // Notify the caller that the operation is completed successfully
                                        continuation.resume(true)
                                    }
                                } else {
                                    Log.d(
                                        "mytag",
                                        "No records found in area masters update =>" + response.body()?.data?.size
                                    )
                                    // Notify the caller that there are no records found
                                    continuation.resume(false)
                                }
                            } else {
                                Log.d(
                                    "mytag",
                                    "fetchAreaMastersToUpdateFromServer:Response Not success"
                                )
                                Toast.makeText(
                                    this@SplashActivity,
                                    resources.getString(R.string.no_records_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Notify the caller about the unsuccessful response
                                continuation.resume(false)
                            }
                        } else {
                            Log.d(
                                "mytag",
                                "fetchAreaMastersToUpdateFromServer:Response unsuccessful"
                            )
                            Toast.makeText(
                                this@SplashActivity,
                                resources.getString(R.string.response_unsuccessfull),
                                Toast.LENGTH_SHORT
                            ).show()
                            // Notify the caller about the unsuccessful response
                            continuation.resume(false)
                        }
                    }

                    override fun onFailure(call: Call<AreaMastersUpdateModel>, t: Throwable) {
                        Log.d("mytag", "fetchAreaMastersToUpdateFromServer:onFailure ${t.message}")
                        Toast.makeText(
                            this@SplashActivity,
                            resources.getString(R.string.error_occured_during_api_call),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Notify the caller about the failure
                        continuation.resume(false)
                    }
                })
            } catch (e: Exception) {
                Log.d("mytag", "SplashActivity: ${e.message}")
                Log.d("mytag", "SplashActivity: ${e.message}", e)
                e.printStackTrace()
                // Notify the caller about the exception
                continuation.resume(false)
            }
        }
    }

    fun mapDataToArea(apiResponseList: List<AreaMaster>): List<AreaItem> {
        return apiResponseList.map { apiResponse ->
            AreaItem(
                parent_id = apiResponse.parent_id.toString(),
                is_active = apiResponse.is_active,
                is_visible = apiResponse.is_visible,
                location_id = apiResponse.location_id.toString(),
                location_type = apiResponse.location_type,
                name = apiResponse.name
            )
        }
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

    fun mapToMaritalGender(apiResponseList: List<Gender>): List<com.sipl.egs.database.entity.Gender> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.Gender(
                id = apiResponse.id,
                gender_name = apiResponse.gender_name,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }

    fun mapToRelation(apiResponseList: List<Relation>): List<com.sipl.egs.database.entity.Relation> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.Relation(
                id = apiResponse.id,
                relation_title = apiResponse.relation_title,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }

    fun mapToDocumentType(apiResponseList: List<Documenttype>): List<com.sipl.egs.database.entity.DocumentTypeDropDown> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.DocumentTypeDropDown(
                id = apiResponse.id,
                documenttype = apiResponse.document_type_name,
                is_deleted = apiResponse.is_deleted,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at,
                doc_color = apiResponse.doc_color
            )
        }
    }

    fun mapToRegistrationStatus(apiResponseList: List<RegistrationStatus>): List<com.sipl.egs.database.entity.RegistrationStatus> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.RegistrationStatus(
                id = apiResponse.id,
                is_deleted = apiResponse.is_deleted,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at,
                status_name = apiResponse.status_name
            )
        }
    }

    fun mapToReasons(apiResponseList: List<Reasons>): List<com.sipl.egs.database.entity.Reasons> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.Reasons(
                id = apiResponse.id,
                is_deleted = apiResponse.is_deleted,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at,
                reason_name = apiResponse.reason_name
            )
        }
    }

    fun mapToDocumentReasons(apiResponseList: List<Reasons>): List<com.sipl.egs.database.entity.DocumentReasons> {
        return apiResponseList.map { apiResponse ->
            com.sipl.egs.database.entity.DocumentReasons(
                id = apiResponse.id,
                is_deleted = apiResponse.is_deleted,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at,
                reason_name = apiResponse.reason_name
            )
        }
    }


}