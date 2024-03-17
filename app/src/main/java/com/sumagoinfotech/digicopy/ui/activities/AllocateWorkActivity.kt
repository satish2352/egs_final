package com.sumagoinfotech.digicopy.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.databinding.ActivityAllocateWorkBinding
import com.sumagoinfotech.digicopy.interfaces.MarkAttendanceListener
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourInfo
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.ui.adapters.AttendanceAdapter
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllocateWorkActivity : AppCompatActivity(),MarkAttendanceListener {
    private lateinit var binding:ActivityAllocateWorkBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    private lateinit var adapter: AttendanceAdapter
    private lateinit var labourList:List<Labour>
    private lateinit var listProject:List<ProjectData>
    private lateinit var labourDataList:List<LabourInfo>
    private var selectedProjectId=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAllocateWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database= AppDatabase.getDatabase(this)
        labourDao=database.labourDao()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.allocate_work)
        getProjectFromServer()
        labourList=ArrayList<Labour>()
        labourDataList=ArrayList<LabourInfo>()
        adapter= AttendanceAdapter(labourDataList,this)
        binding.recyclerViewAttendance.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        val names = listOf("Nashik I", "Pune II ", "Mumbai")
        val atvNamesAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, names
        )

        binding.projectArea.setOnFocusChangeListener{abaad,asd ->
            binding.projectArea.showDropDown()
        }
        binding.projectArea.setOnClickListener{
            binding.projectArea.showDropDown()
        }
            binding.ivSearchByLabourId.setOnClickListener {
                if(validateFields()){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        labourList=labourDao.getLabourByMgnregaIdLike(binding.etLabourId.text.toString())
                        if(labourList!==null){
                            Log.d("mytag","userListSize=>"+labourList.size)
                            runOnUiThread {
                               /* if(labourList.size>0){
                                    adapter= AttendanceAdapter(labourList,this@AllocateWorkActivity)
                                    binding.recyclerViewAttendance.adapter=adapter;
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter= AttendanceAdapter(labourList,this@AllocateWorkActivity)
                                    binding.recyclerViewAttendance.adapter=adapter;
                                    adapter.notifyDataSetChanged()
                                    val toast= Toast.makeText(this@AllocateWorkActivity,"Labour not found",
                                        Toast.LENGTH_SHORT)
                                    toast.show()
                                }*/
                            }
                        }else{
                            runOnUiThread {
                                Log.d("mytag",""+labourList.size)
                               /* adapter= AttendanceAdapter(labourList,this@AllocateWorkActivity)
                                binding.recyclerViewAttendance.adapter=adapter;
                                adapter.notifyDataSetChanged()
                                val toast= Toast.makeText(this@AllocateWorkActivity,"Labour not found",
                                    Toast.LENGTH_SHORT)
                                toast.show()*/

                            }
                        }
                    } catch (e: Exception) {
                        Log.d("mytag","Exception Inserted : ${e.message}")
                        e.printStackTrace()
                    }
                }
                }else{
                    val toast= Toast.makeText(this@AllocateWorkActivity,"Please enter details",
                        Toast.LENGTH_SHORT)
                    toast.show()
                }
            }


        binding.projectArea.onItemClickListener=AdapterView.OnItemClickListener { parent, view, position, id ->
            Log.d("mytag",""+position)
            binding.tvProjectAddress.setText(listProject.get(position).district+" -> "+listProject.get(position).taluka+" -> "+listProject.get(position).village)
            binding.tvProjectDuration.setText(listProject.get(position).start_date+" To "+listProject.get(position).end_date)
            selectedProjectId=listProject.get(position).id.toString()
        }

        binding.etLabourId.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text=s.toString()
                if(s?.length!! <1){
                    (labourList as ArrayList<Labour>).clear()
                }
            }
        })
    }
    private fun validateFields(): Boolean {
        var result= mutableListOf<Boolean>()
        if(binding.projectArea.enoughToFilter()){
            binding.projectArea.error=null
            result.add(true)

        }else{
            result.add(false)
            binding.projectArea.error="Select Project Area"
        }
        if(binding.etLabourId.text?.length!! >0){

            result.add(true)
            binding.etLabourId.error=null

        }else{
            result.add(false)
            binding.etLabourId.error="Please enter MGNREGA Id"
        }

        return !result.contains(false);
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showAttendanceDialog(fullName:String,labourImage:String) {
        val dialog= Dialog(this@AllocateWorkActivity)
        dialog.setContentView(R.layout.layout_dialog_mark_attendence)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
        val tvFullName=dialog.findViewById<TextView>(R.id.tvFullName)
        val ivPhoto=dialog.findViewById<ImageView>(R.id.ivPhoto)
        val radioGroupAttendance=dialog.findViewById<RadioGroup>(R.id.radioGroupAttendance)
        tvFullName.text = fullName
        Glide.with(this@AllocateWorkActivity).load(labourImage).into(ivPhoto)
        val btnSubmit=dialog.findViewById<Button>(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            if(radioGroupAttendance.checkedRadioButtonId==R.id.radioButtonFullDay || radioGroupAttendance.checkedRadioButtonId==R.id.radioButtonHalfDay )
            {
                val toast= Toast.makeText(this@AllocateWorkActivity,"Attendance marked successfully",
                    Toast.LENGTH_SHORT)
                toast.show()
                dialog.dismiss()
            }else{
                val toast= Toast.makeText(this@AllocateWorkActivity,"Select Day",
                    Toast.LENGTH_SHORT)
                toast.show()
            }

        }
    }
    override fun markAttendance(labour: Labour) {
        showAttendanceDialog(labour.fullName,labour.photo)
        (labourList as ArrayList<Labour>).clear()
        adapter.notifyDataSetChanged()
        binding.etLabourId.setText("")
        binding.tvProjectAddress.setText("")
        binding.projectArea.clearListSelection()
        binding.projectArea.setText("")
    }

    private fun getProjectFromServer(){

        val apiService=ApiClient.create(this@AllocateWorkActivity)
        val call=apiService.getProjectList()
        call.enqueue(object : Callback<ProjectLabourListForMarker>{
            override fun onResponse(
                call: Call<ProjectLabourListForMarker>,
                response: Response<ProjectLabourListForMarker>
            ) {
                Log.d("mytag",Gson().toJson(response.body()))
                if(response.isSuccessful) {
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
                            this@AllocateWorkActivity, "No data found",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()

                    }
                }else{
                    Toast.makeText(this@AllocateWorkActivity, "response unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                Log.d("mytag","onFailure getProjectFromServer "+t.message)
                Toast.makeText(this@AllocateWorkActivity, "Error occured during api unsuccessful", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getLabourByIdFromServer(mgnregaId: String){

        val apiService=ApiClient.create(this@AllocateWorkActivity)
        val call=apiService.getLabourDataById(mgnregaId)
        call.enqueue(object : Callback< LabourByMgnregaId>{
            override fun onResponse(
                call: Call< LabourByMgnregaId>,
                response: Response< LabourByMgnregaId>
            ) {
                Log.d("mytag",Gson().toJson(response.body()))
                if(response.isSuccessful) {
                    if (!response.body()?.data.isNullOrEmpty()) {
                        labourDataList = response.body()?.data!!
                        adapter.notifyDataSetChanged()
                    } else {
                        val toast = Toast.makeText(
                            this@AllocateWorkActivity, "No data found",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()

                    }
                }else{
                    Toast.makeText(this@AllocateWorkActivity, "response unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call< LabourByMgnregaId>, t: Throwable) {
                Log.d("mytag","onFailure getLabourByIdFromServer "+t.message)
                Toast.makeText(this@AllocateWorkActivity, "Error occured during api unsuccessful", Toast.LENGTH_SHORT).show()
            }
        })
    }
}