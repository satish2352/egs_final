package com.sumagoinfotech.digicopy.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityViewAttendanceBinding
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.ui.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityViewAttendanceBinding
    private lateinit var attendanceList:ArrayList<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var dialog:CustomProgressDialog
    private lateinit var listProject: List<ProjectData>
    private var selectedProjectId=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.view_attendance)
        dialog= CustomProgressDialog(this)
        apiService = ApiClient.create(this)
        binding.recyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        attendanceList=ArrayList()
        listProject=ArrayList()
        adapter=ViewAttendanceAdapter(attendanceList)
        binding.recyclerView.adapter=adapter
        binding.actSelectProject.setOnClickListener {
            binding.actSelectProject.showDropDown()
        }
        binding.actSelectProject.setOnItemClickListener { parent, view, position, id ->

            selectedProjectId=listProject.get(position).id.toString()
            getAttendanceList(selectedProjectId)
        }
        binding.actSelectProject.setOnFocusChangeListener { v, hasFocus ->
            binding.actSelectProject.showDropDown()
        }
        binding.btnClose.setOnClickListener {
            binding.actSelectProject.setText("")
            getAttendanceList("")
        }
        getProjectList();
        getAttendanceList(selectedProjectId);
    }
    private fun getProjectList() {
            val apiService = ApiClient.create(this@ViewAttendanceActivity)
            val call = apiService.getProjectList()
            call.enqueue(object : Callback<ProjectLabourListForMarker> {
                override fun onResponse(
                    call: Call<ProjectLabourListForMarker>,
                    response: Response<ProjectLabourListForMarker>
                ) {
                    Log.d("mytag", "getProjectList=>"+Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        if (!response.body()?.project_data.isNullOrEmpty()) {
                            listProject = response.body()?.project_data!!
                            val projectNames = mutableListOf<String>()
                            for (project in listProject) {
                                projectNames.add(project.project_name)
                            }

                            val projectNamesAdapter = ArrayAdapter(
                                this@ViewAttendanceActivity,
                                android.R.layout.simple_list_item_1,
                                projectNames
                            )
                            binding.actSelectProject.setAdapter(projectNamesAdapter)
                        } else {
                            val toast = Toast.makeText(
                                this@ViewAttendanceActivity, "No data found",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()

                        }
                    } else {
                        Toast.makeText(
                            this@ViewAttendanceActivity,
                            "response unsuccessful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                    Log.d("mytag", "onFailure getProjectFromServer " + t.message)
                    Toast.makeText(
                        this@ViewAttendanceActivity,
                        "Error occured during api unsuccessful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }

    private fun getAttendanceList(selectedProjectId: String) {

        dialog.show()
        val call=apiService.getListOfMarkedAttendance(selectedProjectId);
        call.enqueue(object :Callback<AttendanceModel>{
            override fun onResponse(
                call: Call<AttendanceModel>,
                response: Response<AttendanceModel>
            ) {
                dialog.dismiss()
                if(response.isSuccessful)
                {
                    attendanceList.clear()
                    if(response.body()?.status.equals("true")){
                        attendanceList= (response.body()?.data as ArrayList<AttendanceData>?)!!
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}