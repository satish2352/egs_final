package com.sipl.egs2.ui.gramsevak

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egs2.R
import com.sipl.egs2.databinding.ActivityViewAttendanceBinding
import com.sipl.egs2.interfaces.AttendanceEditListener
import com.sipl.egs2.model.apis.attendance.AttendanceData
import com.sipl.egs2.model.apis.attendance.AttendanceModel
import com.sipl.egs2.model.apis.masters.MastersModel
import com.sipl.egs2.model.apis.projectlistmarker.ProjectData
import com.sipl.egs2.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sipl.egs2.adapters.ViewAttendanceAdapter
import com.sipl.egs2.pagination.MyPaginationAdapter
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.MySharedPref
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewAttendanceActivity : AppCompatActivity(),AttendanceEditListener,
    MyPaginationAdapter.OnPageNumberClickListener {

    private lateinit var binding:ActivityViewAttendanceBinding
    private lateinit var attendanceList:ArrayList<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var listProject: List<ProjectData>
    private var selectedProjectId=""
    private var selectedProjectIdForAttendance=""
    private lateinit var pref:MySharedPref

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref= MySharedPref(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.view_attendance)
        progressDialog= CustomProgressDialog(this)
        apiService = ApiClient.create(this)

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

        try {
            paginationAdapter= MyPaginationAdapter(0,"0",this)
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            currentPage="1"

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
                getAttendanceList(selectedProjectId,currentPage)
            }
            binding.actSelectProject.setOnFocusChangeListener { v, hasFocus ->
                binding.actSelectProject.showDropDown()
            }
            binding.btnClose.setOnClickListener {
                binding.actSelectProject.setText("")
                getAttendanceList(selectedProjectId = "",currentPage)
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
            getAttendanceList(selectedProjectId,currentPage);
        } catch (e: Exception) {
            Log.d("mytag","ViewAttendanceActivity:",e)
            e.printStackTrace()
        }


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

            progressDialog.show()
            val apiService = ApiClient.create(this@ViewAttendanceActivity)
            val call = apiService.getProjectList(pref.getLatitude()!!,pref.getLongitude()!!)
            call.enqueue(object : Callback<ProjectLabourListForMarker> {
                override fun onResponse(
                    call: Call<ProjectLabourListForMarker>,
                    response: Response<ProjectLabourListForMarker>
                ) {
                    progressDialog.dismiss()
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
                                this@ViewAttendanceActivity, resources.getString(R.string.no_data_found),
                                Toast.LENGTH_SHORT
                            )
                            toast.show()

                        }
                    } else {
                        Toast.makeText(
                            this@ViewAttendanceActivity,
                            resources.getString(R.string.response_unsuccessfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                    progressDialog.dismiss()
                    Log.d("mytag", "onFailure getProjectFromServer " + t.message)
                    Toast.makeText(
                        this@ViewAttendanceActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d("mytag", "Exception getProjectFromServer " + e.message)
            e.printStackTrace()
        }

    }

    private fun getAttendanceList(selectedProjectId: String,pageNumber: String) {

        try {
            if(!progressDialog.isDialogVisible()){
                progressDialog.show()
            }
            val call=apiService.getListOfMarkedAttendance(projectId = selectedProjectId, startPageNumber = pageNumber);
            call.enqueue(object :Callback<AttendanceModel>{
                override fun onResponse(
                    call: Call<AttendanceModel>,
                    response: Response<AttendanceModel>
                ) {
                    progressDialog.dismiss()
                    if(response.isSuccessful)
                    {
                        attendanceList.clear()
                        if(response.body()?.status.equals("true")){
                            attendanceList= (response.body()?.data as ArrayList<AttendanceData>?)!!
                            adapter= ViewAttendanceAdapter(attendanceList,this@ViewAttendanceActivity)
                            binding.recyclerView.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@ViewAttendanceActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        }else{
                            paginationAdapter= MyPaginationAdapter(0,"0",this@ViewAttendanceActivity)
                            paginationLayoutManager=LinearLayoutManager(this@ViewAttendanceActivity, RecyclerView.HORIZONTAL,false)
                            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
                            binding.recyclerViewPageNumbers.adapter=paginationAdapter
                            adapter.notifyDataSetChanged()
                            currentPage="1"
                        }
                    }else{
                        paginationAdapter= MyPaginationAdapter(0,"0",this@ViewAttendanceActivity)
                        paginationLayoutManager=LinearLayoutManager(this@ViewAttendanceActivity, RecyclerView.HORIZONTAL,false)
                        binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
                        binding.recyclerViewPageNumbers.adapter=paginationAdapter
                        adapter.notifyDataSetChanged()
                        currentPage="1"
                        Toast.makeText(this@ViewAttendanceActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AttendanceModel>, t: Throwable) {
                    Toast.makeText(this@ViewAttendanceActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","getAttendanceList : Exception "+e.message)
            e.printStackTrace()
            progressDialog.dismiss()
        }
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

            selectedProjectIdForAttendance=data.project_id
            val dialog = Dialog(this@ViewAttendanceActivity)
            dialog.setContentView(R.layout.layout_dialog_mark_attendence_with_project_list)
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
            val actSelectProjectForAttendance = dialog.findViewById<AutoCompleteTextView>(R.id.actSelectProjectForAttendance)

            actSelectProjectForAttendance.setText(data.project_name)
            actSelectProjectForAttendance.setOnFocusChangeListener { abaad, asd ->
                actSelectProjectForAttendance.showDropDown()
            }
            actSelectProjectForAttendance.setOnClickListener {
                actSelectProjectForAttendance.showDropDown()
            }
            val projectNames = mutableListOf<String>()
            for (project in listProject) {
                projectNames.add(project.project_name)
            }

            val projectNamesAdapter = ArrayAdapter(
                this@ViewAttendanceActivity,
                android.R.layout.simple_list_item_1,
                projectNames
            )
            Log.d("mytag", "" + listProject.size)
            Log.d("mytag", "" + projectNames.size)
            actSelectProjectForAttendance.setAdapter(projectNamesAdapter)
            actSelectProjectForAttendance.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                Log.d("mytag", "" + position)
                /*binding.tvProjectAddress.setText(
                    listProject.get(position).district_name + " -> " + listProject.get(
                        position
                    ).taluka_name + " -> " + listProject.get(position).village_name
                )
                binding.tvProjectDuration.setText(
                    listProject.get(position).start_date + " To " + listProject.get(
                        position
                    ).end_date
                )*/
                selectedProjectIdForAttendance = listProject.get(position).id.toString()
            }
            tvFullName.text = data.full_name
            Glide.with(this@ViewAttendanceActivity).load(data.profile_image).override(100,100).into(ivPhoto)
            if(data.attendance_day.equals("half_day")){
                radioButtonHalfDay.isChecked = true
            }else {
                radioButtonFullDay.isChecked=true
            }
            val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

                btnSubmit.setOnClickListener {
                  if(isInternetAvailable){
                      if(actSelectProjectForAttendance.enoughToFilter())
                      {
                          actSelectProjectForAttendance.error=null
                          if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay || radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {
                              var dayType = ""
                              if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {
                                  dayType = "half_day"
                              } else if(radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay) {
                                  dayType = "full_day"
                              }
                              Log.d("mytag","showAttendanceDialog : "+dayType)
                              val call = apiService.updateAttendance(projectId = selectedProjectIdForAttendance, mgnregaId = data.mgnrega_card_id, attendanceDay = dayType)
                              call.enqueue(object : Callback<MastersModel> {
                                  override fun onResponse(
                                      call: Call<MastersModel>,
                                      response: Response<MastersModel>
                                  ) {
                                      Log.d("mytag","showAttendanceDialog : onResponse ")

                                      if (response.isSuccessful) {

                                          if (response.body()?.status.equals("true")) {
                                              getAttendanceList("", pageNumber = currentPage)
                                              data.attendance_day=dayType
                                              val toast = Toast.makeText(
                                                  this@ViewAttendanceActivity,
                                                  response.body()?.message,
                                                  Toast.LENGTH_SHORT
                                              )
                                              toast.show()
                                              attendanceList.set(position,data)
                                              adapter.notifyDataSetChanged()

                                          }else{
                                              val toast = Toast.makeText(
                                                  this@ViewAttendanceActivity,
                                                  response.body()?.message,
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
                                      dialog.dismiss()
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
                      }else{
                          actSelectProjectForAttendance.error=resources.getString(R.string.select_project)
                      }
                  }else{

                      noInternetDialog.showDialog()
                  }
                }


        } catch (e: Exception) {
            Log.d("mytag","showAttendanceDialog : Exception "+e.message)
            e.printStackTrace()
        }
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        currentPage="$pageNumber"
        getAttendanceList(selectedProjectId,"$pageNumber")
        paginationAdapter.setSelectedPage(pageNumber)

    }
}