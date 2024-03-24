package com.sumagoinfotech.digicopy.ui.activities.registration

import android.Manifest
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
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.GenderDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.MaritalStatusDao
import com.sumagoinfotech.digicopy.database.dao.RelationDao
import com.sumagoinfotech.digicopy.database.entity.Gender
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.entity.MaritalStatus
import com.sumagoinfotech.digicopy.database.entity.Relation
import com.sumagoinfotech.digicopy.databinding.ActivityLabourRegistrationEdit2Binding
import com.sumagoinfotech.digicopy.interfaces.OnDeleteListener
import com.sumagoinfotech.digicopy.model.FamilyDetails
import com.sumagoinfotech.digicopy.ui.activities.SyncLabourDataActivity
import com.sumagoinfotech.digicopy.adapters.FamilyDetailsAdapter
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.MyValidator
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class LabourRegistrationEdit2 : AppCompatActivity(),OnDeleteListener {
    private lateinit var binding: ActivityLabourRegistrationEdit2Binding
    lateinit var  etDob: AutoCompleteTextView
    lateinit var  etFullName: EditText
    lateinit var  actMaritalStatus: AutoCompleteTextView
    lateinit var  actRelationship: AutoCompleteTextView
    lateinit var  actGenderFamily:AutoCompleteTextView
    lateinit var  btnSubmit: Button
    var validationResults = mutableListOf<Boolean>()
    var familyDetailsList=ArrayList<FamilyDetails>()
    lateinit var adapter: FamilyDetailsAdapter
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
    lateinit var labour:Labour
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLabourRegistrationEdit2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.update_details_step_2)
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
        var labourId=intent.extras?.getString("id")
        CoroutineScope(Dispatchers.IO).launch {
            labour=labourDao.getLabourById(Integer.parseInt(labourId))
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
            runOnUiThread {
                initializeFields();
            }

        }
        requestThePermissions()
        labourInputData = intent.getSerializableExtra("LabourInputData") as LabourInputData
        Log.d("mytag",registrationViewModel.fullName)
        val layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        binding.recyclerViewFamilyDetails.layoutManager=layoutManager;
        adapter= FamilyDetailsAdapter(familyDetailsList,this)
        binding.recyclerViewFamilyDetails.adapter=adapter
        binding.btnUpdate.setOnClickListener {
            if(validateFormFields())
            {
                val familyDetails= Gson().toJson(familyDetailsList).toString()
                labour.location=binding.etLocation.text.toString()
                labour.familyDetails=familyDetails
                labour.aadharImage=aadharIdImagePath
                labour.voterIdImage=voterIdImagePath
                labour.photo=photoImagePath
                labour.mgnregaIdImage=mgnregaIdImagePath
                labour.latitude=latitude.toString()
                labour.longitude=longitude.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val rows=labourDao.updateLabour(labour)
                        if(rows>0){
                            runOnUiThread {
                                val toast= Toast.makeText(this@LabourRegistrationEdit2,"Labour updated successfully",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                            }
                            val intent= Intent(this@LabourRegistrationEdit2, SyncLabourDataActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }else{
                            runOnUiThread {
                                val toast= Toast.makeText(this@LabourRegistrationEdit2,"Labour not updated please try again",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                            }
                        }
                        Log.d("mytag","Rows updated : $rows")
                    } catch (e: Exception) {
                        Log.d("mytag","Exception on update : ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{
                Toast.makeText(this@LabourRegistrationEdit2,resources.getString(R.string.select_all_documents),
                    Toast.LENGTH_SHORT).show()
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getTheLocation()
        try {
            cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    Log.d("myatg", "Success => ${uriMap.values}")
                    // Retrieve URI for Aadhar Card
                    val uriAadhar = uriMap[REQUEST_CODE_AADHAR_CARD]
                    if (uriAadhar != null) {
                        Log.d("myatg", "URI for Aadhar Card: $uriAadhar")
                        binding.ivAadhar.setImageURI(uriAadhar)
                        aadharIdImagePath= uriAadhar.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistrationEdit2,uriAadhar.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }
                    } else {
                        Log.d("myatg", "URI for Aadhar Card is null")
                    }
                    // Retrieve URI for MGNREGA Card
                    val uriMgnregaCard = uriMap[REQUEST_CODE_MGNREGA_CARD]
                    if (uriMgnregaCard != null) {
                        Log.d("myatg", "URI for MGNREGA Card: $uriMgnregaCard")
                        binding.ivMgnregaCard.setImageURI(uriMgnregaCard)
                        mgnregaIdImagePath= uriMgnregaCard.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistrationEdit2,uriMgnregaCard.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            try {
                                getAddressFromLatLong()
                            } finally {

                            }
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }
                    } else {
                        Log.d("myatg", "URI for MGNREGA Card is null")
                    }
                    // Retrieve URI for Photo
                    val uriPhoto = uriMap[REQUEST_CODE_PHOTO]
                    if (uriPhoto != null) {
                        Log.d("myatg", "URI for Photo: $uriPhoto")
                        binding.ivPhoto.setImageURI(uriPhoto)
                        photoImagePath= uriPhoto.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistrationEdit2,uriPhoto.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }
                    } else {
                        Log.d("myatg", "URI for Photo is null")
                    }
                    // Retrieve URI for Voter ID
                    val uriVoterId = uriMap[REQUEST_CODE_VOTER_ID]
                    if (uriVoterId != null) {
                        Log.d("myatg", "URI for Voter ID: $uriVoterId")
                        binding.ivVoterId.setImageURI(uriVoterId)
                        voterIdImagePath= uriVoterId.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri=uriStringToBitmap(this@LabourRegistrationEdit2,uriVoterId.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                            withContext(Dispatchers.Main){
                                // binding.ivPhoto.setImageBitmap(bitmap)
                            }

                        }

                    } else {
                        Log.d("myatg", "URI for Voter ID is null")
                    }
                } else {
                    // Image capture failed or was canceled
                    Log.d("myatg", "Failed")
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","cameraLauncher=>registerForActivityException=>"+e.message)
            e.printStackTrace()
        }
        binding.ivChangeAadhar.setOnClickListener {
            captureImage(REQUEST_CODE_AADHAR_CARD)
        }
        binding.ivChangePhoto.setOnClickListener {
            captureImage(REQUEST_CODE_PHOTO)
        }
        binding.ivChangeVoterId.setOnClickListener {
            captureImage(REQUEST_CODE_VOTER_ID)
        }
        binding.ivChangeMgnregaCard.setOnClickListener {
            captureImage(REQUEST_CODE_MGNREGA_CARD)
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
    }
    private fun initializeFields() {
        Log.d("mytag","initializeFields "+labour.familyDetails)

        binding.etLocation.setText(labour.location)
        loadWithGlideFromUri(labour.aadharImage,binding.ivAadhar)
        loadWithGlideFromUri(labour.mgnregaIdImage,binding.ivMgnregaCard)
        loadWithGlideFromUri(labour.voterIdImage,binding.ivVoterId)
        loadWithGlideFromUri(labour.photo,binding.ivPhoto)
        voterIdImagePath=labour.voterIdImage
        photoImagePath=labour.photo
        aadharIdImagePath=labour.aadharImage
        mgnregaIdImagePath=labour.mgnregaIdImage
        val gson=Gson()
        val familyList: ArrayList<FamilyDetails> = gson.fromJson(labour.familyDetails, object : TypeToken<ArrayList<FamilyDetails>>() {}.type)
        familyDetailsList=familyList
        adapter= FamilyDetailsAdapter(familyDetailsList,this)
        binding.recyclerViewFamilyDetails.adapter=adapter
        adapter.notifyDataSetChanged()
        Log.d("mytag",labour.familyDetails)
        Log.d("mytag",""+familyList.size)
    }
    private fun loadWithGlideFromUri(uri: String, imageView: ImageView) {
        Glide.with(this@LabourRegistrationEdit2)
            .load(uri)
            .into(imageView)
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
            val uri = FileProvider.getUriForFile(this, "com.sumagoinfotech.digicopy.provider", outputFile)

            // Store the URI in the map with the corresponding request code
            uriMap[requestCode] = uri

            // Launch the camera to capture the image
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.d("mytag","captureImage=>Exception=>"+e.message)
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
                        this@LabourRegistrationEdit2,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showAddFamilyDetailsDialog() {
        val dialog= Dialog(this@LabourRegistrationEdit2)
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

        PermissionX.init(this@LabourRegistrationEdit2)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showPhotoZoomDialog(uri:String){

        val dialog= Dialog(this@LabourRegistrationEdit2)
        dialog.setContentView(R.layout.layout_zoom_image)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()

        val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
        val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
        Glide.with(this@LabourRegistrationEdit2)
            .load(uri)
            .into(photoView)

        ivClose.setOnClickListener {
            dialog.dismiss()
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

}
