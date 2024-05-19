package com.sipl.egs.ui.officer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.R
import com.sipl.egs.databinding.ActivityOfficerMainBinding
import com.sipl.egs.interfaces.OnLocationStateListener
import com.sipl.egs.ui.activities.start.LoginActivity
import com.sipl.egs.ui.registration.ProfileActivity
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.NoInternetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OfficerMainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener,OnLocationStateListener {

    private lateinit var binding: ActivityOfficerMainBinding
    private lateinit var navController: NavController
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var builder:AlertDialog.Builder
    private lateinit var dialogEnableLocation:AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
}