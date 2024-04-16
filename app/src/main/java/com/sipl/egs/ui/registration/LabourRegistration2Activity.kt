package com.sipl.egs.ui.registration

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.sipl.egs.MainActivity
import com.sipl.egs.R
import com.sipl.egs.adapters.FamilyDetailsAdapter
import com.sipl.egs.camera.CameraActivity
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.GenderDao
import com.sipl.egs.database.dao.LabourDao
import com.sipl.egs.database.dao.MaritalStatusDao
import com.sipl.egs.database.dao.RelationDao
import com.sipl.egs.database.entity.Gender
import com.sipl.egs.database.entity.Labour
import com.sipl.egs.database.entity.MaritalStatus
import com.sipl.egs.database.entity.Relation
import com.sipl.egs.databinding.ActivityLabourRegistration2Binding
import com.sipl.egs.interfaces.OnDeleteListener
import com.sipl.egs.model.FamilyDetails
import com.sipl.egs.utils.LabourInputData
import com.sipl.egs.utils.MyValidator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LabourRegistration2Activity : AppCompatActivity(),OnDeleteListener {
    private lateinit var binding: ActivityLabourRegistration2Binding
    lateinit var  etDob:AutoCompleteTextView
    lateinit var  etFullName:EditText
    lateinit var  actMaritalStatus:AutoCompleteTextView
    lateinit var  actRelationship:AutoCompleteTextView
    lateinit var  actGenderFamily:AutoCompleteTextView
    lateinit var  btnSubmit:Button
    var validationResults = mutableListOf<Boolean>()
    var familyDetailsList=ArrayList<FamilyDetails>()
    lateinit var adapter: FamilyDetailsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase
    private lateinit var LabourDao: LabourDao
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
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private  var addressFromLatLong:String=""
    private  var isInternetAvailable=false
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourRegistration2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.registration_step_2)
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        registrationViewModel.dataObject.observe(this) { labourData ->
            Log.d("mytag", "labourData.mobile")
            Log.d("mytag", labourData.mobile)
        }
        database= AppDatabase.getDatabase(this)
        LabourDao=database.labourDao()
        genderDao=database.genderDao()
        relationDao=database.relationDao()
        maritalStatusDao=database.martialStatusDao()
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
        requestThePermissions()
        CoroutineScope(Dispatchers.IO).launch{
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
        binding.layoutAdd.setOnClickListener {
            showAddFamilyDetailsDialog()
        }

        labourInputData = intent.getSerializableExtra("LabourInputData") as LabourInputData
        Log.d("mytag",registrationViewModel.fullName)
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewFamilyDetails.layoutManager=layoutManager;
        adapter= FamilyDetailsAdapter(familyDetailsList,this)
        binding.recyclerViewFamilyDetails.adapter=adapter
        voterIdImagePath=""
        photoImagePath=""
        aadharIdImagePath=""
        mgnregaIdImagePath=""

        binding.btnSubmit.setOnClickListener {
            if(validateFormFields()){
                val familyDetails=Gson().toJson(familyDetailsList).toString()
                val labour = Labour(
                    fullName = labourInputData.fullName,
                    gender = labourInputData.gender,
                    dob = labourInputData.dateOfBirth,
                    district = labourInputData.district,
                    taluka = labourInputData.taluka,
                    village = labourInputData.village,
                    mobile = labourInputData.mobile,
                    landline = labourInputData.landline,
                    mgnregaId = labourInputData.idCard,
                    familyDetails = familyDetails,
                    location = binding.etLocation.getText().toString(),
                    aadharImage = aadharIdImagePath,
                    mgnregaIdImage = mgnregaIdImagePath,
                    voterIdImage = voterIdImagePath,
                    photo = photoImagePath,
                    isSynced = false,
                    skilled = false,
                    latitude = latitude.toString(),
                    longitude = longitude.toString(),
                    skill = labourInputData.skill)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val rows=LabourDao.insertLabour(labour)
                            if(rows>0){
                                runOnUiThread {
                                    val toast=Toast.makeText(this@LabourRegistration2Activity,
                                        getString(
                                            R.string.labour_added_successfully
                                        ),Toast.LENGTH_SHORT)
                                    toast.show()
                                }
                                val intent= Intent(this@LabourRegistration2Activity,MainActivity::class.java)
                                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }else{
                                runOnUiThread {
                                    val toast=Toast.makeText(this@LabourRegistration2Activity,
                                        getString(
                                            R.string.labour_not_added_please_try_again
                                        ),Toast.LENGTH_SHORT)
                                    toast.show()
                                }
                            }
                            Log.d("mytag","Rows Inserted : $rows")
                        } catch (e: Exception) {
                            Log.d("mytag","Exception Inserted : ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }

        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getTheLocation()
        if (!isLocationEnabled()) {
            // If not enabled, show dialog to enable it
            showEnableLocationDialog()
        } else {
            // Request location updates
            //showProgressDialog()
            requestLocationUpdates()
        }

        try {
            cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    Log.d("mytag", "Success => ${uriMap.values}")

                    // Retrieve URI for Aadhar Card
                    val uriAadhar = uriMap[REQUEST_CODE_AADHAR_CARD]
                    if (uriAadhar != null) {
                        Log.d("mytag", "URI for Aadhar Card: $uriAadhar")
                        //binding.ivAadhar.setImageURI(uriAadhar)
                        Glide.with(this@LabourRegistration2Activity).load(uriAadhar).override(200,200).into(binding.ivAadhar)
                        aadharIdImagePath= uriAadhar.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,uriAadhar.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                          withContext(Dispatchers.Main){
                             // binding.ivPhoto.setImageBitmap(bitmap)
                          }

                        }

                    } else {
                        Log.d("mytag", "URI for Aadhar Card is null")
                    }
                    // Retrieve URI for MGNREGA Card
                    val uriMgnregaCard = uriMap[REQUEST_CODE_MGNREGA_CARD]
                    if (uriMgnregaCard != null) {
                        Log.d("mytag", "URI for MGNREGA Card: $uriMgnregaCard")
                        //binding.ivMgnregaCard.setImageURI(uriMgnregaCard)
                        Glide.with(this@LabourRegistration2Activity).load(uriMgnregaCard).override(200,200).into(binding.ivMgnregaCard)
                        mgnregaIdImagePath= uriMgnregaCard.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,uriMgnregaCard.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }
                    } else {
                        Log.d("mytag", "URI for MGNREGA Card is null")
                    }

                    // Retrieve URI for Photo
                    val uriPhoto = uriMap[REQUEST_CODE_PHOTO]
                    if (uriPhoto != null) {
                        Log.d("mytag", "URI for Photo: $uriPhoto")
                        //binding.ivPhoto.setImageURI(uriPhoto)
                        Glide.with(this@LabourRegistration2Activity).load(uriPhoto).override(200,200).into(binding.ivPhoto)
                        photoImagePath= uriPhoto.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,uriPhoto.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }
                    } else {
                        Log.d("mytag", "URI for Photo is null")
                    }

                    // Retrieve URI for Voter ID
                    val uriVoterId = uriMap[REQUEST_CODE_VOTER_ID]
                    if (uriVoterId != null) {
                        Log.d("mytag", "URI for Voter ID: $uriVoterId")
                        //binding.ivVoterId.setImageURI(uriVoterId)
                        Glide.with(this@LabourRegistration2Activity).load(uriVoterId).override(200,200).into(binding.ivVoterId)
                        voterIdImagePath= uriVoterId.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,uriVoterId.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }
                        }

                    } else {
                        Log.d("mytag", "URI for Voter ID is null")
                    }
                } else {
                    // Image capture failed or was canceled
                    Log.d("mytag", "Failed")
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","cameraLauncher=>registerForActivityException=>"+e.message)
            e.printStackTrace()
        }
        binding.layoutAadharCard.setOnClickListener {
            //captureImage(REQUEST_CODE_AADHAR_CARD)
            startCameraActivity(REQUEST_CODE_AADHAR_CARD)
        }
        binding.layoutPhoto.setOnClickListener {
            //captureImage(REQUEST_CODE_PHOTO)
            startCameraActivity(REQUEST_CODE_PHOTO)
        }
        binding.layoutVoterId.setOnClickListener {
           // captureImage(REQUEST_CODE_VOTER_ID)
            startCameraActivity(REQUEST_CODE_VOTER_ID)
        }
        binding.layoutMgnregaCard.setOnClickListener {
            //captureImage(REQUEST_CODE_MGNREGA_CARD)
            startCameraActivity(REQUEST_CODE_MGNREGA_CARD)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@LabourRegistration2Activity)
                builder.setTitle("Exit")
                    .setMessage("Are you sure you want to exit this screen?")
                    .setPositiveButton("Yes") { _, _ ->
                        // If "Yes" is clicked, exit the app
                        finish()
                    }
                    .setNegativeButton("No", null) // If "No" is clicked, do nothing
                    .show()

            }
        })
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    suspend fun uriStringToBitmap(context: Context, uriString: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .submit()
                futureTarget.get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun uriStringToBitmap(context: Context, uriString: String, text: String,addressText: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uri)
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
                canvas.drawText(text, x, y, paint)
                canvas.drawText(addressText, xAddress, yAddress, paint)
                canvas.drawText(formattedDateTime, xAddress, yAddress-50, paint)

                // Save the modified bitmap back to the same location
                saveBitmapToFile(context, bitmap, uri)

                uri // Return the URI of the modified bitmap
            } catch (e: Exception) {
                Log.d("mytag","Exception => "+e.message)
                e.printStackTrace()
                null
            }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 10, it) }
            outputStream?.flush()
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@LabourRegistration2Activity)
            builder.setTitle("Exit Confirmation")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ ->
                    // If "Yes" is clicked, exit the app
                    finish()
                }
                .setNegativeButton("No", null) // If "No" is clicked, do nothing
                .show()
        }
        return super.onOptionsItemSelected(item)
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

        return !validationResults.contains(false);

    }

    private val uriMap = mutableMapOf<Int, Uri>()
    private fun captureImage(requestCode: Int) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "image_$timestamp"
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            val uriFolder = Uri.parse(mediaStorageDir.absolutePath)
            val myAppFolder = File(uriFolder.toString())

            // Create the folder if it doesn't exist
            if (!myAppFolder.exists()) {
                myAppFolder.mkdirs()
            }

            // Create the file for the image
            val outputFile = File.createTempFile(fileName, ".jpg", myAppFolder)
            val uri = FileProvider.getUriForFile(this, "com.sipl.egs.provider", outputFile)

            val file=File(myAppFolder,fileName+".jpg")
            if(file!=null){

                Log.d("mytag","filecreated")

            }else{
                Log.d("mytag","Failed to create file")
            }

            // Store the URI in the map with the corresponding request code
            uriMap[requestCode] = uri

            // Launch the camera to capture the image
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.d("mytag","captureImage=>Exception=>"+e.message)
            e.printStackTrace()
        }
    }
    private fun addTextOverlay(file: File, text: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                Log.d("mytag", "addTextOverlay=>Exception=>Bitmap is null")
                return
            }

            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isAntiAlias = true
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val x = (bitmap.width - bounds.width()) / 2f
            val y = (bitmap.height - bounds.height()) / 2f
            canvas.drawText(text, x, y, paint)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("mytag", "Text overlay added successfully")
        } catch (e: Exception) {
            Log.d("mytag", "addTextOverlay=>Exception=>" + e.message)
            e.printStackTrace()
        }
    }


    private fun getTheLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
                    latitude=it.latitude
                    longitude=it.longitude
                    binding.etLocation.setText("${it.latitude},${it.longitude}")
                    addressFromLatLong=getAddressFromLatLong()
                } ?: run {
                    Toast.makeText(
                        this@LabourRegistration2Activity,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun requestLocationUpdates() {
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
                        this@LabourRegistration2Activity,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Location services are disabled. Do you want to enable them?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                // Handle the case when the user refuses to enable location services
                Toast.makeText(
                    this@LabourRegistration2Activity,
                    "Unable to retrieve location without enabling location services",
                    Toast.LENGTH_SHORT
                ).show()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun showAddFamilyDetailsDialog() {
        val dialog=Dialog(this@LabourRegistration2Activity)
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
                try {
                    val familyMember=FamilyDetails(
                        fullName = etFullName.text.toString(),
                        dob = etDob.text.toString(),
                        relationship = actRelationship.text.toString(),
                        maritalStatus = actMaritalStatus.text.toString(),
                        gender = actGenderFamily.text.toString(),
                        genderId=genderId,
                        maritalStatusId=maritalStatusId,
                        relationId = relationId
                    )
                    familyDetailsList.add(familyMember)
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.d("mytag",""+e.message)
                    e.printStackTrace()
                }

            }else{
                Log.d("mytag","Validation Error")
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
    }
    private fun validateFields():Boolean{
        var validationResults= mutableListOf<Boolean>()
        // Full Name
        if (MyValidator.isValidName(etFullName.text.toString())) {
            etFullName.error = null
            validationResults.add(true)
            Log.d("mytag","Validation isValidName "+true)
        } else {
            etFullName.error = resources.getString(R.string.full_name_required)
            validationResults.add(false)
            Log.d("mytag","Validation isValidName "+false)
        }
        // DOB
        if (etDob.text.toString().length > 0 && !etDob.text.isNullOrBlank()) {
            etDob.error = null
            validationResults.add(true)
            Log.d("mytag","Validation dob "+true)
        } else {
            etDob.error = resources.getString(R.string.select_date_of_birth)
            validationResults.add(false)
            Log.d("mytag","Validation dob "+false)
        }
        // Relationship
        if (actMaritalStatus.enoughToFilter()) {
            actMaritalStatus.error = null
            validationResults.add(true)
            Log.d("mytag","Validation actMaritalStatus "+true)
        } else {
            actMaritalStatus.error = resources.getString(R.string.select_marital_status)
            validationResults.add(false)
            Log.d("mytag","Validation actMaritalStatus "+false)
        }

        // Relationship
        if (actRelationship.enoughToFilter()) {
            actRelationship.error = null
            validationResults.add(true)
            Log.d("mytag","Validation actRelationship "+true)
        } else {
            actRelationship.error = resources.getString(R.string.select_relationship)
            validationResults.add(false)
            Log.d("mytag","Validation actRelationship "+false)
        }
        // Relationship

        if (actGenderFamily.enoughToFilter()) {
            actGenderFamily.error = null
            validationResults.add(true)
            Log.d("mytag","Validation actGenderFamily "+true)
        } else {
            actGenderFamily.error = resources.getString(R.string.select_gender)
            validationResults.add(false)
            Log.d("mytag","Validation actGenderFamily "+false)
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

        PermissionX.init(this@LabourRegistration2Activity)
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

    fun startCameraActivity(requestCode: Int) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("requestCode", requestCode)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val requestCode = result.data?.getIntExtra("requestCode", -1)
        if (result.resultCode == Activity.RESULT_OK) {
            val capturedImageUri = result.data?.getParcelableExtra<Uri>("capturedImageUri")
            val requestCode = result.data?.getIntExtra("requestCode", -1)
            if (capturedImageUri != null && requestCode != -1) {

                when (requestCode) {
                    REQUEST_CODE_PHOTO -> {
                        Glide.with(this@LabourRegistration2Activity).load(capturedImageUri).override(200,200).into(binding.ivPhoto)
                        photoImagePath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }

                    }
                    REQUEST_CODE_VOTER_ID -> {

                        Glide.with(this@LabourRegistration2Activity).load(capturedImageUri).override(200,200).into(binding.ivVoterId)
                        voterIdImagePath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistration2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }
                        }
                    }
                    REQUEST_CODE_AADHAR_CARD -> {
                            Glide.with(this@LabourRegistration2Activity).load(capturedImageUri).override(200,200).into(binding.ivAadhar)
                            aadharIdImagePath= capturedImageUri.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@LabourRegistration2Activity,capturedImageUri.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                withContext(Dispatchers.Main){
                                }
                            }
                    }
                    REQUEST_CODE_MGNREGA_CARD -> {
                        Glide.with(this@LabourRegistration2Activity).load(capturedImageUri).override(200,200).into(binding.ivMgnregaCard)
                        mgnregaIdImagePath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri = uriStringToBitmap(
                                this@LabourRegistration2Activity,
                                capturedImageUri.toString(),
                                binding.etLocation.text.toString(),
                                addressFromLatLong
                            )
                            withContext(Dispatchers.Main) {
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(this@LabourRegistration2Activity,resources.getString(R.string.unknown_request_code),Toast.LENGTH_SHORT).show()
                    }
                }

            } else {

                Toast.makeText(this@LabourRegistration2Activity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Activity.RESULT_CANCELED) {
            val requestCode = result.data?.getIntExtra("requestCode", -1)
            if (requestCode != -1) {
                Toast.makeText(this@LabourRegistration2Activity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
            }
        }
    }

}
