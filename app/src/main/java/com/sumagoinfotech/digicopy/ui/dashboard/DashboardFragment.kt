package com.sumagoinfotech.digicopy.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.FragmentDashboardBinding
import com.sumagoinfotech.digicopy.ui.activities.LabourRegistration1Activity
import com.sumagoinfotech.digicopy.ui.activities.ScanBarcodeActivity

class DashboardFragment : Fragment(), OnMapReadyCallback  {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null // Reference to the current location marker


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
        binding.layoutRegisterLabour.setOnClickListener {

            val intent= Intent(activity,LabourRegistration1Activity::class.java)
            startActivity(intent)
        }
        binding.layoutScanQR.setOnClickListener {

            val intent= Intent(activity,ScanBarcodeActivity::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment

        // Obtain the GoogleMap object
        //mapFragment.getMapAsync { googleMap -> }
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Check location permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Enable current location button
            map.isMyLocationEnabled = true

            // Get current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)

                        // Move camera to current location
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                        // Add marker for current location
                        if (currentLocationMarker == null) {
                            // If marker doesn't exist, create a new one
                            currentLocationMarker = map.addMarker(
                                MarkerOptions()
                                    .position(currentLatLng)
                                    .title("You are here")
                                    .snippet("${it.latitude}, ${it.longitude}")
                            )
                            currentLocationMarker?.showInfoWindow()
                        } else {
                            // If marker already exists, update its position
                            currentLocationMarker?.position = currentLatLng
                        }
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    public fun updateMarker(){
        if (isAdded && ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Move camera to current location
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Add marker for current location
                    if (currentLocationMarker == null) {
                        // If marker doesn't exist, create a new one
                        currentLocationMarker = map.addMarker(
                            MarkerOptions()
                                .position(currentLatLng)
                                .title("You are here")
                                .snippet("${it.latitude}, ${it.longitude}")
                        )
                        currentLocationMarker?.showInfoWindow()
                    } else {
                        // If marker already exists, update its position
                        currentLocationMarker?.position = currentLatLng
                    }
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}