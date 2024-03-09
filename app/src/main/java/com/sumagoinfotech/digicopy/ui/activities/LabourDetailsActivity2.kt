package com.sumagoinfotech.digicopy.ui.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.User
import com.sumagoinfotech.digicopy.database.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetails2Binding
import com.sumagoinfotech.digicopy.interfaces.OnDeleteListener
import com.sumagoinfotech.digicopy.model.FamilyDetails
import com.sumagoinfotech.digicopy.ui.adapters.FamilyDetailsAdapter
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.LabourInputDataObject
import com.sumagoinfotech.digicopy.utils.MyValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LabourDetailsActivity2 : AppCompatActivity(),OnDeleteListener {
    private lateinit var binding:ActivityLabourDetails2Binding
    lateinit var  etDob:AutoCompleteTextView
    lateinit var  etFullName:EditText
    lateinit var  actMaritalStatus:AutoCompleteTextView
    lateinit var  actRelationship:AutoCompleteTextView
    lateinit var  btnSubmit:Button
    var validationResults = mutableListOf<Boolean>()
    var familyDetailsList=ArrayList<FamilyDetails>()
    lateinit var adapter:FamilyDetailsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private val REQUEST_CODE_AADHAR_CARD = 100
    private  val REQUEST_CODE_PHOTO = 200
    private  val REQUEST_CODE_VOTER_ID = 300
    private  val REQUEST_CODE_MGNREGA_CARD = 400
    private lateinit var voterIdImagePath:String
    private lateinit var aadharIdImagePath:String
    private lateinit var photoImagePath:String
    private lateinit var mgnregaIdImagePath:String

    private lateinit var labourInputData: LabourInputData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutAdd.setOnClickListener {
            showAddFamilyDetailsDialog()
        }
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        labourInputData = intent.getSerializableExtra("LabourInputData") as LabourInputData
        Log.d("mytag",labourInputData.fullName)
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewFamilyDetails.layoutManager=layoutManager;
        adapter=FamilyDetailsAdapter(familyDetailsList,this)
        binding.recyclerViewFamilyDetails.adapter=adapter
        voterIdImagePath=""
        photoImagePath=""
        aadharIdImagePath=""
        mgnregaIdImagePath=""
        binding.btnSubmit.setOnClickListener {
            if(validateFormFields()){
                val familyDetails=Gson().toJson(familyDetailsList).toString()
                val user = User(
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
                    isSynced = false)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val rows=userDao.insertUser(user)
                        if(rows>0){

                            runOnUiThread {
                                val toast=Toast.makeText(this@LabourDetailsActivity2,"Labour record added successfully",Toast.LENGTH_SHORT)
                                toast.show()
                            }

                            val intent= Intent(this@LabourDetailsActivity2,MainActivity::class.java)
                            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }else{
                            runOnUiThread {
                                val toast=Toast.makeText(this@LabourDetailsActivity2,"Something went wrong",Toast.LENGTH_SHORT)
                                toast.show()
                            }
                        }
                        Log.d("mytag","Rows Inserted : $rows")
                    } catch (e: Exception) {
                        Log.d("mytag","Exception Inserted : ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{
                Toast.makeText(this@LabourDetailsActivity2,resources.getString(R.string.select_all_fields),Toast.LENGTH_SHORT).show()
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
                    } else {
                        Log.d("myatg", "URI for Aadhar Card is null")
                    }

                    // Retrieve URI for MGNREGA Card
                    val uriMgnregaCard = uriMap[REQUEST_CODE_MGNREGA_CARD]
                    if (uriMgnregaCard != null) {
                        Log.d("myatg", "URI for MGNREGA Card: $uriMgnregaCard")
                        binding.ivMgnregaCard.setImageURI(uriMgnregaCard)
                        mgnregaIdImagePath= uriMgnregaCard.toString()
                    } else {
                        Log.d("myatg", "URI for MGNREGA Card is null")
                    }

                    // Retrieve URI for Photo
                    val uriPhoto = uriMap[REQUEST_CODE_PHOTO]
                    if (uriPhoto != null) {
                        Log.d("myatg", "URI for Photo: $uriPhoto")
                        binding.ivPhoto.setImageURI(uriPhoto)
                        photoImagePath= uriPhoto.toString()
                    } else {
                        Log.d("myatg", "URI for Photo is null")
                    }

                    // Retrieve URI for Voter ID
                    val uriVoterId = uriMap[REQUEST_CODE_VOTER_ID]
                    if (uriVoterId != null) {
                        Log.d("myatg", "URI for Voter ID: $uriVoterId")
                        binding.ivVoterId.setImageURI(uriVoterId)
                        voterIdImagePath= uriVoterId.toString()
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
        binding.layoutAadharCard.setOnClickListener {
            captureImage(REQUEST_CODE_AADHAR_CARD)
        }
        binding.layoutPhoto.setOnClickListener {
            captureImage(REQUEST_CODE_PHOTO)
        }
        binding.layoutVoterId.setOnClickListener {
            captureImage(REQUEST_CODE_VOTER_ID)
        }
        binding.layoutMgnregaCard.setOnClickListener {
            captureImage(REQUEST_CODE_MGNREGA_CARD)
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
                    binding.etLocation.setText("${it.latitude},${it.longitude}")
                } ?: run {
                    Toast.makeText(
                        this@LabourDetailsActivity2,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showAddFamilyDetailsDialog() {
        val dialog=Dialog(this@LabourDetailsActivity2)
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
         btnSubmit=dialog.findViewById<Button>(R.id.btnSubmit)


        var relationshipList = listOf(
            "Son",
            "Daughter",
            "Wife",
            "Husband",
            "Father",
            "Mother",
            )

        var maritalStatusList = listOf(
            "Single",
            "Married",
            "Divorced"
        )

        val relationshipAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, relationshipList
        )
        actRelationship.setAdapter(relationshipAdapter)
        val maritalStatusAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, maritalStatusList
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
        btnSubmit.setOnClickListener {

            if(validateFields())
            {
                val familyMember=FamilyDetails(fullName = etFullName.text.toString(), dob = etDob.text.toString(), relationship = actRelationship.text.toString(), maritalStatus = actMaritalStatus.text.toString())
                familyDetailsList.add(familyMember)
                adapter.notifyDataSetChanged()
                dialog.dismiss()

            }else{

            }
        }
        etDob.setOnClickListener {
            showDatePicker()
        }


    }
    private fun validateFields():Boolean{
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
}
