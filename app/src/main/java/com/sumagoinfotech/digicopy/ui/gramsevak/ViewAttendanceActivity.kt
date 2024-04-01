package com.sumagoinfotech.digicopy.ui.gramsevak

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityViewAttendanceBinding
import com.sumagoinfotech.digicopy.interfaces.AttendanceEditListener
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.MySharedPref
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewAttendanceActivity : AppCompatActivity(),AttendanceEditListener {

    private lateinit var binding:ActivityViewAttendanceBinding
    private lateinit var attendanceList:ArrayList<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var dialog:CustomProgressDialog
    private lateinit var listProject: List<ProjectData>
    private var selectedProjectId=""
    private lateinit var pref:MySharedPref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref= MySharedPref(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.view_attendance)
        dialog= CustomProgressDialog(this)
        apiService = ApiClient.create(this)
        binding.recyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        attendanceList=ArrayList()
        listProject=ArrayList()
        adapter= ViewAttendanceAdapter(attendanceList,this@ViewAttendanceActivity)
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
        binding.layoutStartDate.setOnClickListener {
            showDatePicker(this,binding.etStartDate)
        }
        binding.layoutEndDate.setOnClickListener {
            showDatePicker(this,binding.etEndDate)
        }
        binding.btnClearAll.setOnClickListener {
            binding.etEndDate.setText("")
            binding.etStartDate.setText("")
            binding.actSelectProject.setText("")
            selectedProjectId=""
        }
        getProjectList();
        getAttendanceList(selectedProjectId);
    }
    private fun showDatePicker(context: Context, editText: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            context, { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = formatDate(dayOfMonth, monthOfYear, year)
                editText.setText(selectedDate)
            }, year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    private fun formatDate(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    private fun getProjectList() {
        try {

            val apiService = ApiClient.create(this@ViewAttendanceActivity)
            val call = apiService.getProjectList(pref.getLatitude()!!,pref.getLongitude()!!)
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
        } catch (e: Exception) {
        }

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
                        adapter= ViewAttendanceAdapter(attendanceList,this@ViewAttendanceActivity)
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

    override fun onAttendanceEdit(data: AttendanceData,position:Int) {

        showAttendanceDialog(data,position)
    }
    private fun showAttendanceDialog(data: AttendanceData,position: Int) {
        try {
            val dialog = Dialog(this@ViewAttendanceActivity)
            dialog.setContentView(R.layout.layout_dialog_mark_attendence)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val tvFullName = dialog.findViewById<TextView>(R.id.tvFullName)
            val ivPhoto = dialog.findViewById<ImageView>(R.id.ivPhoto)
            val radioGroupAttendance = dialog.findViewById<RadioGroup>(R.id.radioGroupAttendance)
            val radioButtonHalfDay = dialog.findViewById<RadioButton>(R.id.radioButtonHalfDay)
            val radioButtonFullDay = dialog.findViewById<RadioButton>(R.id.radioButtonFullDay)
            tvFullName.text = data.full_name
            Glide.with(this@ViewAttendanceActivity).load(data.profile_image).into(ivPhoto)
            if(data.attendance_day.equals("Half Day")){
                radioButtonHalfDay.isChecked = true
            }else {
                radioButtonFullDay.isChecked=true
            }
            val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)
            btnSubmit.setOnClickListener {
                if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay || radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {
                    var dayType = ""
                    if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {
                        dayType = "half_day"
                    } else if(radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay) {
                        dayType = "full_day"
                    }
                    Log.d("mytag","showAttendanceDialog : "+dayType)
                    val call = apiService.updateAttendance(projectId = data.project_id, mgnregaId = data.mgnrega_card_id, attendanceDay = dayType)
                    call.enqueue(object : Callback<MastersModel> {
                        override fun onResponse(
                            call: Call<MastersModel>,
                            response: Response<MastersModel>
                        ) {
                            Log.d("mytag","showAttendanceDialog : onResponse ")
                            dialog.dismiss()
                            if (response.isSuccessful) {

                                if (response.body()?.status.equals("true")) {
                                    data.attendance_day=dayType
                                    val toast = Toast.makeText(
                                        this@ViewAttendanceActivity,
                                        getString(R.string.attendance_updated_successfully),
                                        Toast.LENGTH_SHORT
                                    )
                                    toast.show()
                                   attendanceList.set(position,data)
                                    adapter.notifyDataSetChanged()

                                }else{
                                    val toast = Toast.makeText(
                                        this@ViewAttendanceActivity,
                                        getString(R.string.attendance_updating_failed),
                                        Toast.LENGTH_SHORT
                                    )
                                    toast.show()
                                }
                            }else{
                                val toast = Toast.makeText(
                                    this@ViewAttendanceActivity,
                                    getString(R.string.attendance_updating_failed),
                                    Toast.LENGTH_SHORT
                                )
                                toast.show()
                            }
                        }
                        override fun onFailure(call: Call<MastersModel>, t: Throwable) {
                            Log.d("mytag","showAttendanceDialog : onFailure "+t.message)
                            dialog.dismiss()
                            val toast = Toast.makeText(
                                this@ViewAttendanceActivity,
                                getString(R.string.error_occured_during_api_call),
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        }
                    })

                } else {
                    val toast = Toast.makeText(
                        this@ViewAttendanceActivity, getString(R.string.select_day),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","showAttendanceDialog : Exception "+e.message)
            e.printStackTrace()
        }
    }
}