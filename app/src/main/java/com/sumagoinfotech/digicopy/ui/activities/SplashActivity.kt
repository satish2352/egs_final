package com.sumagoinfotech.digicopy.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.AppDatabase.Companion.getDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var userDao: UserDao
    private lateinit var documentTypeDao: DocumentTypeDao
    private lateinit var areaDao: AreaDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        appDatabase=AppDatabase.getDatabase(this)
        userDao=appDatabase.userDao()
        documentTypeDao=appDatabase.documentTypeDao()
        areaDao=appDatabase.areaDao()
        binding.progressBar.visibility = View.VISIBLE

        // Execute database insertion in a background thread
        Executors.newSingleThreadExecutor().execute {
            // Insert initial records
            CoroutineScope(Dispatchers.IO).launch {
                userDao.insertInitialRecords()
                documentTypeDao.insertInitialRecords()
                val items = readJsonFromAssets(this@SplashActivity, "address.json")
                areaDao.insertAll(items)

                val list=areaDao.getAllDistrict();
                Log.d("mytag","list"+list.size)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    val intent= Intent(this@SplashActivity,LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()

                    // Start next activity or perform other operations
                }
            }
        }
    }
    fun readJsonFromAssets(context: Context, fileName: String): List<AreaItem> {
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
        }
        return items
    }
}