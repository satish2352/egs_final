package com.sumagoinfotech.digicopy.ui.officer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sumagoinfotech.digicopy.databinding.FragmentOfficerReportsBinding
import com.sumagoinfotech.digicopy.model.apis.reportscount.ReportsCount
import com.sumagoinfotech.digicopy.ui.officer.activities.OfficerLabourNotApprovedListActivity
import com.sumagoinfotech.digicopy.ui.officer.activities.OfficerLaboursReceivedForApprovalActivity
import com.sumagoinfotech.digicopy.ui.officer.activities.OfficersLaboursApprovedListActivity
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
 * Use the [OfficerReportsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OfficerReportsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentOfficerReportsBinding
    private lateinit var dialog:CustomProgressDialog
    private lateinit var apiService: ApiService


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
        binding = FragmentOfficerReportsBinding.inflate(inflater, container, false)
        binding.cardSentForApproval.setOnClickListener {
            val intent= Intent(requireActivity(), OfficerLaboursReceivedForApprovalActivity::class.java)
            startActivity(intent)
        }
        binding.cardApproved.setOnClickListener {
            val intent= Intent(requireActivity(), OfficersLaboursApprovedListActivity::class.java)
            startActivity(intent)
        }
        binding.cardSentNotApproved.setOnClickListener {
            val intent= Intent(requireActivity(), OfficerLabourNotApprovedListActivity::class.java)
            startActivity(intent)
        }
        dialog=CustomProgressDialog(requireContext())
        apiService=ApiClient.create(requireContext())

       /* binding.cardRejected.setOnClickListener {
            val intent= Intent(requireActivity(),OfficerLaboursRejectedListActivity::class.java)
            startActivity(intent)
        }*/
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OfficerReportsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OfficerReportsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        getReportsCount()
    }
    private fun getReportsCount() {


        try {
            dialog.show()
            val call=apiService.getLaboursReportCount();
            call.enqueue(object : Callback<ReportsCount> {
                override fun onFailure(call: Call<ReportsCount>, t: Throwable) {
                    Toast.makeText(requireActivity(), "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                override fun onResponse(call: Call<ReportsCount>, response: Response<ReportsCount>) {
                    dialog.dismiss()
                    if(response.isSuccessful)
                    {
                        if(response.body()?.status.equals("true"))
                        {
                            binding.tvApprovedCount.text=response?.body()?.approved_count.toString()
                            binding.tvNotApproved.text=response?.body()?.not_approved_count.toString()
                            binding.tvReceivedCount.text=response?.body()?.sent_for_approval_count.toString()
                        }
                    }else{
                        Toast.makeText(requireActivity(), "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
        }
    }
}