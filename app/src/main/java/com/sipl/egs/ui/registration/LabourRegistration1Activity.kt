package com.sipl.egs.ui.registration

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.AreaDao
import com.sipl.egs.database.dao.GenderDao
import com.sipl.egs.database.dao.SkillsDao
import com.sipl.egs.database.entity.AreaItem
import com.sipl.egs.database.entity.Gender
import com.sipl.egs.database.entity.Skills
import com.sipl.egs.databinding.ActivityLabourRegistration1Binding
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.LabourInputData
import com.sipl.egs.utils.MyValidator
import com.sipl.egs.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LabourRegistration1Activity : AppCompatActivity() {
    lateinit var binding: ActivityLabourRegistration1Binding
    private lateinit var districts: List<String>
    private lateinit var labourInputData: LabourInputData
    private lateinit var registrationViewModel: RegistrationViewModel
    private  var isInternetAvailable=false
    private lateinit var appDatabase: AppDatabase
    private lateinit var areaDao: AreaDao
    private lateinit var genderDao: GenderDao
    private lateinit var skillsDao: SkillsDao
    private lateinit var districtList:List<AreaItem>
    private lateinit var villageList:List<AreaItem>
    private lateinit var talukaList:List<AreaItem>
    private lateinit var genderList:List<Gender>
    private lateinit var skillsList:List<Skills>
    private var districtNames= mutableListOf<String>()
    private var villageNames= mutableListOf<String>()
    private var talukaNames= mutableListOf<String>()
    private var genderNames= mutableListOf<String>()
    private var skillsNames= mutableListOf<String>()
    private var districtId=""
    private var villageId=""
    private var talukaId=""
    private var genderId=""
    private var skillId=""
    private lateinit var progressDialog:CustomProgressDialog
    private var isMgnregaIdVerified=false
    private var isAllFieldsValidated=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourRegistration1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog= CustomProgressDialog(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.registration_step_1)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
        skillsDao=appDatabase.skillsDao()
        genderDao=appDatabase.genderDao()
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        initializeFields()
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




        /*binding.etMgnregaIdNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(s?.length==8){
                    if(isInternetAvailable){
                        CoroutineScope(Dispatchers.IO).launch {
                            checkIfMgnregaIdExists(s.toString())
                        }
                    }
                }
            }
        })*/


        binding.etMgnregaIdNumber.setOnFocusChangeListener { view:View, hasFocus:Boolean ->
            if(!hasFocus){
                if(isInternetAvailable){
                    CoroutineScope(Dispatchers.IO).launch {
                        if(binding.etMgnregaIdNumber.text.toString().length>0)
                        {
                            checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString())
                        }
                    }
                }
            }
        }


        binding.btnNext.setOnClickListener {

            if (validateFieldsX()) {
                isAllFieldsValidated=true
                if(isInternetAvailable && isMgnregaIdVerified==false){

                    CoroutineScope(Dispatchers.IO).launch {
                        checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString())
                    }
                }else{
                    saveAndGoToNextPage()
                }

            } else {
                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@LabourRegistration1Activity)
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
    private fun saveAndGoToNextPage(){
        labourInputData=LabourInputData()
        labourInputData.fullName= binding.etFullName.text.toString()
        labourInputData.dateOfBirth= binding.etDob.text.toString()
        labourInputData.district=districtId
        labourInputData.village= villageId
        labourInputData.skill=skillId
        labourInputData.gender=genderId
        labourInputData.taluka= talukaId
        labourInputData.mobile= binding.etMobileNumber.text.toString()
        labourInputData.landline= binding.etLandLine.text.toString()
        labourInputData.idCard= binding.etMgnregaIdNumber.text.toString()
        registrationViewModel.setData(labourInputData)
        registrationViewModel.fullName= binding.etFullName.text.toString()
        registrationViewModel.dateOfBirth= binding.etDob.text.toString()
        registrationViewModel.gender= binding.actGender.text.toString()
        registrationViewModel.district= binding.actDistrict.text.toString()
        registrationViewModel.village= binding.actVillage.text.toString()
        registrationViewModel.taluka= binding.actTaluka.text.toString()
        registrationViewModel.mobile= binding.etMobileNumber.text.toString()
        registrationViewModel.landline= binding.etLandLine.text.toString()
        registrationViewModel.idCard= binding.etMgnregaIdNumber.text.toString()
        val intent = Intent(this, LabourRegistration2Activity::class.java)
        intent.putExtra("LabourInputData", labourInputData)
        startActivity(intent)
    }

    private suspend fun checkIfMgnregaIdExists(mgnregaId: String) {
        runOnUiThread {
            progressDialog.show()
        }
        val apiService = ApiClient.create(this@LabourRegistration1Activity)
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response= apiService.checkMgnregaCardIdExists(mgnregaId)
                if(response.isSuccessful){
                    runOnUiThread { progressDialog.dismiss() }
                    if(!response.body()?.status.equals("true"))
                    {
                        isMgnregaIdVerified=false
                        runOnUiThread {
                            binding.etMgnregaIdNumber.error="Mgnrega Card Id already exists with another user"
                        }
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@LabourRegistration1Activity,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{

                       isMgnregaIdVerified=true
                        runOnUiThread { binding.etMgnregaIdNumber.error=null }
                        withContext(Dispatchers.Main){
                            if(isAllFieldsValidated){
                                saveAndGoToNextPage()
                            }
                        }
                    }
                }else{
                    withContext(Dispatchers.Main){
                        progressDialog.dismiss()
                        Toast.makeText(this@LabourRegistration1Activity,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                //runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { progressDialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LabourRegistration1Activity,resources.getString(R.string.response_failed),
                        Toast.LENGTH_SHORT).show()
                }
                Log.d("mytag","checkIfAadharCardExists "+e.message)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@LabourRegistration1Activity)
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



    private fun initializeFields() {
        districtList=ArrayList<AreaItem>()
        CoroutineScope(Dispatchers.IO).launch {

            val waitingJob=async {  districtList=areaDao.getAllDistrict()
                genderList=genderDao.getAllGenders();
                skillsList=skillsDao.getAllSkills() }
            waitingJob.await()
            for (district in districtList){
                districtNames.add(district.name)
            }
            for(skill in skillsList){
                skillsNames.add(skill.skills)
            }
            for(gender in genderList){
                genderNames.add(gender.gender_name)
            }
        }
        talukaList=ArrayList<AreaItem>()
        villageList=ArrayList<AreaItem>()
        val genderAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, genderNames
        )
        val skillsAdapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,skillsNames)
        binding.actSkill.setAdapter(skillsAdapter)
        binding.actSkill.setOnFocusChangeListener { v, hasFocus ->
            binding.actSkill.showDropDown()
        }
        binding.actSkill.setOnClickListener {
            binding.actSkill.showDropDown()
        }
        binding.actGender.setAdapter(genderAdapter)
        binding.actGender.setOnFocusChangeListener { abaad, asd ->
            binding.actGender.showDropDown()
        }
        binding.actGender.setOnClickListener {
            binding.actGender.showDropDown()
        }
        binding.actGender.setOnItemClickListener { parent, view, position, id ->
            genderId=genderList[position].id.toString()
        }
        binding.actSkill.setOnItemClickListener { parent, view, position, id ->
            skillId=skillsList[position].id.toString()
        }


        binding.etDob.setOnClickListener {
            //showDatePicker()
            showDatePickerDialog()
        }
        val districtAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, districtNames
        )
        binding.actDistrict.setAdapter(districtAdapter)
        binding.actDistrict.setOnItemClickListener { parent, view, position, id ->
            districtId=districtList[position].location_id
            binding.actTaluka.setText("")
            binding.actVillage.setText("")
            CoroutineScope(Dispatchers.IO).launch {
                talukaNames.clear();
                talukaList=areaDao.getAllTalukas(districtList[position].location_id)
                for (taluka in talukaList){
                    talukaNames.add(taluka.name)
                }
                val talukaAdapter = ArrayAdapter(
                    this@LabourRegistration1Activity, android.R.layout.simple_list_item_1, talukaNames
                )
                withContext(Dispatchers.Main){
                    binding.actTaluka.setAdapter(talukaAdapter)
                }
            }
        }
        binding.actTaluka.setOnItemClickListener { parent, view, position, id ->
            CoroutineScope(Dispatchers.IO).launch {
                talukaId=talukaList[position].location_id
                villageNames.clear();
                binding.actVillage.setText("")
                villageList=areaDao.getVillageByTaluka(talukaList[position].location_id)
                for (village in villageList){
                    villageNames.add(village.name)
                }
                val villageAdapter = ArrayAdapter(
                    this@LabourRegistration1Activity, android.R.layout.simple_list_item_1, villageNames
                )
                Log.d("mytag",""+villageNames.size)
                withContext(Dispatchers.Main){
                    binding.actVillage.setAdapter(villageAdapter)
                    binding.actVillage.setOnFocusChangeListener { abaad, asd ->
                        binding.actVillage.showDropDown()
                    }
                    binding.actVillage.setOnClickListener {
                        binding.actVillage.showDropDown()
                    }
                }
            }
        }
        binding.actVillage.setOnItemClickListener { parent, view, position, id ->
            villageId=villageList[position].location_id
        }
        binding.actDistrict.setOnFocusChangeListener { abaad, asd ->
            binding.actDistrict.showDropDown()
        }
        binding.actDistrict.setOnClickListener {
            binding.actDistrict.showDropDown()
        }
        binding.actTaluka.setOnFocusChangeListener { abaad, asd ->
            binding.actTaluka.showDropDown()
        }
        binding.actTaluka.setOnClickListener {
            binding.actTaluka.showDropDown()
        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val minCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1900) // Set the minimum year to 1900
        }

        val eighteenYearsAgo = Calendar.getInstance().apply {
            add(Calendar.YEAR, -18)
        }
        val datePickerDialog = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = formatDate(dayOfMonth, monthOfYear, year)
                binding.etDob.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis // Set minimum date
        datePickerDialog.datePicker.maxDate = eighteenYearsAgo.timeInMillis //
        datePickerDialog.show()
    }

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun validateFieldsX(): Boolean {
        val validationResults = mutableListOf<Boolean>()
        // Full Name
        if (MyValidator.isValidName(binding.etFullName.text.toString())) {
            binding.etFullName.error = null
            validationResults.add(true)
        } else {
            binding.etFullName.error = resources.getString(R.string.full_name_required)
            validationResults.add(false)
        }
        // Gender
        if (binding.actGender.enoughToFilter()) {
            binding.actGender.error = null
            validationResults.add(true)
        } else {
            binding.actGender.error = resources.getString(R.string.select_duration)
            validationResults.add(false)
        }
        // DOB
        if (binding.etDob.text.toString().length > 0 && !binding.etDob.text.isNullOrBlank()) {
            binding.etDob.error = null
            validationResults.add(true)
        } else {
            binding.etDob.error = resources.getString(R.string.select_date_of_birth)
            validationResults.add(false)
        }
        // District
        if (binding.actDistrict.enoughToFilter()) {
            binding.actDistrict.error = null
            validationResults.add(true)
        } else {
            binding.actDistrict.error = resources.getString(R.string.select_district)
            validationResults.add(false)
        }
        // Taluka

        if (binding.actTaluka.enoughToFilter()) {
            binding.actTaluka.error = null
            validationResults.add(true)
        } else {
            binding.actTaluka.error = resources.getString(R.string.select_taluka)
            validationResults.add(false)
        }
        // Village
        if (binding.actVillage.enoughToFilter()) {
            binding.actVillage.error = null
            validationResults.add(true)
        } else {
            binding.actVillage.error = resources.getString(R.string.select_village)
            validationResults.add(false)
        }


        if(binding.etLandLine.text.toString().length>0){

            if(binding.etLandLine.text.toString().length==11){
                binding.etLandLine.error = null
                validationResults.add(true)
            }else{
                binding.etLandLine.error = resources.getString(R.string.enter_valid_landline_number)
                validationResults.add(false)
            }
        }

        // Mobile
        if (MyValidator.isValidMobileNumber(binding.etMobileNumber.text.toString())) {
            binding.etMobileNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMobileNumber.error = resources.getString(R.string.enter_valid_mobile)
            validationResults.add(false)
        }
        if (binding.etMgnregaIdNumber.text.toString().length >0 && !binding.etMgnregaIdNumber.text.isNullOrBlank()) {
            binding.etMgnregaIdNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMgnregaIdNumber.error =
                resources.getString(R.string.enter_valid_id_card_number)
            validationResults.add(false)
        }
        if (binding.actSkill.enoughToFilter()) {
            binding.actSkill.error = null
            validationResults.add(true)
        } else {
            binding.actSkill.error =
                resources.getString(R.string.select_skills)
            validationResults.add(false)
        }
        return !validationResults.contains(false)
    }
}