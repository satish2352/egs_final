package com.sipl.egs2.ui.gramsevak

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egs2.R
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.dao.LabourDao
import com.sipl.egs2.database.entity.Labour
import com.sipl.egs2.databinding.ActivityAllocateWorkBinding
import com.sipl.egs2.interfaces.MarkAttendanceListener
import com.sipl.egs2.model.apis.getlabour.LabourByMgnregaId
import com.sipl.egs2.model.apis.getlabour.LabourInfo
import com.sipl.egs2.model.apis.masters.MastersModel
import com.sipl.egs2.model.apis.projectlistmarker.ProjectData
import com.sipl.egs2.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sipl.egs2.adapters.AttendanceAdapter
import com.sipl.egs2.pagination.MyPaginationAdapter
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.MySharedPref
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllocateWorkActivity : AppCompatActivity(), MarkAttendanceListener,
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding: ActivityAllocateWorkBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    private lateinit var adapter: AttendanceAdapter
    private lateinit var labourList: ArrayList<Labour>
    private lateinit var listProject: List<ProjectData>
    private lateinit var labourDataList: ArrayList<LabourInfo>
    private var selectedProjectId = ""
    private lateinit var apiService: ApiService
    lateinit var pref:MySharedPref
    private lateinit var progressDialog: CustomProgressDialog

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllocateWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)
        labourDao = database.labourDao()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.allocate_work)
        apiService = ApiClient.create(this)
        pref=MySharedPref(this)
        progressDialog= CustomProgressDialog(this)

        paginationAdapter= MyPaginationAdapter(0,"0",this)
        paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
        currentPage="1"
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
        getProjectFromServer()
        labourList = ArrayList<Labour>()
        labourDataList = ArrayList<LabourInfo>()
        adapter = AttendanceAdapter(labourDataList, this)
        binding.recyclerViewAttendance.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.projectArea.setOnFocusChangeListener { abaad, asd ->
            binding.projectArea.showDropDown()
        }
        binding.projectArea.setOnClickListener {
            binding.projectArea.showDropDown()
        }
        binding.ivSearchByLabourId.setOnClickListener {

            if(isInternetAvailable){
                searchLabourByMgnregaId(currentPage)
            }else{
                noInternetDialog.showDialog()
            }

        }


        binding.projectArea.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.tvProjectAddress.setText(
                    listProject.get(position).district_name + " -> " + listProject.get(
                        position
                    ).taluka_name + " -> " + listProject.get(position).village_name
                )
                binding.tvProjectDuration.setText(
                    listProject.get(position).start_date + " To " + listProject.get(
                        position
                    ).end_date
                )
                selectedProjectId = listProject.get(position).id.toString()
            }

        binding.etLabourId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (s?.length!! < 1) {
                    (labourList as ArrayList<Labour>).clear()
                }
            }
        })
    }

    private fun searchLabourByMgnregaId(currentPage:String) {
        if (validateFields()) {
            progressDialog.show()
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val call = apiService.getLabourDataByIdForAttendance(mgnrega_card_id = binding.etLabourId.text.toString(), startPageNumber = currentPage)
                    call.enqueue(object : Callback<LabourByMgnregaId> {
                        override fun onResponse(
                            call: Call<LabourByMgnregaId>,
                            response: Response<LabourByMgnregaId>
                        ) {
                            if (response.isSuccessful) {
                                (labourDataList as ArrayList<LabourInfo>).clear()
                                if (response.body()?.status.equals("true")) {
                                    labourDataList = (response.body()?.data as ArrayList<LabourInfo>?)!!
                                    runOnUiThread {
                                        if (labourDataList.size > 0) {
                                            adapter = AttendanceAdapter(
                                                labourDataList,
                                                this@AllocateWorkActivity
                                            )
                                            binding.recyclerViewAttendance.adapter = adapter;
                                            adapter.notifyDataSetChanged()
                                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@AllocateWorkActivity)
                                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                                            pageAdapter.notifyDataSetChanged()
                                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                                        } else {
                                            adapter = AttendanceAdapter(
                                                labourDataList,
                                                this@AllocateWorkActivity
                                            )
                                            binding.recyclerViewAttendance.adapter = adapter;
                                            adapter.notifyDataSetChanged()
                                            val toast = Toast.makeText(
                                                this@AllocateWorkActivity,
                                                getString(R.string.labour_not_found),
                                                Toast.LENGTH_LONG
                                            )
                                            toast.show()
                                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@AllocateWorkActivity)
                                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                                            pageAdapter.notifyDataSetChanged()
                                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        val toast = Toast.makeText(
                                            this@AllocateWorkActivity, resources.getString(R.string.please_try_again),
                                            Toast.LENGTH_LONG
                                        )
                                        toast.show()
                                    }
                                }
                            }else{
                                runOnUiThread {
                                    val toast = Toast.makeText(
                                        this@AllocateWorkActivity, resources.getString(R.string.response_unsuccessfull),
                                        Toast.LENGTH_LONG
                                    )
                                    toast.show()
                                }
                            }
                            runOnUiThread { progressDialog.dismiss() }
                        }

                        override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                            Log.d("mytag", "onFailure getLabourDataById : ${t.message}")
                            t.printStackTrace()
                            runOnUiThread { progressDialog.dismiss() }
                            runOnUiThread {
                                val toast = Toast.makeText(
                                    this@AllocateWorkActivity, resources.getString(R.string.error_occured_during_api_call),
                                    Toast.LENGTH_LONG
                                )
                                toast.show()
                            }
                        }
                    })


                } catch (e: Exception) {
                    Log.d("mytag", "Exception Inserted : ${e.message}")
                    e.printStackTrace()
                    runOnUiThread { progressDialog.dismiss() }
                }
            }
        } else {
            val toast = Toast.makeText(
                this@AllocateWorkActivity, "Please enter details",
                Toast.LENGTH_LONG
            )
            toast.show()
        }
    }

    private fun validateFields(): Boolean {
        var result = mutableListOf<Boolean>()
        if (binding.projectArea.enoughToFilter()) {
            binding.projectArea.error = null
            result.add(true)

        } else {
            result.add(false)
            binding.projectArea.error = resources.getString(R.string.please_select_project_area)
        }
        if (binding.etLabourId.text?.length!! > 0) {

            result.add(true)
            binding.etLabourId.error = null

        } else {
            result.add(false)
            binding.etLabourId.error = resources.getString(R.string.please_enter_mgnrega_id)
        }

        return !result.contains(false);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        if (item.itemId == R.id.action_view_attendance) {

            if(isInternetAvailable){
                val intent= Intent(this@AllocateWorkActivity, com.sipl.egs2.ui.gramsevak.ViewAttendanceActivity::class.java)
                startActivity(intent)
            }else{
                noInternetDialog.showDialog()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view_attendance,menu)
        return true
    }

    private fun showAttendanceDialog(fullName: String, labourImage: String, mgnregaId: String) {
        try {
            val dialog = Dialog(this@AllocateWorkActivity)
            dialog.setContentView(R.layout.layout_dialog_mark_attendence)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val tvFullName = dialog.findViewById<TextView>(R.id.tvFullName)
            val ivPhoto = dialog.findViewById<ImageView>(R.id.ivPhoto)
            val radioGroupAttendance = dialog.findViewById<RadioGroup>(R.id.radioGroupAttendance)
            tvFullName.text = fullName
            Glide.with(this@AllocateWorkActivity).load(labourImage).into(ivPhoto)
            val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)
            btnSubmit.setOnClickListener {
                progressDialog.show()
                if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay || radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {

                    var dayType = ""
                    if (radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonHalfDay) {
                        dayType = "half_day"
                    } else if(radioGroupAttendance.checkedRadioButtonId == R.id.radioButtonFullDay) {
                        dayType = "full_day"
                    }
                    val call =
                        apiService.markAttendance(selectedProjectId, mgnregaId = mgnregaId, dayType)
                    call.enqueue(object : Callback<MastersModel>,
                        MyPaginationAdapter.OnPageNumberClickListener {
                        override fun onResponse(
                            call: Call<MastersModel>,
                            response: Response<MastersModel>
                        ) {
                            progressDialog.dismiss()
                            dialog.dismiss()
                            if (response.isSuccessful) {

                                if (response.body()?.status.equals("true")) {
                                    val toast = Toast.makeText(
                                        this@AllocateWorkActivity,
                                        getString(R.string.attendance_marked_successfully),
                                        Toast.LENGTH_LONG
                                    )
                                    toast.show()
                                    labourDataList.clear()
                                    binding.recyclerViewAttendance.adapter=adapter
                                    adapter.notifyDataSetChanged()
                                    binding.etLabourId.setText("")
                                    binding.tvProjectAddress.setText("")
                                    binding.projectArea.clearListSelection()
                                    binding.projectArea.setText("")
                                    binding.tvProjectDuration.setText("")
                                    paginationAdapter= MyPaginationAdapter(0,"0",this)
                                    binding.recyclerViewPageNumbers.adapter=paginationAdapter
                                    paginationAdapter.notifyDataSetChanged()


                                }else{
                                    val toast = Toast.makeText(
                                        this@AllocateWorkActivity, response.body()?.message,
                                        Toast.LENGTH_LONG
                                    )
                                    toast.show()
                                    labourDataList.clear()
                                    binding.recyclerViewAttendance.adapter=adapter
                                    adapter.notifyDataSetChanged()
                                    binding.etLabourId.setText("")
                                    binding.tvProjectAddress.setText("")
                                    binding.projectArea.clearListSelection()
                                    binding.projectArea.setText("")
                                    binding.tvProjectDuration.setText("")
                                    paginationAdapter= MyPaginationAdapter(0,"0",this)
                                    binding.recyclerViewPageNumbers.adapter=paginationAdapter
                                    paginationAdapter.notifyDataSetChanged()
                                }
                            }else{
                                val toast = Toast.makeText(
                                    this@AllocateWorkActivity,
                                    getString(R.string.unable_to_mark_please_try_again),
                                    Toast.LENGTH_LONG
                                )
                                toast.show()
                            }
                        }

                        override fun onFailure(call: Call<MastersModel>, t: Throwable) {
                            progressDialog.dismiss()
                            Log.d("mytag","showAttendanceDialog : onFailure "+t.message);
                            dialog.dismiss()
                            val toast = Toast.makeText(
                                this@AllocateWorkActivity, resources.getString(R.string.error_occured_during_api_call),
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        }

                        override fun onPageNumberClicked(pageNumber: Int) {

                        }
                    })

                } else {
                    val toast = Toast.makeText(
                        this@AllocateWorkActivity, resources.getString(R.string.select_day),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                }

            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d("mytag","showAttendanceDialog : Exception "+e.message);
            e.printStackTrace()
        }
    }

    override fun markAttendance(labour: LabourInfo) {
        showAttendanceDialog(labour.full_name, labour.profile_image, labour.mgnrega_card_id)
        try {
            (labourList as ArrayList<Labour>).clear()
            adapter.notifyDataSetChanged()
            binding.etLabourId.setText("")
            binding.tvProjectAddress.setText("")
            binding.projectArea.clearListSelection()
            binding.projectArea.setText("")
            binding.tvProjectDuration.setText("")
            paginationAdapter= MyPaginationAdapter(0,"0",this)
            paginationLayoutManager=LinearLayoutManager(this@AllocateWorkActivity, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            paginationAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d("mytag","AllocateWorkActivity:",e)
            e.printStackTrace()
        }
    }

    private fun getProjectFromServer()
    {
        try {
            progressDialog.show()
            val apiService = ApiClient.create(this@AllocateWorkActivity)
            val call = apiService.getProjectListForAttendance(latitude = pref.getLatitude()!!, longitude = pref.getLongitude()!!)
            call.enqueue(object : Callback<ProjectLabourListForMarker> {
                override fun onResponse(
                    call: Call<ProjectLabourListForMarker>,
                    response: Response<ProjectLabourListForMarker>
                ) {
                    progressDialog.dismiss()
                    Log.d("mytag", Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        if (!response.body()?.project_data.isNullOrEmpty()) {
                            listProject = response.body()?.project_data!!
                            val projectNames = mutableListOf<String>()
                            for (project in listProject) {
                                projectNames.add(project.project_name)
                            }
                            val projectNamesAdapter = ArrayAdapter(
                                this@AllocateWorkActivity,
                                android.R.layout.simple_list_item_1,
                                projectNames
                            )
                            binding.projectArea.setAdapter(projectNamesAdapter)
                        } else {
                            val toast = Toast.makeText(
                                this@AllocateWorkActivity,
                                getString(R.string.project_list_not_found),
                                Toast.LENGTH_LONG
                            )
                            toast.show()

                        }
                    } else {
                        Toast.makeText(
                            this@AllocateWorkActivity,
                            resources.getString(R.string.response_unsuccessfull),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                    progressDialog.dismiss()
                    Log.d("mytag", "onFailure getProjectFromServer " + t.message)
                    Toast.makeText(
                        this@AllocateWorkActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        } catch (e: Exception) {
            progressDialog.dismiss()
        }
    }
    override fun onPageNumberClicked(pageNumber: Int) {
        try {
            currentPage="$pageNumber"
            searchLabourByMgnregaId("$pageNumber")
            paginationAdapter.setSelectedPage(pageNumber)
        } catch (e: Exception) {
            Log.d("mytag","AllowCateWorkActivity: ",e)
            e.printStackTrace()
        }

    }
}