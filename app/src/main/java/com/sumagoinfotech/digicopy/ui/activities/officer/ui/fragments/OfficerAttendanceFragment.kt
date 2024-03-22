package com.sumagoinfotech.digicopy.ui.activities.officer.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.ViewAttendanceAdapter
import com.sumagoinfotech.digicopy.databinding.FragmentOfficerAttendanceBinding
import com.sumagoinfotech.digicopy.interfaces.AttendanceEditListener
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService

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

}