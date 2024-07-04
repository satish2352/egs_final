package com.sipl.egs2.ui.officer

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX
import com.sipl.egs2.R
import com.sipl.egs2.databinding.ActivityOfficerMainBinding
import com.sipl.egs2.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs2.interfaces.OnLocationStateListener
import com.sipl.egs2.ui.activities.start.LoginActivity
import com.sipl.egs2.ui.registration.ProfileActivity
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.MySharedPref
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.utils.XFileDownloader
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class OfficerMainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener,OnLocationStateListener,OnDownloadDocumentClickListener {

    private lateinit var binding: ActivityOfficerMainBinding
    private lateinit var navController: NavController
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var builder:AlertDialog.Builder
    private lateinit var dialogEnableLocation:AlertDialog

    private var downloadId: Long = -1
    private lateinit var downloadReceiver: BroadcastReceiver
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog= CustomProgressDialog(this)
        try {
            val navView: BottomNavigationView = binding.navView
            navController = findNavController(R.id.nav_host_fragment_activity_officer_main)
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_officer_home,
                    R.id.navigation_officer_reports,
                    R.id.navigation_officer_attendance,
                    R.id.navigation_officer_documents
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            navView.setOnNavigationItemSelectedListener(this)
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (!navController.popBackStack()) {
                        // If there are no more fragments to pop in the back stack,
                        // show the exit confirmation dialog
                        AlertDialog.Builder(this@OfficerMainActivity)
                            .setTitle(resources.getString(R.string.exit))
                            .setMessage(getString(R.string.are_you_sure_you_want_to_exit_app))
                            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                                finish()
                            }
                            .setNegativeButton(resources.getString(R.string.no),null)  // If "No" is clicked, do nothing
                            .show()
                    }

                }
            })
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            noInternetDialog= NoInternetDialog(this)
            ReactiveNetwork
                .observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectivity: Connectivity ->
                    //Log.d("##", "=>" + connectivity.state())
                    if (connectivity.state().toString() == "CONNECTED") {
                        isInternetAvailable = true
                        noInternetDialog.hideDialog()
                    } else {
                        isInternetAvailable = false
                        noInternetDialog.showDialog()
                    }
                }) { throwable: Throwable? -> }
            builder= AlertDialog.Builder(this@OfficerMainActivity)
            builder.setMessage("Location services are disabled. App requires location for core features please enable gps & location.?")
                .setCancelable(false).setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user refuses to enable location services
                    Toast.makeText(
                        this@OfficerMainActivity,
                        "Unable to retrieve location without enabling location services",
                        Toast.LENGTH_LONG
                    ).show()
                }
            dialogEnableLocation = builder.create()

            if (!isLocationEnabled()) {
                //showEnableLocationDialog()
            } else {
                requestLocationUpdates()
                dialogEnableLocation.dismiss()
            }

            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                        Log.d("mytag","onReceive : Complete")
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            handleDownloadCompletion(id)
                        }
                    }
                }
            }

            // Register the BroadcastReceiver
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                AppCompatActivity.RECEIVER_EXPORTED)
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
    private fun requestLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this@OfficerMainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@OfficerMainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    this@OfficerMainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
                )
                Log.d("mytag", "requestLocationUpdates()  return ")
                return
            }
            Log.d("mytag", "requestLocationUpdates() ")

            // Request last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                } ?: run {
                    // Handle case where location is null

                    Toast.makeText(
                        this@OfficerMainActivity, "Unable to retrieve location", Toast.LENGTH_LONG
                    ).show()

                }
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
    private fun isLocationEnabled(): Boolean {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        } catch (e: Exception) {
            return false;
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
    private fun showEnableLocationDialog() {

        dialogEnableLocation.show()
    }
    private fun refreshCurrentFragment(){
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!,true)
        navController.navigate(id)
    }
    private fun requestThePermissions() {

        try {
            PermissionX.init(this@OfficerMainActivity)
                .permissions(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION ,android.Manifest.permission.CAMERA)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        refreshCurrentFragment()
                    } else {
                        Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        requestThePermissions()
        if (!isLocationEnabled()) {
            showEnableLocationDialog()
        }else{
            dialogEnableLocation.dismiss()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu_home,menu)
        return false
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.action_logout)
        {

            try {
                AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to logout ?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Open the location settings to enable GPS
                        val mySharedPref= MySharedPref(this@OfficerMainActivity)
                        mySharedPref.clearAll()
                        val intent= Intent(this@OfficerMainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                        // Handle the case when the user chooses not to enable GPS
                    }
                    .show()
            } catch (e: Exception) {
                Log.d("mytag","Exception "+e.message,e);
                e.printStackTrace()
            }

        }
        if(item.itemId==R.id.action_profile)
        {
            val intent=Intent(this@OfficerMainActivity,ProfileActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle bottom navigation item clicks here
        when (item.itemId) {
            R.id.navigation_officer_home -> {
                navController.navigate(R.id.navigation_officer_home)
                return true
            }
            R.id.navigation_officer_reports -> {
                navController.navigate(R.id.navigation_officer_reports)
                return true
            }
            R.id.navigation_officer_attendance -> {
                navController.navigate(R.id.navigation_officer_attendance)
                return true
            }
            R.id.navigation_officer_documents -> {
                navController.navigate(R.id.navigation_officer_documents)
                return true
            }
        }
        return false
    }

    override fun onLocationStateChange(status: Boolean) {
        if(!status){
            showEnableLocationDialog()
        }else{
            dialogEnableLocation.dismiss()
        }
    }
    private fun handleDownloadCompletion(downloadId: Long) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // File download successful
                    val uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val uri = Uri.parse(uriString)
                    val file = File(uri.path!!)
                    val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

                    // Example action: open the downloaded file
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, "application/pdf")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                        val chooserIntent = Intent.createChooser(intent, "Open PDF with")
                        startActivity(chooserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@OfficerMainActivity,resources.getString(R.string.no_app_available_to_view_pdf),Toast.LENGTH_LONG).show()
                    }catch (e:Exception){
                        Toast.makeText(this@OfficerMainActivity,resources.getString(R.string.error_while_opening_pdf),Toast.LENGTH_LONG).show()
                    }
                } else {
                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    showDownloadFailedDialog(reason)
                }
            }
            progressDialog.dismiss()
            cursor.close()
        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocsActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }
    }
    private fun showDownloadFailedDialog(reason: Int) {
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
            AlertDialog.Builder(this@OfficerMainActivity)
                .setTitle("Download Failed")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Log.d("mytag","OfficerMainActivity: => Exception => ${e.message}",e)
            e.printStackTrace()        }
    }


    override fun onDownloadDocumentClick(url: String,fileName:String) {
        try {
            downloadId = XFileDownloader.downloadFile(this@OfficerMainActivity, url, fileName)
            Log.d("mytag","$downloadId")
            progressDialog.show()
        } catch (e: Exception) {
            Log.d("mytag","OfficerMainActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }

    }
}