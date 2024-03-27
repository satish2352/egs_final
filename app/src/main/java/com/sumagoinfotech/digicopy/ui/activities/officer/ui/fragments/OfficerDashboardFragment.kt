package com.sumagoinfotech.digicopy.ui.activities.officer.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.FragmentDashboardOfficerBinding
import com.sumagoinfotech.digicopy.model.apis.DocumentDownloadModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectMarkerData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.LabourData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.ui.activities.ScanBarcodeActivity
import com.sumagoinfotech.digicopy.ui.activities.ScannerActivity
import com.sumagoinfotech.digicopy.utils.CustomInfoWindowAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.FileDownloader
import com.sumagoinfotech.digicopy.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OfficerDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OfficerDashboardFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentDashboardOfficerBinding
    private var markersList = mutableListOf<ProjectMarkerData>()
    private var labourData = mutableListOf<LabourData>()
    private var projectData = mutableListOf<ProjectData>()
    lateinit var dialog: CustomProgressDialog
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null // Reference to the current location marker
    private var isCurrentMarkerVisible = true

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
        try {
            dialog = CustomProgressDialog(requireContext())
            binding = FragmentDashboardOfficerBinding.inflate(inflater, container, false)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
            mapFragment.getMapAsync(this)
            binding.layoutByProjectId.setOnClickListener {
                //fetchProjectDataForMarker(binding.etInput.text.toString())
                Log.d("mytag", binding.etInput.text.toString())
            }
            binding.layoutByLabourId.setOnClickListener {
                //fetchLabourDataForMarker(binding.etInput.text.toString())
            }
            binding.layoutScanQR.setOnClickListener {

                if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                    startScanner()
                }else{
                    requestThePermissions()
                }
            }
        } catch (e: Exception) {
            Log.d("mytag", "Exception " + e.message)
        }
        return binding.root;
    }
    private fun requestThePermissions() {

        PermissionX.init(requireActivity())
            .permissions(android.Manifest.permission.CAMERA)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {

                } else {
                }
            }
    }
    private fun startScanner() {
        ScannerActivity.startScanner(requireContext()) { barcodes ->
            barcodes.forEach { barcode ->
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {

                    }
                    Barcode.TYPE_CONTACT_INFO -> {

                    }
                    else -> {

                        getFileDownloadUrl(barcode.rawValue.toString())
                        Log.d("mytag",""+barcode.rawValue.toString())

                    }
                }
            }
        }
    }
    private fun getFileDownloadUrl(fileName:String){


        val dialog=CustomProgressDialog(requireContext())
        dialog.show()
        val apiService= ApiClient.create(requireContext())
        val call=apiService.downloadPDF(fileName)
        call.enqueue(object : Callback<DocumentDownloadModel> {
            override fun onResponse(call: Call<DocumentDownloadModel>, response: Response<DocumentDownloadModel>) {
                dialog.dismiss()
                if(response.isSuccessful){

                    if(response.body()?.status.equals("true")){
                        val url=response.body()?.data+"/"+fileName
                        Log.d("mytag",url)
                        FileDownloader.downloadFile(requireContext(),url,fileName)
                    }else{
                        Toast.makeText(requireContext(),response.body()?.message,Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(requireContext(),"response unsuccessful",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DocumentDownloadModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(requireContext(),"response failed",Toast.LENGTH_SHORT).show()
            }
        })

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowClickListener(this)
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
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(layoutInflater))
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMarkerClick(p0: Marker): Boolean {

        return true
    }

    override fun onInfoWindowClick(p0: Marker) {

    }
}