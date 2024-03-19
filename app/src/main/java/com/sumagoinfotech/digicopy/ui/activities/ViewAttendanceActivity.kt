package com.sumagoinfotech.digicopy.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityViewAttendanceBinding
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.ui.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityViewAttendanceBinding
    private lateinit var attendanceList:List<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var dialog:CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.view_attendance)
        dialog=CustomProgressDialog(this)
        apiService = ApiClient.create(this)
        binding.recyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        attendanceList=ArrayList<AttendanceData>()
        adapter=ViewAttendanceAdapter(attendanceList)
        binding.recyclerView.adapter=adapter
        getAttendanceList();


    }

    private fun getAttendanceList() {

        dialog.show()
        val call=apiService.getListOfMarkedAttendance();
        call.enqueue(object :Callback<AttendanceModel>{
            override fun onResponse(
                call: Call<AttendanceModel>,
                response: Response<AttendanceModel>
            ) {
                dialog.dismiss()
                if(response.isSuccessful)
                {
                    if(response.body()?.status.equals("true")){
                        attendanceList=response.body()?.data!!
                        adapter= ViewAttendanceAdapter(attendanceList)
                        binding.recyclerView.adapter=adapter
                        adapter.notifyDataSetChanged()
                    }
                }else{
                    Toast.makeText(this@ViewAttendanceActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AttendanceModel>, t: Throwable) {
                Toast.makeText(this@ViewAttendanceActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }
}