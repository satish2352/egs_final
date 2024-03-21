package com.sumagoinfotech.digicopy.ui.activities.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivityLoginBinding
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.ui.activities.officer.OfficerMainActivity
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        customProgressDialog= CustomProgressDialog(this)
        binding.btnLogin.setOnClickListener {
            if(validateFields()) {
                customProgressDialog.show()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val user=userDao.getUser(binding.etEmail.text.toString(),binding.etPassword.text.toString())
                        val list=userDao.getAllUsers()
                        Log.d("mytag","found "+list.size)
                        val apiService=ApiClient.create(this@LoginActivity)
                        val call=apiService.loginUser(binding.etEmail.text.toString(),binding.etPassword.text.toString())
                        call.enqueue(object :Callback<LoginModel>{
                            override fun onResponse(
                                call: Call<LoginModel>,
                                response: Response<LoginModel>
                            ) {
                                if(response.isSuccessful)
                                {
                                    val loginModel=response.body()
                                    val mySharedPref=MySharedPref(this@LoginActivity)
                                    mySharedPref.setIsLoggedIn(true)
                                    mySharedPref.setId(loginModel?.data?.id!!)
                                    mySharedPref.setEmail(loginModel?.data?.email!!)
                                    mySharedPref.setRememberToken(loginModel?.data?.remember_token!!)
                                    Log.d("mytag",""+loginModel?.data?.remember_token!!)
                                    Log.d("mytag","User_ID"+loginModel?.data?.id!!)
                                    runOnUiThread {
                                        customProgressDialog.dismiss()
                                        val toast= Toast.makeText(this@LoginActivity,
                                            getString(R.string.login_successful),
                                            Toast.LENGTH_SHORT)
                                        toast.show()
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
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
        binding.ivHome.setOnClickListener {
            val intent = Intent(this@LoginActivity, OfficerMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()

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