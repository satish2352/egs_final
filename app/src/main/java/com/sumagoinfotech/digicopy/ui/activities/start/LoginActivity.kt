package com.sumagoinfotech.digicopy.ui.activities.start

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivityLoginBinding
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.ui.officer.OfficerMainActivity
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.DeviceUtils
import com.sumagoinfotech.digicopy.utils.MySharedPref
import com.sumagoinfotech.digicopy.utils.MyValidator
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var mySharedPref:MySharedPref
    private val WRITE_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        mySharedPref=MySharedPref(this)
        val deviceId= DeviceUtils.getDeviceId(this@LoginActivity)
        mySharedPref.setDeviceId(deviceId)
        customProgressDialog= CustomProgressDialog(this)

        binding.btnLogin.setOnClickListener {
            if(validateFields()) {
                customProgressDialog.show()
                val mySharedPref=MySharedPref(this@LoginActivity)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val user=userDao.getUser(binding.etEmail.text.toString(),binding.etPassword.text.toString())
                        val list=userDao.getAllUsers()
                        Log.d("mytag","found "+list.size)
                        val apiService=ApiClient.create(this@LoginActivity)
                        val call=apiService.loginUser(binding.etEmail.text.toString(),binding.etPassword.text.toString(),mySharedPref.getDeviceId()!!)
                        call.enqueue(object :Callback<LoginModel>{
                            override fun onResponse(
                                call: Call<LoginModel>,
                                response: Response<LoginModel>
                            ) {
                                if(response.isSuccessful)
                                {
                                    val message=response.body()?.message
                                    if(response.body()?.status.equals("True")){
                                        val loginModel=response.body()
                                        mySharedPref.setIsLoggedIn(true)
                                        mySharedPref.setId(loginModel?.data?.id!!)
                                        mySharedPref.setEmail(loginModel?.data?.email!!)
                                        mySharedPref.setRememberToken(loginModel?.data?.remember_token!!)
                                        mySharedPref.setRoleId(loginModel?.data?.role_id!!)
                                        mySharedPref.setFName(loginModel?.data?.f_name!!)
                                        mySharedPref.setMName(loginModel?.data?.m_name!!)
                                        mySharedPref.setLName(loginModel?.data?.l_name!!)
                                        mySharedPref.setUserDistrictId(loginModel?.data?.user_district.toString())
                                        mySharedPref.setUserTalukaId(loginModel?.data?.user_taluka.toString())
                                        mySharedPref.setUserVillageId(loginModel?.data?.user_village.toString())
                                        if(loginModel?.data?.role_id==2)
                                        {
                                            mySharedPref.setOfficerDistrictID(loginModel?.data?.user_district.toString())
                                            mySharedPref.setUserTalukaId(loginModel?.data?.user_taluka.toString())
                                            mySharedPref.setUserVillageId(loginModel?.data?.user_village.toString())
                                        }
                                        runOnUiThread {
                                            customProgressDialog.dismiss()
                                            if(loginModel?.data?.role_id==2){
                                                val toast= Toast.makeText(this@LoginActivity,
                                                    getString(R.string.login_successful),
                                                    Toast.LENGTH_SHORT)
                                                toast.show()
                                                val intent = Intent(this@LoginActivity, OfficerMainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                                finish()
                                            }else if(loginModel?.data?.role_id==3) {
                                                val toast= Toast.makeText(this@LoginActivity,
                                                    getString(R.string.login_successful),
                                                    Toast.LENGTH_SHORT)
                                                toast.show()
                                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    }else{
                                        runOnUiThread {
                                            customProgressDialog.dismiss()
                                            val toast= Toast.makeText(this@LoginActivity,
                                                message,
                                                Toast.LENGTH_SHORT)
                                            toast.show()
                                        }
                                    }

                                }else{
                                    runOnUiThread {
                                        customProgressDialog.dismiss()
                                        val toast= Toast.makeText(this@LoginActivity,
                                            getString(R.string.error_while_login),
                                            Toast.LENGTH_SHORT)
                                        toast.show()
                                    }
                                }
                            }
                            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                                runOnUiThread {
                                    customProgressDialog.dismiss()
                                    val toast= Toast.makeText(this@LoginActivity,
                                        getString(R.string.error_while_login),
                                        Toast.LENGTH_SHORT)
                                    toast.show()
                                }
                            }
                        })
                        /*if(user!==null){
                            Log.d("mytag","found "+user.email)
                            runOnUiThread {
                                val toast= Toast.makeText(this@LoginActivity,"Login successful",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                        }else{
                            runOnUiThread {
                                val toast= Toast.makeText(this@LoginActivity,"Please enter correct details",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                            }
                        }*/
                    } catch (e: Exception) {
                        Log.d("mytag","Exception Inserted : ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{

            }
        }
    }

    override fun onResume() {
        super.onResume()


    }
    fun requestWritePermission(context: Context?): Boolean {
        // Check if the permission is already granted
        return if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
            true
        } else {
            // Check Android version for handling permission request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Request permission at runtime for devices running Android 6.0 to Android 9 (excluding Android 10+)
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!,
                    arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_PERMISSION_REQUEST_CODE
                )
                // Permission will be handled in onRequestPermissionsResult() callback
                false
            } else {
                // No need to request permission for devices running Android 10+
                false
            }
        }
    }
    private fun requestThePermissions() {

        PermissionX.init(this@LoginActivity)
            .permissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {

                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateFields(): Boolean {
        var list=ArrayList<Boolean>()
        if(MyValidator.isValidEmail(binding.etEmail.text.toString())){

            binding.txLayoutEmail.error=null
            list.add(true)
        }else{
            list.add(false)
            binding.txLayoutEmail.error="Please Enter Valid Email"
        }
        if(MyValidator.isValidPassword(binding.etPassword.text.toString())){
            binding.txLayoutPassword.error=null
            list.add(true)
        }else{
            list.add(false)
            binding.txLayoutPassword.error="Please enter at least 8 digit password "
        }

        return !list.contains(false)
    }
}