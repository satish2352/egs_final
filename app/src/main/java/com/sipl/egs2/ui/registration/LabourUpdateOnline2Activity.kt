package com.sipl.egs2.ui.registration

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX
import com.sipl.egs2.R
import com.sipl.egs2.adapters.FamilyDetailsOnlineEditAdapter
import com.sipl.egs2.camera.CameraActivity
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.dao.GenderDao
import com.sipl.egs2.database.dao.LabourDao
import com.sipl.egs2.database.dao.MaritalStatusDao
import com.sipl.egs2.database.dao.RelationDao
import com.sipl.egs2.database.entity.Gender
import com.sipl.egs2.database.entity.MaritalStatus
import com.sipl.egs2.database.entity.Relation
import com.sipl.egs2.databinding.ActivityLabourUpdateOnline2Binding
import com.sipl.egs2.interfaces.OnDeleteListener
import com.sipl.egs2.model.apis.getlabour.FamilyDetail
import com.sipl.egs2.model.apis.masters.MastersModel
import com.sipl.egs2.ui.gramsevak.ReportsActivity
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.LabourInputData
import com.sipl.egs2.utils.MyValidator
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.FileInfo
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import io.getstream.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LabourUpdateOnline2Activity : AppCompatActivity(), OnDeleteListener {
    private lateinit var binding:ActivityLabourUpdateOnline2Binding
    lateinit var  etDob: AutoCompleteTextView
    lateinit var  etFullName: EditText
    lateinit var  actMaritalStatus: AutoCompleteTextView
    lateinit var  actRelationship: AutoCompleteTextView
    lateinit var  actGenderFamily: AutoCompleteTextView
    lateinit var  btnSubmit: Button
    var validationResults = mutableListOf<Boolean>()
    var familyDetailsList=ArrayList<com.sipl.egs2.model.apis.LaboureEditDetailsOnline.FamilyDetail>()
    lateinit var adapter: FamilyDetailsOnlineEditAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private val REQUEST_CODE_AADHAR_CARD = 100
    private  val REQUEST_CODE_PHOTO = 200
    private  val REQUEST_CODE_VOTER_ID = 300
    private  val REQUEST_CODE_MGNREGA_CARD = 400
    private lateinit var voterIdImagePath:String
    private lateinit var aadharIdImagePath:String
    private lateinit var photoImagePath:String
    private lateinit var mgnregaIdImagePath:String
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var labourInputData: LabourInputData
    private lateinit var labourDao: LabourDao
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private  var addressFromLatLong:String=""
    private  lateinit var genderDao: GenderDao
    private lateinit var maritalStatusDao: MaritalStatusDao
    private lateinit var relationDao: RelationDao
    private lateinit var genderList:List<Gender>
    private lateinit var relationList:List<Relation>
    private lateinit var maritalStatusList:List<MaritalStatus>
    private var genderNames= mutableListOf<String>()
    private var relationNames= mutableListOf<String>()
    private var maritalStatusNames= mutableListOf<String>()
    private var genderId=""
    private var relationId=""
    private var maritalStatusId=""
    private var mgnregaId=""
    private var labourId=""
    private lateinit var dialog:CustomProgressDialog
    private var isInternetAvailable=false

    private  var voterIdImagePathNew:String=""
    private  var aadharIdImagePathNew:String=""
    private  var photoImagePathNew:String=""
    private  var mgnregaIdImagePathNew:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLabourUpdateOnline2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.update_details_step_2)
            dialog= CustomProgressDialog(this)
            registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
            registrationViewModel.dataObject.observe(this) { labourData ->
                Log.d("mytag", "labourData.mobile")
                Log.d("mytag", labourData.mobile)
            }
            binding.layoutAdd.setOnClickListener {
                showAddFamilyDetailsDialog()
            }
            database= AppDatabase.getDatabase(this)
            labourDao=database.labourDao()
            genderDao=database.genderDao()
            relationDao=database.relationDao()
            maritalStatusDao=database.martialStatusDao()
            mgnregaId= intent.extras?.getString("id").toString()
            val labourIdx= intent.extras?.getString("labour_id")
            getDetailsFromServer(mgnregaId!!,labourIdx.toString()!!)
            ReactiveNetwork
                .observeNetworkConnectivity(applicationContext)
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
            CoroutineScope(Dispatchers.IO).launch {
                genderList=genderDao.getAllGenders()
                relationList=relationDao.getAllRelation()
                maritalStatusList=maritalStatusDao.getAllMaritalStatus()
                for(gender in genderList){
                    genderNames.add(gender.gender_name)
                }
                for(status in maritalStatusList){
                    maritalStatusNames.add(status.maritalstatus)
                }
                for(relation in relationList){
                    relationNames.add(relation.relation_title)
                }

            }
            requestThePermissions()
            labourInputData = intent.getSerializableExtra("LabourInputData") as LabourInputData
            // Log.d("mytag",registrationViewModel.fullName)
            val layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)
            binding.recyclerViewFamilyDetails.layoutManager=layoutManager;
            binding.btnUpdate.setOnClickListener {
                if(validateFormFields())
                {
                    if(isInternetAvailable){
                        val familyDetails= Gson().toJson(familyDetailsList).toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                //uploadLabourOnline()
                                uploadLabourOnlineImageOptional()
                            } catch (e: Exception) {
                                Log.d("mytag","Exception on update : ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }else{
                        val toast = Toast.makeText(applicationContext,
                            getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }else{
                    Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.select_all_documents),
                        Toast.LENGTH_SHORT).show()
                }
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            binding.ivChangeAadhar.setOnClickListener {

                requestThePermissions()
                if(isLocationEnabled()){
                    startCameraActivity(REQUEST_CODE_AADHAR_CARD)
                    requestLocationUpdates()
                }else{
                    showEnableLocationDialog()
                }

            }
            binding.ivChangePhoto.setOnClickListener {
                requestThePermissions()
                if(isLocationEnabled()){
                    startCameraActivity(REQUEST_CODE_PHOTO)
                    requestLocationUpdates()
                }else{
                    showEnableLocationDialog()
                }

            }
            binding.ivChangeVoterId.setOnClickListener {
                requestThePermissions()
                if(isLocationEnabled()){
                    startCameraActivity(REQUEST_CODE_VOTER_ID)
                    requestLocationUpdates()
                }else{
                    showEnableLocationDialog()
                }

            }
            binding.ivChangeMgnregaCard.setOnClickListener {
                requestThePermissions()
                if(isLocationEnabled()){
                    startCameraActivity(REQUEST_CODE_MGNREGA_CARD)
                    requestLocationUpdates()
                }else{
                    showEnableLocationDialog()
                }
            }
            binding.ivAadhar.setOnClickListener {
                showPhotoZoomDialog(aadharIdImagePath)
            }
            binding.ivMgnregaCard.setOnClickListener {
                showPhotoZoomDialog(mgnregaIdImagePath)
            }
            binding.ivVoterId.setOnClickListener {
                showPhotoZoomDialog(voterIdImagePath)
            }
            binding.ivPhoto.setOnClickListener {
                showPhotoZoomDialog(photoImagePath)
            }

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    val builder = AlertDialog.Builder(this@LabourUpdateOnline2Activity)
                    builder.setTitle("Exit")
                        .setMessage("Are you sure you want to exit this screen?")
                        .setPositiveButton("Yes") { _, _ ->
                            // If "Yes" is clicked, exit the app
                            finish()
                        }
                        .setNegativeButton(resources.getString(R.string.no),null)  // If "No" is clicked, do nothing
                        .show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }
    private fun requestLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000
                )
                return
            }

            // Request last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        latitude = it.latitude
                        longitude = it.longitude
                        // Update UI with latitude and longitude
                        // Note: getAddressFromLatLong() needs to be implemented to fetch address
                        // from latitude and longitude
                        binding.etLocation.setText("${it.latitude},${it.longitude}")
                        addressFromLatLong = getAddressFromLatLong()
                    } ?: run {
                        // Handle case where location is null
                        Toast.makeText(
                            this@LabourUpdateOnline2Activity,
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }
    private fun isLocationEnabled(): Boolean {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
            return false;
        }
    }
    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.enable_location_services_message))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                // Handle the case when the user refuses to enable location services
                Toast.makeText(
                    this@LabourUpdateOnline2Activity,
                    resources.getString(R.string.unable_to_retrive_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onResume() {
        super.onResume()
        getTheLocation()

    }

    fun loadImageWithRetry(url: String,imageView: ImageView,retryCount: Int = 3,) {
        try {
            Glide.with(imageView.context)
                .load(url)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.progress_bg) // Placeholder image while loading
                        .error(R.drawable.ic_error) // Image to display if loading fails
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
                        .skipMemoryCache(false) // Whether to skip the memory cache
                        .override(200,200) // Specify the size of the image

                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Retry once

                        loadImageWithRetry(url,imageView,retryCount-1,)
                        return false // Return false to let Glide handle the error
                    }
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message)
            e.printStackTrace()
        }
    }

    private fun validateFormFields(): Boolean {
        var validationResults = mutableListOf<Boolean>()

        if(photoImagePath.length>0){

            validationResults.add(true)
        }else{
            validationResults.add(false)
        }
        if(voterIdImagePath.length>0){

            validationResults.add(true)
        }else{
            validationResults.add(false)
        }
        if(mgnregaIdImagePath.length>0){

            validationResults.add(true)
        }else{
            validationResults.add(false)
        }
        if(aadharIdImagePath.length>0){

            validationResults.add(true)
        }else{
            validationResults.add(false)
        }

        if(binding.etLocation.text.length<0){
            getTheLocation()
        }
        return !validationResults.contains(false);

    }

    private fun getTheLocation() {

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               requestThePermissions()
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        latitude=it.latitude
                        longitude=it.longitude
                        binding.etLocation.setText("${it.latitude},${it.longitude}")
                        addressFromLatLong=getAddressFromLatLong()
                    } ?: run {
                        Toast.makeText(
                            this@LabourUpdateOnline2Activity,
                            resources.getString(R.string.unable_to_retrive_location),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }

    private fun showAddFamilyDetailsDialog() {
        try {
            val dialog= Dialog(this@LabourUpdateOnline2Activity)
            dialog.setContentView(R.layout.layout_dialog_add_family_details)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()


            etFullName=dialog.findViewById<EditText>(R.id.etFullName)
            etDob=dialog.findViewById<AutoCompleteTextView>(R.id.etDob)
            actRelationship=dialog.findViewById<AutoCompleteTextView>(R.id.actRelationShip)
            actMaritalStatus=dialog.findViewById<AutoCompleteTextView>(R.id.actMaritalStatus)
            actGenderFamily=dialog.findViewById<AutoCompleteTextView>(R.id.actGenderFamily)
            btnSubmit=dialog.findViewById<Button>(R.id.btnSubmit)


            val relationshipAdapter = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, relationNames
            )
            actRelationship.setAdapter(relationshipAdapter)
            val maritalStatusAdapter = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, maritalStatusNames
            )
            actMaritalStatus.setAdapter(maritalStatusAdapter)

            actRelationship.setOnFocusChangeListener { abaad, asd ->
                actRelationship.showDropDown()
            }

            actRelationship.setOnClickListener {
                actRelationship.showDropDown()
            }
            actMaritalStatus.setOnClickListener {
                actMaritalStatus.showDropDown()
            }
            actMaritalStatus.setOnFocusChangeListener {abaad, asd ->
                actMaritalStatus.showDropDown()
            }
            val genderAdapter1 = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, genderNames
            )
            actGenderFamily.setAdapter(genderAdapter1)
            actGenderFamily.setOnFocusChangeListener { abaad, asd ->
                actGenderFamily.showDropDown()
            }
            actGenderFamily.setOnClickListener {
                actGenderFamily.showDropDown()
            }
            btnSubmit.setOnClickListener {
                if(validateFields())
                {
                    val familyMemberNew=com.sipl.egs2.model.apis.LaboureEditDetailsOnline.FamilyDetail(
                        full_name = etFullName.text.toString(),
                        date_of_birth = etDob.text.toString(),
                        relation = actRelationship.text.toString(),
                        maritalStatus = actMaritalStatus.text.toString(),
                        gender = actGenderFamily.text.toString(),
                        gender_id  =genderId,
                        married_status_id =maritalStatusId,
                        relationship_id = relationId,
                    )
                    familyDetailsList.add(familyMemberNew)
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }else{

                }
            }
            etDob.setOnClickListener {
                showDatePicker()
            }
            actGenderFamily.setOnItemClickListener { parent, view, position, id ->
                genderId=genderList[position].id.toString()
            }
            actRelationship.setOnItemClickListener { parent, view, position, id ->
                relationId=relationList[position].id.toString()
            }
            actMaritalStatus.setOnItemClickListener { parent, view, position, id ->
                maritalStatusId=maritalStatusList[position].id.toString()
            }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }

    }
    private fun validateFields():Boolean{

        val validationResults= mutableListOf<Boolean>()
        // Full Name
        if (MyValidator.isValidName(etFullName.text.toString())) {
            etFullName.error = null
            validationResults.add(true)
        } else {
            etFullName.error = resources.getString(R.string.full_name_required)
            validationResults.add(false)
        }
        // DOB
        if (etDob.text.toString().length > 0 && !etDob.text.isNullOrBlank()) {
            etDob.error = null
            validationResults.add(true)
        } else {
            etDob.error = resources.getString(R.string.select_date_of_birth)
            validationResults.add(false)
        }
        // Relationship
        if (actMaritalStatus.enoughToFilter()) {
            actMaritalStatus.error = null
            validationResults.add(true)
        } else {
            actMaritalStatus.error = resources.getString(R.string.select_marital_status)
            validationResults.add(false)
        }

        // Relationship
        if (actRelationship.enoughToFilter()) {
            actRelationship.error = null
            validationResults.add(true)
        } else {
            actRelationship.error = resources.getString(R.string.select_relationship)
            validationResults.add(false)
        }
        return !validationResults.contains(false);
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = formatDate(dayOfMonth, monthOfYear, year)
                etDob.setText(selectedDate)
            }, year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onDelete(position: Int) {
        familyDetailsList.removeAt(position)
        adapter.notifyDataSetChanged()
    }
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putSerializable("LabourInputData",labourInputData     )

    }
    private fun requestThePermissions() {

        try {
            PermissionX.init(this@LabourUpdateOnline2Activity)
                .permissions(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION ,android.Manifest.permission.CAMERA)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        //Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                        //val dashboardFragment=DashboardFragment();
                        //dashboardFragment.updateMarker()
                    } else {
                        Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@LabourUpdateOnline2Activity)
            builder.setTitle(resources.getString(R.string.exit))
                .setMessage(resources.getString(R.string.are_you_sure_you_want_to_exit_this_screen))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    // If "Yes" is clicked, exit the app
                    finish()
                }
                .setNegativeButton(resources.getString(R.string.no), null) // If "No" is clicked, do nothing
                .show()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showPhotoZoomDialog(uri:String){

        try {
            val dialog= Dialog(this@LabourUpdateOnline2Activity)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()

            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@LabourUpdateOnline2Activity)
                .load(uri)
                .into(photoView)

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }

    suspend fun uriStringToBitmap(context: Context, uriString: String, latlongtext: String,addressText: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uriString)
                    .submit()
                val bitmap = futureTarget.get()

                // Add text overlay to the bitmap
                val canvas = Canvas(bitmap)
                val paint = Paint().apply {
                    color = Color.RED
                    textSize = 50f // Text size in pixels
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                val currentDateTime = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = formatter.format(currentDateTime)
                val x = 50f // Adjust the x-coordinate as needed
                val y = bitmap.height.toFloat() - 50f // Adjust the y-coordinate as needed
                val xAddress = 50f // Adjust the x-coordinate as needed
                val yAddress = bitmap.height.toFloat() - 100f
                canvas.drawText(latlongtext, x, y, paint)
                val addressTextWidth = paint.measureText(addressText)
                val availableWidth = bitmap.width.toFloat() - xAddress // A
                if(addressTextWidth > availableWidth){
                    val (firstHalf, secondHalf) = splitStringByHalf(addressText)
                    canvas.drawText(firstHalf, xAddress, yAddress-50, paint)
                    canvas.drawText(secondHalf, xAddress, yAddress, paint)
                    canvas.drawText(formattedDateTime, xAddress, yAddress-100, paint)
                }else{
                    canvas.drawText(addressText, xAddress, yAddress, paint)
                    canvas.drawText(formattedDateTime, xAddress, yAddress-50, paint)
                }

                // Save the modified bitmap back to the same location
                saveBitmapToFile(context, bitmap, uri)

                uri // Return the URI of the modified bitmap
            } catch (e: Exception) {
                Log.d("mytag","Exception => "+e.message)
                Log.d("mytag","Exception => ${e.message}",e)
                e.printStackTrace()
                null
            }
        }
    }
    fun splitStringByHalf(input: String): Pair<String, String> {
        val length = input.length
        val halfLength = length / 2
        val firstHalf = input.substring(0, halfLength)
        val secondHalf = input.substring(halfLength)
        return Pair(firstHalf, secondHalf)
    }

    private suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it) }
            outputStream?.flush()
            outputStream?.close()
            val imageFile=bitmapToFile(context,bitmap)
            val compressedImageFile = imageFile?.let {
                Compressor.compress(context, it) {
                    format(Bitmap.CompressFormat.JPEG)
                    resolution(780,1360)
                    quality(100)
                    size(1_097_152) // 500 KB
                }
            }
            compressedImageFile?.let { compressedFile:File ->
                try {
                    val inputStream = FileInputStream(compressedFile)
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    inputStream.use { input ->
                        outputStream?.use { output ->
                            input.copyTo(output)
                        }
                    }

                } catch (e: IOException) {
                    // Handle exception
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("mytag","Exception => ${e.message}",e)
        }
    }


    fun bitmapToFile(context: Context, bitmap: Bitmap): File? {
        // Create a file in the cache directory
        val time=Calendar.getInstance().timeInMillis.toString()
        val file = File(context.cacheDir, time)

        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            fos.flush()
            fos.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("mytag","Exception => ${e.message}",e)
        }
        return null
    }


    private fun getAddressFromLatLong():String{
        try {
            val geocoder: Geocoder
            val addresses: List<Address>?
            geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(
                latitude, longitude,
                1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            var fullAddress=""
            if (addresses != null) {
                if(addresses.size>0){
                    fullAddress= addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    val city: String = addresses!![0].locality
                    val state: String = addresses!![0].adminArea
                    val country: String = addresses!![0].countryName
                    val postalCode: String = addresses!![0].postalCode
                    val knownName: String = addresses!![0].featureName

                    Log.d("mytag",fullAddress)
                    Log.d("mytag",city)
                    Log.d("mytag",state)
                    Log.d("mytag",country)
                    Log.d("mytag",postalCode)
                    Log.d("mytag",knownName)
                }
            }
            return fullAddress
        } catch (e: Exception) {
            return ""

        }

    }
    private  fun getDetailsFromServer(mgnregaCardId:String,labourIdx:String){
        Log.d("mytag","getDetailsFromServer")
        try {
            dialog.show()
            val apiService= ApiClient.create(this@LabourUpdateOnline2Activity)
            CoroutineScope(Dispatchers.IO).launch {
                val response=apiService.getLabourDetailsForUpdate2(mgnregaCardId,labourIdx)
                dialog.dismiss()
                Log.d("mytag","getDetailsFromServer")
                if(response.isSuccessful){
                    Log.d("mytag","getDetailsFromServer isSuccessful")
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        if(response.body()?.status.equals("true"))
                        {
                            Log.d("mytag","getDetailsFromServer isSuccessful true")
                            withContext(Dispatchers.Main) {
                                binding.etLocation.setText(list?.get(0)?.latitude+","+list?.get(0)?.longitude)
                                loadImageWithRetry(list?.get(0)?.aadhar_image!!,binding.ivAadhar)
                                loadImageWithRetry(list?.get(0)?.mgnrega_image!!,binding.ivMgnregaCard)
                                loadImageWithRetry(list?.get(0)?.voter_image!!,binding.ivVoterId)
                                loadImageWithRetry(list?.get(0)?.profile_image!!,binding.ivPhoto)
                                voterIdImagePath=list?.get(0)?.voter_image!!
                                photoImagePath=list?.get(0)?.profile_image!!
                                aadharIdImagePath=list?.get(0)?.aadhar_image!!
                                mgnregaIdImagePath=list?.get(0)?.mgnrega_image!!
                                labourId=list?.get(0)?.id.toString()!!
                                val gson= Gson()
                                val jsonList=gson.toJson(response.body()?.data?.get(0)?.family_details)
                                val familyList: ArrayList<com.sipl.egs2.model.apis.LaboureEditDetailsOnline.FamilyDetail> = gson.fromJson(jsonList, object : TypeToken<ArrayList<FamilyDetail>>() {}.type)
                                familyDetailsList=familyList
                                Log.d("mytag",""+familyDetailsList.size)
                                familyDetailsList= response.body()?.data?.get(0)?.family_details as ArrayList<com.sipl.egs2.model.apis.LaboureEditDetailsOnline.FamilyDetail>
                                 adapter= FamilyDetailsOnlineEditAdapter(familyDetailsList,this@LabourUpdateOnline2Activity)
                                binding.recyclerViewFamilyDetails.adapter=adapter
                                adapter.notifyDataSetChanged()
                            }
                        }else{
                            Log.d("mytag","getDetailsFromServer isSuccessful false")
                        }
                    }else {
                        runOnUiThread {
                            Toast.makeText(this@LabourUpdateOnline2Activity, "No records found", Toast.LENGTH_SHORT).show()
                        }

                    }
                } else{
                    runOnUiThread {
                        Toast.makeText(this@LabourUpdateOnline2Activity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        } catch (e: Exception) {
            Log.d("mytag","getDetailsFromServer : Exception => "+e.message)
            dialog.dismiss()
            e.printStackTrace()
        }
    }
    suspend fun uriToFile(context: Context, uri: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Don't cache to avoid reading from cache
                    .skipMemoryCache(false) // Skip memory cache
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(requestOptions)
                    .submit()
                    .get()
                val time=Calendar.getInstance().timeInMillis.toString()
                // Create a temporary file to store the bitmap
                val file = File(context.cacheDir, "$time.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream)
                outputStream.flush()
                outputStream.close()

                file // Return the temporary file
            } catch (e: Exception) {
                Log.d("mytag", "Exception uriToFile: ${e.message}")
                null // Return null if there's an error
            }
        }
    }
    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        Log.d("mytag",""+fileInfo.fileUri)
        val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
        return file?.let {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
        }
    }
    private suspend fun uploadLabourOnlineImageOptional(){
        runOnUiThread {
            dialog.show()
        }

        val apiService = ApiClient.create(this@LabourUpdateOnline2Activity)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val familyDetails= Gson().toJson(familyDetailsList).toString()
                val filesList = mutableListOf<MultipartBody.Part>()
                if(aadharIdImagePathNew.length>0){
                    val aadharCardImageFile =
                        createFilePart(FileInfo("aadhar_image", aadharIdImagePathNew))
                    filesList.add(aadharCardImageFile!!)
                }
                if(voterIdImagePathNew.length>0){
                    val voterImageFile =
                        createFilePart(FileInfo("voter_image", voterIdImagePathNew))
                    filesList.add(voterImageFile!!)
                }
                if(photoImagePathNew.length>0){
                    val photImageFile =
                        createFilePart(FileInfo("profile_image", photoImagePathNew))
                    filesList.add(photImageFile!!)
                }
                if(mgnregaIdImagePathNew.length>0){
                    val mgnregaIdCardImageFile =
                        createFilePart(FileInfo("mgnrega_image", mgnregaIdImagePathNew))
                    filesList.add(mgnregaIdCardImageFile!!)
                }
                var response: Response<MastersModel>? =null
                if(filesList.size>0){
                     response= apiService.updateLabourFormTwoImageOptionalFileList(
                        latitude=latitude.toString(),
                        longitude = longitude.toString(),
                        family = familyDetails,
                        id = labourId,
                        files =filesList
                    )
                }else{
                     response= apiService.updateLabourFormTwoWithoutImage(
                        latitude=latitude.toString(),
                        longitude = longitude.toString(),
                        family = familyDetails,
                        id = labourId,
                    )
                }

                if(response.isSuccessful){
                    if(response.body()?.status.equals("true")){

                        withContext(Dispatchers.Main){
                            Toast.makeText(this@LabourUpdateOnline2Activity,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                            val intent= Intent(this@LabourUpdateOnline2Activity, ReportsActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                    }else{
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@LabourUpdateOnline2Activity,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d("mytag",""+response.body()?.message)
                    Log.d("mytag",""+response.body()?.status)
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { dialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.failed_updating_labour_response),
                        Toast.LENGTH_SHORT).show()
                }
                Log.d("mytag","uploadLabourOnline "+e.message)
            }
        }
    }
    fun startCameraActivity(requestCode: Int) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("requestCode", requestCode)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val requestCode = result.data?.getIntExtra("requestCode", -1)
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val capturedImageUri = result.data?.getParcelableExtra<Uri>("capturedImageUri")
                val requestCode = result.data?.getIntExtra("requestCode", -1)
                if (capturedImageUri != null && requestCode != -1) {

                    when (requestCode) {
                        REQUEST_CODE_PHOTO -> {
                            Glide.with(this@LabourUpdateOnline2Activity).load(capturedImageUri).override(200,200).into(binding.ivPhoto)
                            photoImagePath= capturedImageUri.toString()
                            photoImagePathNew=capturedImageUri.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@LabourUpdateOnline2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                photoImagePathNew=uri.toString()
                                withContext(Dispatchers.Main){
                                }
                            }
                        }
                        REQUEST_CODE_VOTER_ID -> {

                            Glide.with(this@LabourUpdateOnline2Activity).load(capturedImageUri).override(200,200).into(binding.ivVoterId)
                            voterIdImagePath= capturedImageUri.toString()
                            voterIdImagePathNew=capturedImageUri.toString()

                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@LabourUpdateOnline2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                voterIdImagePathNew=uri.toString()
                                withContext(Dispatchers.Main){
                                }
                            }
                        }
                        REQUEST_CODE_AADHAR_CARD -> {

                            Glide.with(this@LabourUpdateOnline2Activity).load(capturedImageUri)
                                .override(200, 200).into(binding.ivAadhar)
                            aadharIdImagePathNew = capturedImageUri.toString();
                            aadharIdImagePath = capturedImageUri.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri = uriStringToBitmap(
                                    this@LabourUpdateOnline2Activity,
                                    capturedImageUri.toString(),
                                    binding.etLocation.text.toString(),
                                    addressFromLatLong
                                )
                                withContext(Dispatchers.Main) {
                                    aadharIdImagePathNew = uri.toString()
                                }

                            }
                        }
                        REQUEST_CODE_MGNREGA_CARD -> {
                            Glide.with(this@LabourUpdateOnline2Activity).load(capturedImageUri).override(200,200).into(binding.ivMgnregaCard)
                            mgnregaIdImagePath= capturedImageUri.toString()
                            mgnregaIdImagePathNew=capturedImageUri.toString();
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@LabourUpdateOnline2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                mgnregaIdImagePathNew=uri.toString();
                                try {
                                    getAddressFromLatLong()
                                } finally {

                                }
                                withContext(Dispatchers.Main){
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.unknown_request_code),Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == Activity.RESULT_CANCELED) {
                val requestCode = result.data?.getIntExtra("requestCode", -1)
                if (requestCode != -1) {
                    Toast.makeText(this@LabourUpdateOnline2Activity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","LabourUpdateOnline2Activity: ",e)
            e.printStackTrace()
        }
    }


}
