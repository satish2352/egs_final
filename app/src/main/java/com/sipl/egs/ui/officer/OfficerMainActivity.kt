package com.sipl.egs.ui.officer

import android.content.Context
import android.content.Intent
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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.R
import com.sipl.egs.databinding.ActivityOfficerMainBinding
import com.sipl.egs.ui.activities.start.LoginActivity
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.NoInternetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OfficerMainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityOfficerMainBinding
    private lateinit var navController: NavController
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
    }
    private fun refreshCurrentFragment(){
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!,true)
        navController.navigate(id)
    }
    private fun requestThePermissions() {

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
        requestThePermissions()
        checkAndPromptGps()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu_home,menu)
        return false
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.action_logout)
        {

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
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()

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
}