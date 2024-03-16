package com.sumagoinfotech.digicopy

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.databinding.ActivityMainBinding
import com.sumagoinfotech.digicopy.ui.activities.LoginActivity
import com.sumagoinfotech.digicopy.ui.fragments.dashboard.DashboardFragment
import com.sumagoinfotech.digicopy.utils.MySharedPref

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController

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
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            requestThePermissions()
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message)
        }
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
                .setPositiveButton("Yes") { _, _ ->
                    // Open the location settings to enable GPS
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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
        checkAndPromptGps()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu_home,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.action_logout)
        {
            val mySharedPref=MySharedPref(this@MainActivity)
            mySharedPref.clearAll()
            val intent=Intent(this@MainActivity,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}