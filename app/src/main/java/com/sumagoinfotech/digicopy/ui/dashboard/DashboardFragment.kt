package com.sumagoinfotech.digicopy.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // Add the MapFragment to the FrameLayout

//        val mapFragment = DashboardFragment()
//
//        // Add the MapFragment to the FrameLayout
//        childFragmentManager.beginTransaction()
//            .replace(R.id.map_container, mapFragment)
//            .commit()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment

        // Obtain the GoogleMap object
        mapFragment.getMapAsync { googleMap ->

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}