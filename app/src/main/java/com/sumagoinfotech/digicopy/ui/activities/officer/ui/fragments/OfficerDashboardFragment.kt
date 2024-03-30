package com.sumagoinfotech.digicopy.ui.activities.officer.ui.fragments

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.sumagoinfotech.digicopy.model.apis.documetqrdownload.QRDocumentDownloadModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectMarkerData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.LabourData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.ui.activities.ScanBarcodeActivity
import com.sumagoinfotech.digicopy.ui.activities.ScannerActivity
import com.sumagoinfotech.digicopy.ui.fragments.dashboard.MapTypeBottomSheetDialogFragment
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
    GoogleMap.OnInfoWindowClickListener, MapTypeBottomSheetDialogFragment.BottomSheetListener {
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
    val bottomSheetDialogFragment = MapTypeBottomSheetDialogFragment()

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
            binding.ivMapType.setOnClickListener {

                bottomSheetDialogFragment.setBottomSheetListener(this@OfficerDashboardFragment)
                bottomSheetDialogFragment.show(childFragmentManager, bottomSheetDialogFragment.tag)
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
    private fun getFileDownloadUrl(fileName: String) {
        Log.d("mytag","filename ==>"+fileName)
        val activity = requireActivity()
        if (activity != null && isAdded && !isDetached) {
            val dialog = CustomProgressDialog(activity)
            dialog.show()
            val apiService = ApiClient.create(activity)
            val call = apiService.downloadPDF(fileName)
            call.enqueue(object : Callback<QRDocumentDownloadModel> {
                override fun onResponse(
                    call: Call<QRDocumentDownloadModel>,
                    response: Response<QRDocumentDownloadModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true")) {
                            val url = response.body()?.data?.document_pdf.toString()
                            Log.d("mytag", url)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(url), "application/pdf")
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            try {
                                activity.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    requireContext(),
                                    "No PDF viewer application found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            //FileDownloader.downloadFile(activity, url, fileName)
                            Toast.makeText(activity, "file download started", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(activity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<QRDocumentDownloadModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(activity, "response failed", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Fragment is not attached to an activity, handle accordingly
            // For example, log an error or show a message to the user
        }
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

    override fun onDataReceived(data: String) {
        bottomSheetDialogFragment.dismiss()
        if(data.equals("normal")){
            map.mapType=GoogleMap.MAP_TYPE_NORMAL
        }else if(data.equals("hybrid")){
            map.mapType=GoogleMap.MAP_TYPE_HYBRID
        }else if(data.equals("satellite")) {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }
}