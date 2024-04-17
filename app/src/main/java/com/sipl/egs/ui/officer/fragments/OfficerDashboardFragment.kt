package com.sipl.egs.ui.officer.fragments

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.R
import com.sipl.egs.databinding.FragmentDashboardOfficerBinding
import com.sipl.egs.model.apis.documetqrdownload.QRDocumentDownloadModel
import com.sipl.egs.model.apis.officermapdash.DashboardMapOfficerModel
import com.sipl.egs.model.apis.officermapdash.OfficerDashMapMarkers
import com.sipl.egs.model.apis.projectlistforofficermap.ProjectMarkerData
import com.sipl.egs.model.apis.projectlistmarker.LabourData
import com.sipl.egs.model.apis.projectlistmarker.ProjectData
import com.sipl.egs.ui.activities.start.LoginActivity
import com.sipl.egs.ui.gramsevak.LabourListByProjectActivity
import com.sipl.egs.ui.gramsevak.ScannerActivity
import com.sipl.egs.ui.gramsevak.dashboard.MapTypeBottomSheetDialogFragment
import com.sipl.egs.utils.CustomInfoWindowAdapter
import com.sipl.egs.utils.CustomMarkerObject
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.webservice.ApiClient
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
    private var latitude = ""
    private var longitude = ""
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
    private var mapMarkerData = mutableListOf<OfficerDashMapMarkers>()

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
            dialog = CustomProgressDialog(requireActivity())
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
        ScannerActivity.startScanner(requireActivity()) { barcodes ->
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
                                    requireActivity(),
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
                requireActivity(),
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
                        latitude = it.latitude.toString()
                        longitude = it.longitude.toString()
                        fetchProjectDataFromLatLongNew()
                        // Move camera to current location
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                        // Add marker for current location
                        if (currentLocationMarker == null) {
                            // If marker doesn't exist, create a new one
                            val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)

                            currentLocationMarker = map.addMarker(
                                MarkerOptions()
                                    .position(currentLatLng)
                                    .title("You are here")
                                    .icon(markerIcon)
                                    .snippet("${it.latitude}, ${it.longitude}")
                            )
                            currentLocationMarker?.showInfoWindow()
                        } else {
                            // If marker already exists, update its position
                            currentLocationMarker?.position = currentLatLng
                        }
                    } ?: run {
                        Toast.makeText(
                            requireActivity(),
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
    private fun fetchProjectDataFromLatLongNew() {
        dialog.show()
        val apiService = ApiClient.create(requireContext())
        apiService.getDashboardProjectListForOfficer()
            .enqueue(object : Callback<DashboardMapOfficerModel> {
                override fun onResponse(
                    call: Call<DashboardMapOfficerModel>, response: Response<DashboardMapOfficerModel>
                ) {
                    dialog.dismiss()
                    if(response.code()!=401){
                        if (response.isSuccessful) {
                            Log.d("mytag", Gson().toJson(response.body()))
                            if (!response.body()?.data.isNullOrEmpty()) {
                                mapMarkerData = response.body()?.data as MutableList<OfficerDashMapMarkers>
                                if (mapMarkerData.size > 0) {
                                   showProjectMarkersNew(mapMarkerData)
                                }
                            } else {
                                Toast.makeText(requireActivity(), "No records found", Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Toast.makeText(
                                requireActivity(), "Response unsuccessful", Toast.LENGTH_LONG
                            ).show()
                        }
                    }else{

                        val intent= Intent(requireActivity(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }

                }

                override fun onFailure(call: Call<DashboardMapOfficerModel>, t: Throwable) {
                    Toast.makeText(
                        requireActivity(), "onFailure Error Occurred during api call", Toast.LENGTH_LONG
                    ).show()

                    Log.d("mytag",t.message.toString())
                    t.printStackTrace()
                    dialog.dismiss()
                }
            })
    }
    private fun showProjectMarkersNew(mapData: MutableList<OfficerDashMapMarkers>) {
        map.clear()
        val redHue = 0F
        val orangeHue = 30F
        val yellowHue = 60F
        val greenHue = 120F
        val cyanHue = 180F
        val blueHue = 240F
        val purpleHue = 270F
        try {
            if (mapData.isNotEmpty()) {
                mapData.forEach { marker ->
                    Log.d("mytag", "showProjectMarkersNew: ${marker.latitude},${marker.longitude}")
                    val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                    var myMarker: Marker? = null
                    val markerIcon = BitmapDescriptorFactory.defaultMarker(greenHue)
                    //val markerIcon=getMarkerIcon(Color.GREEN)
                    myMarker = map.addMarker(
                        MarkerOptions().position(position).icon(markerIcon).title(marker.project_name)
                    )
                    var customMarkerObject = CustomMarkerObject(
                        id = marker.id.toString(),
                        type = "project",
                        name = marker.project_name,
                        url = ""
                    )
                    myMarker?.tag = customMarkerObject
                    myMarker?.showInfoWindow()
                    // Move camera to the first marker
                    val firstMarker = mapData.last()
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                firstMarker.latitude.toDouble(), firstMarker.longitude.toDouble()
                            ), 13f
                        )
                    )


                }
                updateCurrentMarker()

            } else {
                // Handle case when projectData is empty
                Log.d("mytag", "showProjectMarkers: Project data is empty")
            }
        } catch (e: Exception) {
            Log.d("mytag", "showProjectMarkers: Exception " + e.message)
            e.printStackTrace()
        }
    }
    private fun updateCurrentMarker()
    {
        try {
            var currentMarker: Marker? = null
            val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            currentMarker = map.addMarker(
                MarkerOptions().icon(markerIcon).position(LatLng(latitude.toDouble(),longitude.toDouble()))
                    .title("You are here")
            )
            var customMarkerObject = CustomMarkerObject(
                id = "",
                type = "current_marker",
                name ="",
                url = "")
            currentMarker?.tag = customMarkerObject
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMarkerClick(p0: Marker): Boolean {

        return false
    }

    override fun onInfoWindowClick(marker: Marker) {
        try {
            val customMarkerObject = marker.tag as CustomMarkerObject
            if (customMarkerObject.type.equals("project")) {
                val intent = Intent(context, LabourListByProjectActivity::class.java)
                intent.putExtra("id", "" + customMarkerObject.id)
                context?.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.d("mytag", "onInfoWindowClick: Exception => " + e.message)
        }
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

    override fun onResume() {
        super.onResume()
    }
}