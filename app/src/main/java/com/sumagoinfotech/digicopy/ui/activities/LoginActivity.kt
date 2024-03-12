package com.sumagoinfotech.digicopy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivityLoginBinding
import com.sumagoinfotech.digicopy.ui.adapters.AttendanceAdapter
import com.sumagoinfotech.digicopy.utils.MyValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        binding.btnLogin.setOnClickListener {
            if(validateFields()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val user=userDao.getUser(binding.etEmail.text.toString(),binding.etPassword.text.toString())
                        val list=userDao.getAllUsers()
                        Log.d("mytag","found "+list.size)

                        if(user!==null){
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
                        }
                    } catch (e: Exception) {
                        Log.d("mytag","Exception Inserted : ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{

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