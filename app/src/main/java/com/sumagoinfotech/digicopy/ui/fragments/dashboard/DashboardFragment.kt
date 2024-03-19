package com.sumagoinfotech.digicopy.ui.fragments.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.FragmentDashboardBinding
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectMarkerData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.LabourData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectData
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.ui.activities.LabourListByProjectActivity
import com.sumagoinfotech.digicopy.ui.activities.registration.LabourRegistration1Activity
import com.sumagoinfotech.digicopy.ui.activities.ScanBarcodeActivity
import com.sumagoinfotech.digicopy.ui.activities.ViewLabourFromMarkerClick
import com.sumagoinfotech.digicopy.utils.CustomInfoWindowAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener {

    private var _binding: FragmentDashboardBinding? = null
    private var markersList= mutableListOf<ProjectMarkerData>()
    private var labourData= mutableListOf<LabourData>()
    private var projectData= mutableListOf<ProjectData>()
    lateinit var dialog:CustomProgressDialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null // Reference to the current location marker
    private var isCurrentMarkerVisible=true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
            dialog= CustomProgressDialog(requireContext())
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        try {
            binding.layoutRegisterLabour.setOnClickListener {
                val intent= Intent(activity, LabourRegistration1Activity::class.java)
                startActivity(intent)
            }
            binding.layoutScanQR.setOnClickListener {
                val intent= Intent(activity,ScanBarcodeActivity::class.java)
                startActivity(intent)
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
            mapFragment.getMapAsync(this)
            binding.layoutByProjectId.setOnClickListener {
                fetchProjectDataForMarker(binding.etInput.text.toString())
                Log.d("mytag",binding.etInput.text.toString())
            }
            binding.layoutByLabourId.setOnClickListener {
                fetchLabourDataForMarker(binding.etInput.text.toString())
            }
        } catch (e: Exception) {
           Log.d("mytag","Exception "+e.message)
        }
        return root
    }
    private fun fetchDataAndShowMarkers() {

        val apiService=ApiClient.create(requireContext())
        apiService.getProjectListForMap().enqueue(object : Callback<ProjectListModel>{
            override fun onResponse(
                call: Call<ProjectListModel>,
                response: Response<ProjectListModel>
            ) {

                if(response.isSuccessful){
                    Log.d("mytag",""+response.body()?.data?.get(0)?.latitude)
                    markersList= response.body()?.data as MutableList<ProjectMarkerData>
                    isCurrentMarkerVisible=false
                    addMarkersToMap()
                }else{
                }
            }
            override fun onFailure(call: Call<ProjectListModel>, t: Throwable) {
            }
        })
    }
    private fun fetchLabourDataForMarker( mgnregaId: String){
        dialog.show()
        val apiService=ApiClient.create(requireContext())
        apiService.getLabourForMarker(mgnregaId).enqueue(object : Callback<ProjectLabourListForMarker>{
            override fun onResponse(
                call: Call<ProjectLabourListForMarker>,
                response: Response<ProjectLabourListForMarker>
            ) {

                dialog.dismiss()
                if(response.isSuccessful){
                    if(!response.body()?.labour_data.isNullOrEmpty()) {
                        Log.d("mytag", Gson().toJson(response.body()))
                        labourData = response.body()?.labour_data as MutableList<LabourData>
                        if (labourData.size > 0) {
                            showLabourMakkers(labourData)
                        }
                    }else {
                        Toast.makeText(requireContext(), "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Toast.makeText(requireContext(), "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                Toast.makeText(requireContext(), "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }

    private fun showLabourMakkers(labourData: MutableList<LabourData>) {
        try {
            labourData.forEach { marker ->
                val latLongArray = marker.latitude.split(",")
                val latitude = latLongArray[0].toDouble()
                val longitude = latLongArray[1].toDouble()
                //val position = LatLng(latitude.toDouble(), longitude.toDouble())
                val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                val myMarker=map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(marker.full_name).snippet(marker.district_id+" "+marker.taluka_id+" "+marker.village_id)
                )
                myMarker?.tag=marker.mgnrega_card_id
                myMarker?.showInfoWindow()
            }
            // Move camera to the first marker
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(labourData[0].latitude.toDouble(), labourData[0].longitude.toDouble()), 15f))
        } catch (e: Exception) {
        }

    }

    private fun fetchProjectDataForMarker( projectName: String){
        dialog.show()
        val apiService=ApiClient.create(requireContext())
        apiService.getProjectListForMarker(projectName).enqueue(object : Callback<ProjectLabourListForMarker>{
            override fun onResponse(
                call: Call<ProjectLabourListForMarker>,
                response: Response<ProjectLabourListForMarker>
            ) {
                dialog.dismiss()
                if(response.isSuccessful){
                    Log.d("mytag",Gson().toJson(response.body()))
                    if(!response.body()?.project_data.isNullOrEmpty()){
                        projectData= response.body()?.project_data as MutableList<ProjectData>
                        if(projectData.size>0){
                            showProjectMarkers(projectData)
                        }
                    }else{
                        Toast.makeText(requireContext(), "No records found", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ProjectLabourListForMarker>, t: Throwable) {
                Toast.makeText(requireContext(), "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }

    private fun showProjectMarkers(projectData: MutableList<ProjectData>) {
        map.clear()

        try {
            projectData.forEach { marker ->
                /*val latLongArray = marker.latitude.split(",")
                val latitude = latLongArray[0].toDouble()
                val longitude = latLongArray[1].toDouble()*/

                val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                val myMarker=map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Project : "+marker.project_name).snippet(marker.district)
                )
                myMarker?.tag=marker.id
                myMarker?.showInfoWindow()
            }
            // Move camera to the first marker
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(projectData[0].latitude.toDouble(), projectData[0].longitude.toDouble()), 15f))
        } catch (e: Exception) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private  fun addMarkersToMap() {
        //val icon = bitmapDescriptorFromVector( R.drawable.ic_marker, R.color.appBlue)
        markersList.forEach { marker ->
            val position = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
            val myMarker=map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(marker.project_name).snippet(marker.district)
            )

            myMarker?.showInfoWindow()
        }
        // Move camera to the first marker
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(markersList[0].latitude.toDouble(), markersList[0].longitude.toDouble()), 15f))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d("mytag","onMarkerClick"+marker.title)
        val toast=Toast.makeText(activity,""+marker.title+ " "+marker.snippet+" "+marker.position,Toast.LENGTH_SHORT)
        toast.show()
        return false
    }
    private fun bitmapDescriptorFromVector(
        drawableId: Int,
        colorId: Int
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        drawable?.setTint(ContextCompat.getColor(requireContext(), colorId))
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    override fun onInfoWindowClick(marker: Marker) {

        if(marker.title?.startsWith("Project :")!!){
            val intent=Intent(context,LabourListByProjectActivity::class.java)
            intent.putExtra("id",""+marker.tag)
            context?.startActivity(intent)
        }else if(marker.title?.startsWith("You")!!){
        }
        else{
            val intent=Intent(context,ViewLabourFromMarkerClick::class.java)
            intent.putExtra("id",""+marker.tag)
            context?.startActivity(intent)
        }
        //Toast.makeText(requireContext(), "${marker.tag}Info window clicked: ${marker.title}", Toast.LENGTH_SHORT).show()
    }
}