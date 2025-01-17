package com.sipl.egs2.ui.gramsevak.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sipl.egs2.databinding.FragmentMapTypeBottomSheetDialogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapTypeBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapTypeBottomSheetDialogFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding:FragmentMapTypeBottomSheetDialogBinding
    interface BottomSheetListener {
        fun onDataReceived(data: String)
    }

    private var listener: BottomSheetListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    fun setBottomSheetListener(listener: BottomSheetListener) {
        this.listener = listener
    }

    private fun sendDataToFragment(data: String) {
        listener?.onDataReceived(data)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentMapTypeBottomSheetDialogBinding.inflate(layoutInflater, container, false)
        binding.layoutNormal.setOnClickListener {
            sendDataToFragment("normal")
        }
        binding.layoutHybird.setOnClickListener {
            sendDataToFragment("hybrid")
        }
        binding.layoutSatellite.setOnClickListener {
            sendDataToFragment("satellite")
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
         * @return A new instance of fragment MapTypeBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapTypeBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}