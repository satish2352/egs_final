package com.sipl.egs.ui.activities.start

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.MainActivity
import com.sipl.egs.R
import com.sipl.egs.adapters.DocumentPagesAdapter
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.UserDao
import com.sipl.egs.database.entity.DocumentTypeDropDown
import com.sipl.egs.databinding.ActivityLoginBinding
import com.sipl.egs.model.apis.labourlist.LabourListModel
import com.sipl.egs.model.apis.login.LoginModel
import com.sipl.egs.ui.officer.OfficerMainActivity
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.DeviceUtils
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.MyValidator
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var mySharedPref:MySharedPref
    private val WRITE_PERMISSION_REQUEST_CODE = 1001
    private var isInternetAvailable = false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            noInternetDialog=NoInternetDialog(this)
            database= AppDatabase.getDatabase(this)
            userDao=database.userDao()
            mySharedPref=MySharedPref(this)
            val deviceId= DeviceUtils.getDeviceId(this@LoginActivity)
            mySharedPref.setDeviceId(deviceId)
            customProgressDialog= CustomProgressDialog(this)
            noInternetDialog=NoInternetDialog(this)
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
            binding.btnLogin.setOnClickListener {
                if(isInternetAvailable){
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
                                            Log.d("mytag",""+response.body()?.message)
                                            runOnUiThread {
                                                customProgressDialog.dismiss()
                                                val toast= Toast.makeText(this@LoginActivity,
                                                   response.body()?.message.toString(),
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
                            } catch (e: Exception) {
                                Log.d("mytag","Exception Inserted : ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }else{

                    }
                }else{
                    noInternetDialog.showDialog()
                    Toast.makeText(this,resources.getString(R.string.internet_is_not_available_please_check),Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","LoginActivity: ${e.message}",e)


        }
        binding.tvResetPassword.setOnClickListener {

            try {
                showResetPasswordDialog()
            } catch (e: Exception) {
                Log.d("mytag","LoginActivity: ${e.message}",e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestWritePermission(this@LoginActivity)
    }
    fun requestWritePermission(context: Context?): Boolean {
        try {
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
                    // Permission will be handled in onRequestPermissionsResult() callback
                    false
                } else {
                    // No need to request permission for devices running Android 10+
                    false
                }
            }
        }catch (e:Exception){
            Log.d("mytag","LoginActivity: ${e.message}",e)
            return false
        }
    }
    private fun requestThePermissions()
    {
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
            binding.txLayoutEmail.error= getString(R.string.please_enter_valid_email)
        }
        if(MyValidator.isValidPasswordOld(binding.etPassword.text.toString())){
            binding.txLayoutPassword.error=null
            list.add(true)
        }else{
            list.add(false)
            binding.txLayoutPassword.error=
                getString(R.string.please_enter_at_least_8_digit_password)
        }
        return !list.contains(false)
    }

    fun showResetPasswordDialog(){

        try {
            val dialog = Dialog(this@LoginActivity)
            dialog.setContentView(R.layout.layout_dialog_reset_password)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            val etEmail = dialog.findViewById<TextInputEditText>(R.id.etEmail)
            val btnResetPassword = dialog.findViewById<Button>(R.id.btnResetPassword)
            val txLayoutEmail = dialog.findViewById<TextInputLayout>(R.id.txLayoutEmail)
            dialog.show()
            btnResetPassword.setOnClickListener {

                if(isInternetAvailable){

                    if(MyValidator.isValidEmail(etEmail.text.toString().trim()))
                    {
                        txLayoutEmail.error=null

                        resetPassword(etEmail.text.toString().trim(),dialog)

                    }else{
                        txLayoutEmail.error=resources.getString(R.string.enter_valid_email)
                    }
                }else{

                    Toast.makeText(this@LoginActivity,resources.getString(R.string.internet_is_not_available_please_check),Toast.LENGTH_LONG).show()
                }
            }


        } catch (e: Exception) {
            Log.d("mytag","ScanDocumentsActivity:",e)
            e.printStackTrace()
        }
    }

    private fun resetPassword(email: String,dialog: Dialog) {
        try {
            val apiService=ApiClient.create(this@LoginActivity)
            customProgressDialog.show()
            val call=apiService.resetPassword(email)
            call.enqueue(object :Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    customProgressDialog.dismiss()
                    if(response.isSuccessful)
                    {
                        if(response.body()?.status.equals("true"))
                        {
                            Toast.makeText(this@LoginActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }else{
                            Toast.makeText(this@LoginActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this@LoginActivity,resources.getString(R.string.response_unsuccessfull),Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<LabourListModel>, t: Throwable) {
                    customProgressDialog.dismiss()
                    Log.d("mytag","onFailure",t)
                    t.printStackTrace()
                    Toast.makeText(this@LoginActivity,resources.getString(R.string.error_occured_during_api_call),Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("mytag","onFailure",e)
            e.printStackTrace()
        }

    }
}