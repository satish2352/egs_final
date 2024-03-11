package com.sumagoinfotech.digicopy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.AppDatabase.Companion.getDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var userDao: UserDao
    private lateinit var documentTypeDao: DocumentTypeDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDatabase=AppDatabase.getDatabase(this)
        userDao=appDatabase.userDao()
        documentTypeDao=appDatabase.documentTypeDao()
        binding.progressBar.visibility = View.VISIBLE

        // Execute database insertion in a background thread
        Executors.newSingleThreadExecutor().execute {
            // Insert initial records
            CoroutineScope(Dispatchers.IO).launch {
                userDao.insertInitialRecords()
                documentTypeDao.insertInitialRecords()
                // Database insertion completed, hide progress bar and proceed to next activity
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
}