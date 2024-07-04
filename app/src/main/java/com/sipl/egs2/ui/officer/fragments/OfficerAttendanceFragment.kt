package com.sipl.egs2.ui.officer.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sipl.egs2.R
import com.sipl.egs2.adapters.ViewAttendanceAdapter
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.dao.AreaDao
import com.sipl.egs2.database.entity.AreaItem
import com.sipl.egs2.databinding.FragmentOfficerAttendanceBinding
import com.sipl.egs2.interfaces.AttendanceEditListener
import com.sipl.egs2.model.apis.attendance.AttendanceData
import com.sipl.egs2.model.apis.attendance.AttendanceModel
import com.sipl.egs2.model.apis.projectlistofficer.ProjectDataForOfficer
import com.sipl.egs2.model.apis.projectlistofficer.ProjectListForOfficerModel
import com.sipl.egs2.pagination.MyPaginationAdapter
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.MySharedPref
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.ApiService
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
class OfficerAttendanceFragment : Fragment(),AttendanceEditListener,
    MyPaginationAdapter.OnPageNumberClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding:FragmentOfficerAttendanceBinding
    private lateinit var attendanceList:ArrayList<AttendanceData>
    private lateinit var apiService: ApiService
    private lateinit var adapter: ViewAttendanceAdapter
    private lateinit var dialog: CustomProgressDialog
    private lateinit var listProject: List<ProjectDataForOfficer>
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
    private var fromDate=""
    private var toDate=""
    private lateinit var mySharedPref:MySharedPref
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager

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
                talukaList=areaDao.getAllTalukas(mySharedPref.getOfficerDistrictId()!!)

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

            paginationAdapter= MyPaginationAdapter(0,"0",this@OfficerAttendanceFragment)
            paginationLayoutManager=LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            binding.recyclerViewPageNumbers.adapter=paginationAdapter
            currentPage="1"


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
                getAttendanceList()
            }
            binding.actSelectProject.setOnFocusChangeListener { v, hasFocus ->
                binding.actSelectProject.showDropDown()
            }
            binding.btnClose.setOnClickListener {
                binding.actSelectProject.setText("")
                selectedProjectId=""
                getAttendanceList()
            }
            binding.actSelectTaluka.setOnItemClickListener { parent, view, position, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main){
                        talukaId=talukaList[position].location_id
                        getAttendanceList()
                        getProjectList()
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
                        binding.actSelectVillage.setAdapter(villageAdapter)
                        binding.actSelectVillage.setOnFocusChangeListener { abaad, asd ->
                            binding.actSelectVillage.showDropDown()
                        }
                        binding.actSelectVillage.setOnClickListener {
                            binding.actSelectVillage.showDropDown()
                        }
                        binding.actSelectVillage.setOnItemClickListener { parent, view, position, id ->
                            villageId=villageList[position].location_id
                            getAttendanceList()
                            getProjectList()
                        }
                    }
                }
            }

            binding.btnCloseTaluka.setOnClickListener {
                binding.actSelectTaluka.setText("")
                binding.actSelectVillage.setText("")
                talukaId=""
                binding.actSelectVillage.setAdapter(null)
                villageId=""


                getAttendanceList()
            }
            binding.btnCloseVillage.setOnClickListener {
                binding.actSelectVillage.setText("")
                villageId=""
                getAttendanceList()
            }

            binding.btnClose.setOnClickListener {
                binding.actSelectProject.setText("")
                selectedProjectId=""
                getAttendanceList()
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
                fromDate=""
                toDate=""
                currentPage="1"
                //getProjectList();
                getAttendanceList();
            }
            getProjectList();
            getAttendanceList();
            binding.btnClose.visibility=View.GONE
            binding.btnCloseTaluka.visibility=View.GONE
            binding.btnCloseVillage.visibility=View.GONE
            binding.btnSearch.setOnClickListener {
                getAttendanceList()
            }
            binding.actSelectProject.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {

                    val length = s?.length ?: 0
                    val text = s?.toString() ?: ""
                    if (length > 0) {
                        binding.btnClose.visibility = View.VISIBLE
                    } else {
                        binding.btnClose.visibility = View.GONE
                    }
                }
            })
            binding.actSelectTaluka.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {

                    val length = s?.length ?: 0
                    val text = s?.toString() ?: ""
                    if (length > 0) {
                        binding.btnCloseTaluka.visibility = View.VISIBLE
                    } else {
                        binding.btnCloseTaluka.visibility = View.GONE
                    }
                }
            })
            binding.actSelectVillage.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {

                    val length = s?.length ?: 0
                    val text = s?.toString() ?: ""
                    if (length > 0) {
                        binding.btnCloseVillage.visibility = View.VISIBLE
                    } else {
                        binding.btnCloseVillage.visibility = View.GONE
                    }
                }
            })

        } catch (e: Exception)
        {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
        return binding.root
    }
    private fun showDatePicker(context:Context,editText: TextView) {
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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

    private fun getAttendanceList() {

        try {
            fromDate=binding.etStartDate.text.toString()
            toDate=binding.etEndDate.text.toString()
            dialog.show()
            val call=apiService.getAttendanceListForOfficer(selectedProjectId,talukaId,villageId,fromDate,toDate, startPageNumber = currentPage);
            call.enqueue(object : Callback<AttendanceModel> {
                override fun onResponse(
                    call: Call<AttendanceModel>,
                    response: Response<AttendanceModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful)
                    {
                        attendanceList.clear()
                        if(response.body()?.status.equals("true"))
                        {
                            if(response.body()?.data!=null)
                            {
                                attendanceList= (response.body()?.data as ArrayList<AttendanceData>?)!!
                                if(attendanceList.size<1){
                                    Toast.makeText(requireActivity(), "No records found", Toast.LENGTH_SHORT).show()
                                }
                                adapter= ViewAttendanceAdapter(attendanceList,this@OfficerAttendanceFragment)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()

                                val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@OfficerAttendanceFragment)
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()
                                paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)

                            }else{
                                if(!response.body()?.message.isNullOrEmpty()){
                                    Toast.makeText(requireActivity(), response.body()?.message, Toast.LENGTH_SHORT).show()
                                }
                                paginationAdapter= MyPaginationAdapter(0,"0",this@OfficerAttendanceFragment)
                                binding.recyclerViewPageNumbers.adapter=paginationAdapter
                                paginationAdapter.notifyDataSetChanged()
                            }

                        }
                    }else{
                        Toast.makeText(requireActivity(), resources.getString(R.string.response_unsuccessfull), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AttendanceModel>, t: Throwable) {
                    Toast.makeText(requireActivity(), resources.getString(R.string.error_occured_during_api_call), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                }
            })
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
    private fun getProjectList() {
        try {
            val apiService = ApiClient.create(requireActivity())
            val call = apiService.getProjectListForOfficer()
            call.enqueue(object : Callback<ProjectListForOfficerModel> {
                override fun onResponse(
                    call: Call<ProjectListForOfficerModel>,
                    response: Response<ProjectListForOfficerModel>
                ) {
                    Log.d("mytag", "getProjectList=>"+ Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        if (!response.body()?.data.isNullOrEmpty()) {
                            listProject = response.body()?.data!!
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

                override fun onFailure(call: Call<ProjectListForOfficerModel>, t: Throwable) {
                    Log.d("mytag", "onFailure getProjectFromServer " + t.message)
                    Toast.makeText(
                        requireActivity(),
                        "Error occured during api unsuccessful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        currentPage="$pageNumber"
        getAttendanceList()
        paginationAdapter.setSelectedPage(pageNumber)
    }

}