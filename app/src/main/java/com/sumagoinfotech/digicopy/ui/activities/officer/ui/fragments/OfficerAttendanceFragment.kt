package com.sumagoinfotech.digicopy.ui.activities.officer.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.databinding.FragmentOfficerAttendanceBinding
import com.sumagoinfotech.digicopy.interfaces.AttendanceEditListener
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            getProjectList();
            getAttendanceList(selectedProjectId);
        } catch (e: Exception) {

        }
        return binding.root
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