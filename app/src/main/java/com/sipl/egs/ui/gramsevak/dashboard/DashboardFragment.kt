package com.sipl.egs.ui.gramsevak.dashboard

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import com.google.mlkit.vision.barcode.common.Barcode
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.R
import com.sipl.egs.databinding.FragmentDashboardBinding
import com.sipl.egs.interfaces.OnLocationStateListener
import com.sipl.egs.model.apis.documetqrdownload.QRDocumentDownloadModel
import com.sipl.egs.model.apis.mapmarker.MapData
import com.sipl.egs.model.apis.mapmarker.MapMarkerModel
import com.sipl.egs.model.apis.projectlist.ProjectDataFromLatLong
import com.sipl.egs.model.apis.projectlist.ProjectsFromLatLongModel
import com.sipl.egs.model.apis.projectlistforofficermap.ProjectMarkerData
import com.sipl.egs.model.apis.projectlistmarker.LabourData
import com.sipl.egs.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sipl.egs.ui.activities.start.LoginActivity
import com.sipl.egs.ui.gramsevak.LabourListByProjectActivity
import com.sipl.egs.ui.gramsevak.ScannerActivity
import com.sipl.egs.ui.gramsevak.ViewLabourFromMarkerClick
import com.sipl.egs.ui.registration.LabourRegistration1Activity
import com.sipl.egs.utils.CustomInfoWindowAdapter
import com.sipl.egs.utils.CustomMarkerObject
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener, MapTypeBottomSheetDialogFragment.BottomSheetListener,
    OnMapLoadedCallback {

    private var _binding: FragmentDashboardBinding? = null
    private var markersList = mutableListOf<ProjectMarkerData>()
    private var labourData = mutableListOf<LabourData>()
    private var projectData = mutableListOf<ProjectDataFromLatLong>()
    private var mapMarkerData = mutableListOf<MapData>()
    lateinit var dialog: CustomProgressDialog
    private var latitude = ""
    private var longitude = ""
    private lateinit var adapter: ArrayAdapter<String>
    private var suggestionList= mutableListOf<String>()

    private var isInternetAvailable=false
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null // Reference to the current location marker
    val bottomSheetDialogFragment = MapTypeBottomSheetDialogFragment()
    private lateinit var locationEnabledListener:OnLocationStateListener
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog = CustomProgressDialog(requireActivity())
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        try {
            ReactiveNetwork
                .observeNetworkConnectivity(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectivity: Connectivity ->
                    Log.d("##", "=>" + connectivity.state())
                    if (connectivity.state().toString() == "CONNECTED") {
                        isInternetAvailable = true


                    } else {
                        isInternetAvailable = false
                    }
                }) { throwable: Throwable? -> }

            binding.layoutRegisterLabour.setOnClickListener {
                val intent = Intent(activity, LabourRegistration1Activity::class.java)
                startActivity(intent)
            }
            binding.layoutScanQR.setOnClickListener {

                if (ActivityCompat.checkSelfPermission(
                        requireActivity(), android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if(isInternetAvailable){
                        startScanner()
                    }else{
                        Toast.makeText(
                            requireActivity(), requireActivity().resources.getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    requestThePermissions()
                }
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            if (isLocationEnabled())
            {
                requestLocationUpdates()
            }
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
            mapFragment.getMapAsync(this)
            binding.layoutByProjectId.setOnClickListener {
                if(isInternetAvailable){
                    if(!binding.etInput.text.isNullOrEmpty())
                    {
                        fetchProjectDataForMarkerWhenSearchByName(binding.etInput.text.toString())
                    }else{

                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.please_enter_project_name), Toast.LENGTH_LONG
                        ).show()
                    }

                    Log.d("mytag", binding.etInput.text.toString())
                }else{
                    Toast.makeText(
                        requireActivity(), requireActivity().resources.getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_LONG
                    ).show()
                }


            }
            binding.layoutByLabourId.setOnClickListener {
                if(isInternetAvailable){
                    if(!binding.etInput.text.isNullOrEmpty())
                    {
                        fetchLabourDataForMarkerById(binding.etInput.text.toString())
                    }else{

                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.please_enter_mgnrega_id), Toast.LENGTH_LONG
                        ).show()
                    }
                }else{
                    Toast.makeText(
                        requireActivity(), requireActivity().resources.getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_LONG
                    ).show()
                }


            }
            binding.ivMapType.setOnClickListener {

                bottomSheetDialogFragment.setBottomSheetListener(this@DashboardFragment)
                bottomSheetDialogFragment.show(childFragmentManager, bottomSheetDialogFragment.tag)
            }

            adapter= ArrayAdapter(requireActivity(),android.R.layout.simple_dropdown_item_1line,suggestionList)
            binding.etInput.setAdapter(adapter)
            binding.etInput.threshold=3

            binding.etInput.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if(s?.length!!>=3){

                        CoroutineScope(Dispatchers.IO).launch {
                            getSuggestionsForMgnregaId(s.toString())
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if(s?.length!!>=3){
                        if(isInternetAvailable){
                            CoroutineScope(Dispatchers.IO).launch {
                                getSuggestionsForMgnregaId(s.toString())
                            }
                        }else{
                            Toast.makeText(
                                requireActivity(), requireActivity().resources.getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }
            })
        } catch (e: Exception) {
            Log.d("mytag", "Exception " + e.message)
            e.printStackTrace()
        }
        return root
    }

    suspend fun getSuggestionsForMgnregaId(text:String){

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val apiService=ApiClient.create(requireActivity())

                val response=apiService.getSuggestionForMgnregaId(text)
                if(response.isSuccessful){

                    if(response.body()?.status.equals("true"))
                    {
                        if(!response.body()?.data?.isNullOrEmpty()!!){
                            Log.d("mytag",""+ response.body()?.data!!.size)
                            withContext(Dispatchers.Main){
                                adapter.clear()
                                suggestionList.clear()
                                suggestionList= response.body()?.data?.toMutableList()!!
                                adapter= ArrayAdapter(requireActivity(),android.R.layout.simple_dropdown_item_1line,suggestionList)
                                binding.etInput.setAdapter(adapter)
                                adapter.notifyDataSetChanged()
                            }
                        }

                    }else{

                    }
                }else{

                }

            }
        } catch (e: Exception) {
            Log.d("mytag", "Exception " + e.message)
            e.printStackTrace()
        }
    }

    private fun requestLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
                )
                Log.d("mytag", "requestLocationUpdates()  return ")
                return
            }
            Log.d("mytag", "requestLocationUpdates() ")

            // Request last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        latitude
                    } ?: run {
                        // Handle case where location is null
                        if (isAdded && view != null) {
                            Toast.makeText(
                                requireActivity(), resources.getString(R.string.unable_to_retrive_location), Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun isLocationEnabled(): Boolean {
        try {
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
            return false
        }
    }

    private fun startScanner() {
        try {
            ScannerActivity.startScanner(requireActivity()) { barcodes ->
                barcodes.forEach { barcode ->
                    when (barcode.valueType) {
                        Barcode.TYPE_URL -> {}
                        Barcode.TYPE_CONTACT_INFO -> {}
                        else -> {
                            getFileDownloadUrl(barcode.rawValue.toString())
                            Log.d("mytag", "" + barcode.rawValue.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun requestThePermissions() {

        try {
            PermissionX.init(requireActivity()).permissions(android.Manifest.permission.CAMERA)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"
                    )
                }.onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }.request { allGranted, grantedList, deniedList ->
                    if (allGranted) {

                    } else {
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun fetchLabourDataForMarkerById(mgnregaId: String) {
        try {
            dialog.show()
            val apiService = ApiClient.create(requireActivity())
            apiService.getLabourDataForMarkerById(mgnregaId)
                .enqueue(object : Callback<ProjectLabourListForMarker> {
                    override fun onResponse(
                        call: Call<ProjectLabourListForMarker>,
                        response: Response<ProjectLabourListForMarker>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            if (!response.body()?.status.isNullOrEmpty() && response.body()?.status.equals(
                                    "true"
                                )
                            ) {
                                if (!response.body()?.labour_data.isNullOrEmpty()) {
                                    Log.d("mytag", Gson().toJson(response.body()))
                                    labourData = response.body()?.labour_data as MutableList<LabourData>
                                    if (labourData.size > 0) {
                                        showLabourMarkers(labourData)
                                    }
                                } else {
                                    Toast.makeText(
                                        requireActivity(), "No records found", Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(requireActivity(), "Please try again", Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Toast.makeText(
                                requireActivity(), "Response unsuccessful", Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                        Toast.makeText(
                            requireActivity(), "Error Ocuured during api call", Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }
                })
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun showLabourMarkers(labourData: MutableList<LabourData>) {
        map.clear()
        val redVue = 0F
        try {
            if (labourData.isNotEmpty()) {
                labourData.forEach { marker ->
                    Log.d("mytag", "showProjectMarkers: ${marker.latitude},${marker.longitude}")
                    val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                    var myMarker: Marker? = null
                    val markerIcon = BitmapDescriptorFactory.defaultMarker(redVue)
                    //val markerIcon=getMarkerIcon(Color.GREEN)
                    myMarker = map.addMarker(
                        MarkerOptions().position(position).icon(markerIcon).title(marker.full_name)
                    )
                    var customMarkerObject = CustomMarkerObject(
                        id = marker.mgnrega_card_id.toString(),
                        type = "labour",
                        name = marker.full_name,
                        url =marker.id.toString()
                    )
                    Log.d("mytag","=>mnrega" +
                            "||=>"+customMarkerObject.id)
                    Log.d("mytag","=>id=>"+customMarkerObject.url)
                    myMarker?.tag = customMarkerObject
                    myMarker?.showInfoWindow()
                }
                val firstMarker = labourData.last()
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstMarker.latitude.toDouble(), firstMarker.longitude.toDouble()
                        ), 13f
                    )
                )
                updateCurrentMarker()
            } else {
                // Handle case when projectData is empty
                Log.d("mytag", "showLabourMarkers:  data is empty")
            }
        } catch (e: Exception) {
            Log.d("mytag", "showLabourMarkers: Exception " + e.message)
            e.printStackTrace()
        }

    }

    private fun fetchProjectDataForMarkerWhenSearchByName(projectName: String) {
        try {
            dialog.show()
            val apiService = ApiClient.create(requireActivity())
            apiService.getProjectListForMarkerByNameSearch(projectName, latitude, longitude)
                .enqueue(object : Callback<ProjectsFromLatLongModel> {
                    override fun onResponse(
                        call: Call<ProjectsFromLatLongModel>,
                        response: Response<ProjectsFromLatLongModel>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("mytag", Gson().toJson(response.body()))
                            if (!response.body()?.project_data.isNullOrEmpty()) {
                                projectData =
                                    response.body()?.project_data as MutableList<ProjectDataFromLatLong>
                                if (projectData.size > 0) {
                                    showProjectMarkersWhenSearchByName(projectData)
                                    //showProjectMarkersWhenSearchByNameWithCluster(projectData)
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
                    }

                    override fun onFailure(call: Call<ProjectsFromLatLongModel>, t: Throwable) {
                        Toast.makeText(
                            requireActivity(), "Error Ocuured during api call", Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }
                })
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun showProjectMarkersWhenSearchByName(projectData: MutableList<ProjectDataFromLatLong>) {
        map.clear()
        val greenHue = 120F
        try {
            if (projectData.isNotEmpty()) {
                projectData.forEach { marker ->
                    Log.d("mytag", "showProjectMarkers: ${marker.latitude},${marker.longitude}")
                    val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                    var myMarker: Marker? = null
                    val markerIcon = BitmapDescriptorFactory.defaultMarker(greenHue)
                    //val markerIcon=getMarkerIcon(Color.GREEN)
                    myMarker = map.addMarker(
                        MarkerOptions().position(position).icon(markerIcon)
                            .title(marker.project_name)
                    )
                    var customMarkerObject = CustomMarkerObject(
                        id = marker.id.toString(),
                        type = "project",
                        name = marker.project_name,
                        url = ""
                    )
                    myMarker?.tag = customMarkerObject
                    myMarker?.showInfoWindow()
                }
                val firstMarker = projectData.last()
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstMarker.latitude.toDouble(), firstMarker.longitude.toDouble()
                        ), 13f
                    )
                )
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
    private fun showProjectMarkersWhenSearchByNameWithCluster(projectData: MutableList<ProjectDataFromLatLong>) {
        map.clear()
        val greenHue = 120F
        try {
            if (projectData.isNotEmpty()) {
                // Create a ClusterManager
                val clusterManager = ClusterManager<ClusterMarkerObject>(context, map)

                // Set custom renderer for clusters
                val clusterRenderer = CustomClusterRenderer(requireContext(), map, clusterManager)
                clusterManager.renderer = clusterRenderer

                // Add items to the ClusterManager
                projectData.forEach { marker ->
                    val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                    val customMarkerObject = ClusterMarkerObject(
                        id = marker.id.toString(),
                        type = "project",
                        name = marker.project_name,
                        url = "",
                        latLng = position,
                        snippets = "snipp"

                    )
                    clusterManager.addItem(customMarkerObject)
                }

                // Set OnClusterItemClickListener to handle individual marker clicks
                clusterManager.setOnClusterItemClickListener { item ->
                    // Handle marker click
                    true
                }

                // Set OnClusterClickListener to handle cluster clicks
                clusterManager.setOnClusterClickListener { cluster ->
                    // Handle cluster click
                    true
                }

                // Register ClusterManager with the map
                map.setOnCameraIdleListener(clusterManager)
                map.setOnMarkerClickListener(clusterManager)

                // Zoom to the extent of the clustered markers
                val visibleRegion = map.projection.visibleRegion
                val builder = LatLngBounds.builder()
                builder.include(visibleRegion.farLeft)
                builder.include(visibleRegion.farRight)
                builder.include(visibleRegion.nearLeft)
                builder.include(visibleRegion.nearRight)
                val bounds = builder.build()
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                //map.moveCamera(CameraUpdateFactory.newLatLngBounds(clusterManager.algorithm.visibleRegion.latLngBounds, 100))
                updateCurrentMarker()

                clusterManager.setOnClusterClickListener { cluster ->
                    val clusterMarkers = cluster.items.toList()
                    if (clusterMarkers.size == 2) {
                        // Handle the case where the cluster contains exactly two markers
                        val firstMarkerData = clusterMarkers[0].name // Assuming ClusterMarkerObject has a 'data' property containing the marker's data
                        val secondMarkerData = clusterMarkers[1].name

                        Log.d("mytag", "$firstMarkerData")
                        Log.d("mytag", "$secondMarkerData")
                        // Now you have the data of the two markers
                        // Handle the data as needed
                    } else {
                        // Handle other cases where the cluster contains more or less than two markers
                    }
                    true // Indicate that the click event has been handled
                }
            } else {
                // Handle case when projectData is empty
                Log.d("mytag", "showProjectMarkers: Project data is empty")
            }
        } catch (e: Exception) {
            Log.d("mytag", "showProjectMarkers: Exception " + e.message)
            e.printStackTrace()
        }
    }

    private fun showProjectMarkersNew(mapData: MutableList<MapData>) {
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
                    if (marker.type.equals("project")) {
                        val markerIcon = BitmapDescriptorFactory.defaultMarker(greenHue)
                        //val markerIcon=getMarkerIcon(Color.GREEN)
                        myMarker = map.addMarker(
                            MarkerOptions().position(position).icon(markerIcon).title(marker.name)
                        )
                        var customMarkerObject = CustomMarkerObject(
                            id = marker.id.toString(),
                            type = marker.type,
                            name = marker.name,
                            url = ""
                        )
                        myMarker?.tag = customMarkerObject
                        myMarker?.showInfoWindow()
                    } else if (marker.type.equals("labour")) {
                        //val markerIcon=getMarkerIcon(Color.RED)
                        val markerIcon = BitmapDescriptorFactory.defaultMarker(redHue)
                        myMarker = map.addMarker(
                            MarkerOptions().icon(markerIcon).position(position).title(marker.name)
                        )
                        var customMarkerObject = CustomMarkerObject(
                            id = marker.mgnrega_card_id.toString(),
                            type = "labour",
                            name = marker.name,
                            url =marker.id.toString()
                        )


                        myMarker?.tag = customMarkerObject
                        myMarker?.showInfoWindow()
                    } else if (marker.type.equals("document")) {
                        val markerIcon = BitmapDescriptorFactory.defaultMarker(purpleHue)
                        myMarker = map.addMarker(
                            MarkerOptions().icon(markerIcon).position(position)
                                .title(marker.document_name)
                        )
                        var customMarkerObject = CustomMarkerObject(
                            id = marker.id.toString(),
                            type = marker.type,
                            name = marker.document_name,
                            url = marker.document_pdf
                        )
                        myMarker?.tag = customMarkerObject
                        myMarker?.showInfoWindow()
                    }
                }
                // Move camera to the first marker
                val firstMarker = mapData.last()
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstMarker.latitude.toDouble(), firstMarker.longitude.toDouble()
                        ), 13f
                    )
                )

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            map.setOnMarkerClickListener(this)
            map.setOnInfoWindowClickListener(this)

            // Check location permission
            if (ContextCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            Log.d("mytag", "addOnSuccessListener => ${it.latitude} ${it.longitude}")
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            var pref = MySharedPref(requireActivity())
                            pref.setLatitude(it.latitude.toString())
                            pref.setLongitude(it.longitude.toString())
                            latitude = it.latitude.toString()
                            longitude = it.longitude.toString()
                            // fetchProjectDataFromLatLong(it.latitude.toString(),it.longitude.toString())
                            fetchProjectDataFromLatLongNew(
                                it.latitude.toString(), it.longitude.toString()
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))
                            // Add marker for current location
                            if (currentLocationMarker == null) {
                                val markerIcon =
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                                currentLocationMarker = map.addMarker(
                                    MarkerOptions().position(currentLatLng).title("You are here")
                                        .icon(markerIcon).snippet("${it.latitude}, ${it.longitude}")
                                )
                                latitude = it.latitude.toString()
                                longitude = it.longitude.toString()
                                currentLocationMarker?.showInfoWindow()
                            } else {
                                // If marker already exists, update its position
                                currentLocationMarker?.position = currentLatLng
                            }
                            currentLocationMarker?.tag = CustomMarkerObject(
                                id = "0", type = "current_location", url = "", name = "You Are Here"
                            )
                        } ?: run {
                            Toast.makeText(
                                requireActivity(), "Unable to retrieve location", Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            map.setInfoWindowAdapter(CustomInfoWindowAdapter(layoutInflater))
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        return false
    }


    override fun onInfoWindowClick(marker: Marker) {

        try {
            val customMarkerObject = marker.tag as CustomMarkerObject
            if (customMarkerObject.type.equals("project")) {
                val intent = Intent(context, LabourListByProjectActivity::class.java)
                intent.putExtra("id", "" + customMarkerObject.id)
                context?.startActivity(intent)
            } else if (customMarkerObject.type.equals("document")) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(customMarkerObject.url), "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        requireActivity(), "No PDF viewer application found", Toast.LENGTH_LONG
                    ).show()
                }
            } else if (customMarkerObject.type.equals("labour")) {
                Log.d("mytag",customMarkerObject.id.toString())
                Log.d("mytag",customMarkerObject.url)
                val intent = Intent(context, ViewLabourFromMarkerClick::class.java)
                  intent.putExtra("id", customMarkerObject.id.toString())
                  intent.putExtra("labour_id", Integer.parseInt(customMarkerObject.url))
                context?.startActivity(intent)
            } else if (customMarkerObject.type.equals("current_marker")){

            }
        } catch (e: Exception) {
            Log.d("mytag", "onInfoWindowClick: Exception => " + e.message,e)
                e.printStackTrace()
        }
    }


    private fun fetchProjectDataFromLatLongNew(latitude: String, longitude: String) {
        try {
            dialog.show()
            val apiService = ApiClient.create(requireActivity())
            apiService.getMapsMarkersFromLatLong(latitude, longitude)
                .enqueue(object : Callback<MapMarkerModel> {
                    override fun onResponse(
                        call: Call<MapMarkerModel>, response: Response<MapMarkerModel>
                    ) {
                        dialog.dismiss()
                        if(response.code()!=401){
                            if (response.isSuccessful) {
                                Log.d("mytag", Gson().toJson(response.body()))
                                if (!response.body()?.map_data.isNullOrEmpty()) {
                                    mapMarkerData = response.body()?.map_data as MutableList<MapData>
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

                    override fun onFailure(call: Call<MapMarkerModel>, t: Throwable) {
                        Toast.makeText(
                            requireActivity(), "onFailure Error Occurred during api call", Toast.LENGTH_LONG
                        ).show()

                        Log.d("mytag",t.message.toString())
                            t.printStackTrace()
                        dialog.dismiss()
                    }
                })
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }
    }

    private fun getFileDownloadUrl(fileName: String) {


        try {
            val dialog = CustomProgressDialog(requireActivity())
            dialog.show()
            val apiService = ApiClient.create(requireActivity())
            val call = apiService.downloadPDF(fileName)
            call.enqueue(object : Callback<QRDocumentDownloadModel> {
                override fun onResponse(
                    call: Call<QRDocumentDownloadModel>, response: Response<QRDocumentDownloadModel>
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
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    requireActivity(),
                                    "No PDF viewer application found",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            //FileDownloader.downloadFile(requireActivity(), url, fileName)
                        } else {
                            Toast.makeText(
                                requireActivity(), response.body()?.message, Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        Toast.makeText(requireActivity(), "response unsuccessful", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                override fun onFailure(call: Call<QRDocumentDownloadModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireActivity(), "response failed", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","DashboardFragment",e)
            e.printStackTrace()
        }

    }

    override fun onDataReceived(data: String) {
        bottomSheetDialogFragment.dismiss()
        if (data.equals("normal")) {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        } else if (data.equals("hybrid")) {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
        } else if (data.equals("satellite")) {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLocationStateListener) {
            locationEnabledListener = context as OnLocationStateListener
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnLocationStateListener"
            )
        }
    }

    override fun onMapLoaded() {

    }

    override fun onResume() {
        super.onResume()
        if(!isLocationEnabled()){
            locationEnabledListener.onLocationStateChange(false)
        }
    }
}