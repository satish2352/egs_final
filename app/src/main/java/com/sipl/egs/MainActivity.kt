package com.sipl.egs

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
import com.sipl.egs.databinding.ActivityMainBinding
import com.sipl.egs.interfaces.OnLocationStateListener
import com.sipl.egs.ui.activities.start.LoginActivity
import com.sipl.egs.ui.registration.ProfileActivity
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.NoInternetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(),OnLocationStateListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var builder:AlertDialog.Builder
    private lateinit var dialogEnableLocation:AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            val navView: BottomNavigationView = binding.navView
            navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_sync, R.id.navigation_dashboard, R.id.navigation_attendence,R.id.navigation_document,R.id.navigation_reports
                )
            )
            noInternetDialog= NoInternetDialog(this)
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
                        noInternetDialog.showDialog()
                    }
                }) { throwable: Throwable? -> }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            requestThePermissions()
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (!navController.popBackStack()) {
                        // If there are no more fragments to pop in the back stack,
                        // show the exit confirmation dialog
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Exit")
                            .setMessage("Are you sure you want to exit App?")
                            .setPositiveButton("Yes") { _, _ ->
                                finish()
                            }
                            .setNegativeButton("No", null) // If "No" is clicked, do nothing
                            .show()
                    }
                }
            })
            if (!isLocationEnabled()) {

            } else {
                requestLocationUpdates()
            }
            builder= AlertDialog.Builder(this@MainActivity)
            builder.setMessage("Location services are disabled. App requires location for core features please enable gps & location.?")
                .setCancelable(false).setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user refuses to enable location services
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to retrieve location without enabling location services",
                        Toast.LENGTH_LONG
                    ).show()
                }
             dialogEnableLocation = builder.create()

        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message)
        }
    }
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
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
                        this@MainActivity, "Unable to retrieve location", Toast.LENGTH_LONG
                    ).show()

            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun refreshCurrentFragment(){
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!,true)
        navController.navigate(id)
    }

    private fun requestThePermissions() {

        PermissionX.init(this@MainActivity)
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
    }
    private fun checkAndPromptGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, prompt the user to enable it
            AlertDialog.Builder(this)
                .setMessage(" Please enable GPS on your device")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()
        }
    }
    override fun onResume() {
        super.onResume()
        if(!isLocationEnabled()){
            showEnableLocationDialog()
        }else{
            dialogEnableLocation.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu_home,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.action_logout)
        {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    val mySharedPref=MySharedPref(this@MainActivity)
                    mySharedPref.clearAll()
                    val intent=Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()

        }
        if(item.itemId==R.id.action_profile)
        {
            val intent=Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
    private fun showEnableLocationDialog() {

       dialogEnableLocation.show()
    }

    override fun onLocationStateChange(status: Boolean) {
        if(!status){
            showEnableLocationDialog()
        }else{
            dialogEnableLocation.dismiss()
        }
    }

}