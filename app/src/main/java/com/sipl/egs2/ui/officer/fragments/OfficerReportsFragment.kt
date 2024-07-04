package com.sipl.egs2.ui.officer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.sipl.egs2.databinding.FragmentOfficerReportsBinding
import com.sipl.egs2.model.apis.reportscount.ReportCountOfficer
import com.sipl.egs2.ui.officer.activities.OfficerDocsApprovedListActivity
import com.sipl.egs2.ui.officer.activities.OfficerDocsNotApprovedListActivity
import com.sipl.egs2.ui.officer.activities.OfficerDocsReSubmittedListActivity
import com.sipl.egs2.ui.officer.activities.OfficerDocsReceivedForApprovalListActivity
import com.sipl.egs2.ui.officer.activities.OfficerLabourNotApprovedListActivity
import com.sipl.egs2.ui.officer.activities.OfficerLabourReSubmittedListActivity
import com.sipl.egs2.ui.officer.activities.OfficerLaboursReceivedForApprovalActivity
import com.sipl.egs2.ui.officer.activities.OfficersLaboursApprovedListActivity
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.ApiService
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
    private lateinit var dialog: CustomProgressDialog
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
        try {
            binding.cardSentForApproval.setOnClickListener {
                val intent =
                    Intent(requireActivity(), OfficerLaboursReceivedForApprovalActivity::class.java)
                startActivity(intent)
            }
            binding.cardApproved.setOnClickListener {
                val intent = Intent(requireActivity(), OfficersLaboursApprovedListActivity::class.java)
                startActivity(intent)
            }
            binding.cardSentNotApproved.setOnClickListener {
                val intent = Intent(requireActivity(), OfficerLabourNotApprovedListActivity::class.java)
                startActivity(intent)
            }
            binding.cardDocsSentForApproval.setOnClickListener {
                val intent =
                    Intent(requireActivity(), OfficerDocsReceivedForApprovalListActivity::class.java)
                startActivity(intent)
            }
            binding.cardApprovedDocumets.setOnClickListener {
                val intent = Intent(requireActivity(), OfficerDocsApprovedListActivity::class.java)
                startActivity(intent)
            }
            binding.cardNotApprovedDocumets.setOnClickListener {
                val intent = Intent(requireActivity(), OfficerDocsNotApprovedListActivity::class.java)
                startActivity(intent)
            }
            binding.cardReSubmittedDocs.setOnClickListener {
                val intent = Intent(requireActivity(), OfficerDocsReSubmittedListActivity::class.java)
                startActivity(intent)
            }
            binding.cardReSubmittedLabour.setOnClickListener {
                val intent = Intent(requireActivity(), OfficerLabourReSubmittedListActivity::class.java)
                startActivity(intent)
            }
            dialog = CustomProgressDialog(requireContext())
            apiService = ApiClient.create(requireContext())

            binding.ivRefresh.setOnClickListener {
                getReportsCount()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }

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
            val call = apiService.getLaboursReportCount();
            call.enqueue(object : Callback<ReportCountOfficer> {
                override fun onFailure(call: Call<ReportCountOfficer>, t: Throwable) {
                    Toast.makeText(
                        requireActivity(),
                        "Error Occurred during api call",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }

                override fun onResponse(
                    call: Call<ReportCountOfficer>,
                    response: Response<ReportCountOfficer>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true")) {
                            Log.d("mytag", Gson().toJson(response.body()))
                            binding.tvApprovedCount.text =
                                response?.body()?.approved_count.toString()
                            binding.tvNotApproved.text =
                                response?.body()?.not_approved_count.toString()
                            binding.tvSentForApproval.text =
                                response?.body()?.sent_for_approval_count.toString()
                            binding.tvCountReSubmittedLabour.text =
                                response?.body()?.resubmitted_labour_count.toString()
                            binding.tvDocsReceivedForApprovalCount.text =
                                response?.body()?.sent_for_approval_document_count.toString()
                            binding.tvDocsNotApprovedCount.text =
                                response?.body()?.not_approved_document_count.toString()
                            binding.tvDocsApprovedCount.text =
                                response?.body()?.approved_document_count.toString()
                            binding.tvDocsReSubmittedCount.text =
                                response?.body()?.resubmitted_document_count.toString()
                        }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Error Occurred during api call",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
        }
    }
}