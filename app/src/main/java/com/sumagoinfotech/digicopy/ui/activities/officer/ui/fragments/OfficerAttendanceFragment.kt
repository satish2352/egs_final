package com.sumagoinfotech.digicopy.ui.activities.officer.ui.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.databinding.FragmentOfficerAttendanceBinding
import com.sumagoinfotech.digicopy.interfaces.AttendanceEditListener
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.MySharedPref
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OfficerAttendanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OfficerAttendanceFragment : Fragment(),AttendanceEditListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding:FragmentOfficerAttendanceBinding
    private lateinit var attendanceList:ArrayList<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var dialog: CustomProgressDialog
    private lateinit var listProject: List<ProjectData>
    private var selectedProjectId=""
    private lateinit var appDatabase: AppDatabase
    private lateinit var areaDao: AreaDao
    private  var isInternetAvailable=false
    private lateinit var talukaList:List<AreaItem>
    private lateinit var villageList:List<AreaItem>
    private var villageNames= mutableListOf<String>()
    private var talukaNames= mutableListOf<String>()
    private var talukaId=""
    private var villageId=""
    private var startDate=""
    private var endDate=""
    private lateinit var mySharedPref:MySharedPref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentOfficerAttendanceBinding.inflate(layoutInflater,container,false)
        try {
            appDatabase=AppDatabase.getDatabase(requireActivity())
            areaDao=appDatabase.areaDao()
            mySharedPref= MySharedPref(requireContext())
            CoroutineScope(Dispatchers.IO).launch {
                talukaList=areaDao.getAllTalukas("3")
                villageList=areaDao.getVillageByTaluka("641")

                withContext(Dispatchers.Main){
                    for (taluka in talukaList)
                    {
                        talukaNames.add(taluka.name)
                    }
                    Log.d("mytag",""+talukaNames.size);
                    val talukaAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, talukaNames
                    )
                    binding.actSelectTaluka.setAdapter(talukaAdapter)
                   /* for (village in villageList){
                        villageNames.add(village.name)
                    }
                    Log.d("mytag",""+villageNames.size);
                    val villageAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, villageNames
                    )
                    binding.actSelectVillage.setAdapter(villageAdapter)*/

                    binding.actSelectTaluka.setOnFocusChangeListener { abaad, asd ->
                        binding.actSelectTaluka.showDropDown()
                    }
                    binding.actSelectTaluka.setOnClickListener {
                        binding.actSelectTaluka.showDropDown()
                    }


                }
            }
            dialog= CustomProgressDialog(requireContext())
            apiService = ApiClient.create(requireContext())
            binding.recyclerView.layoutManager= LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            attendanceList=ArrayList()
            adapter= ViewAttendanceAdapter(attendanceList,this)
            binding.recyclerView.adapter=adapter
            dialog= CustomProgressDialog(requireActivity())
            apiService = ApiClient.create(requireActivity())
            binding.recyclerView.layoutManager=LinearLayoutManager(requireActivity(),RecyclerView.VERTICAL,false)
            attendanceList=ArrayList()
            listProject=ArrayList()
            adapter= ViewAttendanceAdapter(attendanceList,this@OfficerAttendanceFragment)
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

            binding.actSelectTaluka.setOnItemClickListener { parent, view, position, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    talukaId=talukaList[position].location_id
                    villageNames.clear();
                    binding.actSelectVillage.setText("")
                    villageList=areaDao.getVillageByTaluka(talukaList[position].location_id)
                    for (village in villageList){
                        villageNames.add(village.name)
                    }
                    val villageAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, villageNames
                    )
                    Log.d("mytag",""+villageNames.size)
                    withContext(Dispatchers.Main){
                        binding.actSelectVillage.setAdapter(villageAdapter)
                        binding.actSelectVillage.setOnFocusChangeListener { abaad, asd ->
                            binding.actSelectVillage.showDropDown()
                        }
                        binding.actSelectVillage.setOnClickListener {
                            binding.actSelectVillage.showDropDown()
                        }
                    }
                }
            }
            binding.actSelectVillage.setOnItemClickListener { parent, view, position, id ->
                villageId=villageList[position].location_id
            }
            binding.btnCloseTaluka.setOnClickListener {

                binding.actSelectTaluka.setText("")
                talukaId=""
            }
            binding.btnCloseVillage.setOnClickListener {

                binding.actSelectVillage.setText("")
                villageId=""
            }

            binding.btnClose.setOnClickListener {

                binding.actSelectProject.setText("")
                selectedProjectId=""
            }
            binding.layoutStartDate.setOnClickListener {
                showDatePicker(requireContext(),binding.etStartDate)
            }
            binding.layoutEndDate.setOnClickListener {
                showDatePicker(requireContext(),binding.etEndDate)
            }
            binding.layoutClearAll.setOnClickListener {

                binding.actSelectProject.setText("")
                binding.actSelectTaluka.setText("")
                binding.actSelectVillage.setText("")
                binding.etStartDate.setText("")
                binding.etEndDate.setText("")
                selectedProjectId=""
                talukaId=""
                villageId=""
                startDate=""
                endDate=""
            }
            getProjectList();
            getAttendanceList(selectedProjectId);
        } catch (e: Exception)
        {

        }
        return binding.root
    }
    private fun showDatePicker(context:Context,editText: EditText) {
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
    override fun onAttendanceEdit(data: AttendanceData, postion: Int) {

    }

    override fun onResume() {
        super.onResume()
        fetchAttendanceFromServer();
    }

    private fun fetchAttendanceFromServer() {



    }

    private fun getAttendanceList(selectedProjectId: String) {

        dialog.show()
        val call=apiService.getListOfMarkedAttendance(selectedProjectId);
        call.enqueue(object : Callback<AttendanceModel> {
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
                        adapter= ViewAttendanceAdapter(attendanceList,this@OfficerAttendanceFragment)
                        binding.recyclerView.adapter=adapter
                        adapter.notifyDataSetChanged()
                    }
                }else{
                    Toast.makeText(requireActivity(), "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AttendanceModel>, t: Throwable) {
                Toast.makeText(requireActivity(), "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }
    private fun getProjectList() {
        try {
            val apiService = ApiClient.create(requireActivity())
            val call = apiService.getProjectList()
            call.enqueue(object : Callback<ProjectLabourListForMarker> {
                override fun onResponse(
                    call: Call<ProjectLabourListForMarker>,
                    response: Response<ProjectLabourListForMarker>
                ) {
                    Log.d("mytag", "getProjectList=>"+ Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        if (!response.body()?.project_data.isNullOrEmpty()) {
                            listProject = response.body()?.project_data!!
                            val projectNames = mutableListOf<String>()
                            for (project in listProject) {
                                projectNames.add(project.project_name)
                            }

                            val projectNamesAdapter = ArrayAdapter(
                                requireActivity(),
                                android.R.layout.simple_list_item_1,
                                projectNames
                            )
                            binding.actSelectProject.setAdapter(projectNamesAdapter)
                        } else {
                            val toast = Toast.makeText(
                                requireActivity(), "No data found",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()

                        }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "response unsuccessful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                    Log.d("mytag", "onFailure getProjectFromServer " + t.message)
                    Toast.makeText(
                        requireActivity(),
                        "Error occured during api unsuccessful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
        }
    }

}