package com.sipl.egs2.ui.registration

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs2.R
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.dao.AreaDao
import com.sipl.egs2.database.dao.GenderDao
import com.sipl.egs2.database.dao.LabourDao
import com.sipl.egs2.database.dao.SkillsDao
import com.sipl.egs2.database.entity.AreaItem
import com.sipl.egs2.database.entity.Gender
import com.sipl.egs2.database.entity.Labour
import com.sipl.egs2.database.entity.Skills
import com.sipl.egs2.databinding.ActivityLabourRegistrationEdit1Binding
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.LabourInputData
import com.sipl.egs2.utils.MyValidator
import com.sipl.egs2.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

class LabourRegistrationEdit1 : AppCompatActivity() {
    lateinit var binding: ActivityLabourRegistrationEdit1Binding
    private lateinit var districts: List<String>
    private lateinit var labourInputData: LabourInputData
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var appDatabase: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labour:Labour
    private  var isInternetAvailable=false
    private lateinit var areaDao: AreaDao
    private lateinit var districtList:List<AreaItem>
    private lateinit var villageList:List<AreaItem>
    private lateinit var talukaList:List<AreaItem>
    private var districtNames= mutableListOf<String>()
    private var villageNames= mutableListOf<String>()
    private var talukaNames= mutableListOf<String>()
    private var districtId=""
    private var villageId=""
    private var talukaId=""
    private lateinit var prevselectedDistrict:AreaItem
    private lateinit var prevSelectedVillage:AreaItem
    private lateinit var prevSelectedTaluka:AreaItem
    private lateinit var prevSelectedGender:Gender
    private lateinit var prevSelectedSkill:Skills
    private lateinit var skillsDao: SkillsDao
    private lateinit var genderDao: GenderDao
    private lateinit var genderList:List<Gender>
    private lateinit var skillsList:List<Skills>
    private var genderNames= mutableListOf<String>()
    private var skillsNames= mutableListOf<String>()
    private var genderId=""
    private var skillId=""
    private lateinit var progressDialog:CustomProgressDialog
    private var isMgnregaIdVerified=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourRegistrationEdit1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.update_details_step_1)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
        progressDialog= CustomProgressDialog(this)
        var labourId=intent.extras?.getString("id")
        labourDao=appDatabase.labourDao()
        genderDao=appDatabase.genderDao()
        skillsDao=appDatabase.skillsDao()
        CoroutineScope(Dispatchers.IO).launch {
           val waitingJob=async {
               labour=labourDao.getLabourById(Integer.parseInt(labourId))
               prevselectedDistrict=areaDao.getAreaByLocationId(labour.district)
               prevSelectedTaluka=areaDao.getAreaByLocationId(labour.taluka)
               prevSelectedVillage=areaDao.getAreaByLocationId(labour.village)
               prevSelectedGender=genderDao.getGenderById(labour.gender)
               prevSelectedSkill=skillsDao.getSkillById(labour.skill)
               talukaList=areaDao.getAllTalukas(labour.district)
               villageList=areaDao.getVillageByTaluka(labour.taluka)
               skillsList=skillsDao.getAllSkills()
               genderList=genderDao.getAllGenders()
           }
            waitingJob.await()
            withContext(Dispatchers.Main){
                binding.actVillage.setText(prevSelectedVillage.name)
                binding.actTaluka.setText(prevSelectedTaluka.name)
                binding.actDistrict.setText(prevselectedDistrict.name)
                binding.actGender.setText(prevSelectedGender.gender_name)
                binding.actSkill.setText(prevSelectedSkill.skills)
                for (taluka in talukaList)
                {
                    talukaNames.add(taluka.name)
                }
                Log.d("mytag",""+talukaNames.size);
                val talukaAdapter = ArrayAdapter(
                    this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, talukaNames
                )
                binding.actTaluka.setAdapter(talukaAdapter)
                for (village in villageList){
                    villageNames.add(village.name)
                }
                Log.d("mytag",""+villageNames.size);
                val villageAdapter = ArrayAdapter(
                    this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, villageNames
                )
                binding.actVillage.setAdapter(villageAdapter)
                for (skill in skillsList){
                    skillsNames.add(skill.skills)
                }
                val skillsAdapter = ArrayAdapter(
                    this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, skillsNames
                )
                binding.actSkill.setAdapter(skillsAdapter)
                for (gender in genderList){
                    genderNames.add(gender.gender_name)
                }
                val genderAdapter = ArrayAdapter(
                    this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, genderNames
                )
                binding.actGender.setAdapter(genderAdapter)

                initializeFields()

                binding.etMgnregaIdNumber.setOnFocusChangeListener { view: View, hasFocus:Boolean ->
                    if(!hasFocus){
                        if(isInternetAvailable){
                            CoroutineScope(Dispatchers.IO).launch {
                                if(binding.etMgnregaIdNumber.text.toString().length==10)
                                {
                                    checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
        districtList=ArrayList<AreaItem>()
        CoroutineScope(Dispatchers.IO).launch {
            districtList=areaDao.getAllDistrict()
            for (district in districtList){
                districtNames.add(district.name)
            }
        }
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
        binding.btnNext.setOnClickListener {
            if (validateFieldsX())
            {
                labour.fullName= binding.etFullName.text.toString()
                labour.dob= binding.etDob.text.toString()
                labour.district= districtId
                labour.village= villageId
                labour.taluka= talukaId
                labour.gender=genderId
                labour.skill=skillId
                labour.mobile= binding.etMobileNumber.text.toString()
                labour.landline= binding.etLandLine.text.toString()
                labour.mgnregaId= binding.etMgnregaIdNumber.text.toString()
                labourInputData=LabourInputData()
                labour.fullName= binding.etFullName.text.toString()
                labourInputData.dateOfBirth= binding.etDob.text.toString()
                labourInputData.gender= binding.actGender.text.toString()
                labourInputData.district= binding.actDistrict.text.toString()
                labourInputData.village= binding.actVillage.text.toString()
                labourInputData.taluka= binding.actTaluka.text.toString()
                labourInputData.mobile= binding.etMobileNumber.text.toString()
                labourInputData.landline= binding.etLandLine.text.toString()
                labourInputData.idCard= binding.etMgnregaIdNumber.text.toString()

                if (isInternetAvailable) {
                    CoroutineScope(Dispatchers.IO).launch {
                       val waitJob=async {  checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString()) }
                        val result=waitJob.await()
                        if(result){
                           runOnUiThread {  updateLabourDetails() }
                        }
                    }
                } else {
                    updateLabourDetails()
                }
            } else {
                val toast = Toast.makeText(this@LabourRegistrationEdit1, resources.getString(R.string.please_enter_all_details), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        binding.btnUpdateLabour.setOnClickListener {
            if (validateFieldsX())
            {
            } else {

                val toast = Toast.makeText(this@LabourRegistrationEdit1, resources.getString(R.string.please_enter_all_details), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@LabourRegistrationEdit1)
                builder.setTitle(resources.getString(R.string.exit))
                    .setMessage(resources.getString(R.string.are_you_sure_you_want_to_exit_this_screen))
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        // If "Yes" is clicked, exit the app
                        finish()
                    }
                    .setNegativeButton(resources.getString(R.string.no), null) // If "No" is clicked, do nothing
                    .show()

            }
        })
    }

    private fun updateLabourDetails(){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                var row=labourDao.updateLabour(labour)
                if(row>0){
                    runOnUiThread {
                        val intent = Intent(this@LabourRegistrationEdit1, LabourRegistrationEdit2::class.java)
                        intent.putExtra("id",labour.id.toString())
                        intent.putExtra("LabourInputData", labourInputData)
                        startActivity(intent)
                        val toast= Toast.makeText(this@LabourRegistrationEdit1,
                            getString(R.string.labour_information_updated_successfully),
                            Toast.LENGTH_SHORT)
                        toast.show()

                    }
                }else{
                    runOnUiThread {
                        val toast= Toast.makeText(this@LabourRegistrationEdit1,
                            getString(R.string.labour_details_not_updated_please_try_again),
                            Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("mytag", "LabourRegistrationEdit1: ${e.message}", e)
            e.printStackTrace()
        }

    }

    private suspend fun checkIfMgnregaIdExists(mgnregaId: String):Boolean {

        return suspendCancellableCoroutine {continuation->
            runOnUiThread {
                progressDialog.show()
            }
            val apiService = ApiClient.create(this@LabourRegistrationEdit1)
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val response= apiService.checkMgnregaCardIdExists(mgnregaId)
                    if(response.isSuccessful){
                        runOnUiThread { progressDialog.dismiss() }
                        if(!response.body()?.status.equals("true"))
                        {

                            continuation.resume(false)

                            Log.d("mytag"," exists")
                            runOnUiThread {
                                isMgnregaIdVerified=false
                                binding.etMgnregaIdNumber.error=
                                    getString(R.string.mgnrega_card_id_already_exists_with_another_user)
                            }
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@LabourRegistrationEdit1,response.body()?.message,
                                    Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            continuation.resume(true)
                            withContext(Dispatchers.Main){
                                isMgnregaIdVerified=true
                            }
                            runOnUiThread {
                                binding.etMgnregaIdNumber.error=null }

                        }
                    }else{
                        continuation.resume(false)
                        withContext(Dispatchers.Main){
                            progressDialog.dismiss()
                            Toast.makeText(this@LabourRegistrationEdit1,resources.getString(R.string.failed_updating_labour_response),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    //runOnUiThread {dialog.dismiss()  }
                } catch (e: Exception) {
                    continuation.resume(false)
                    runOnUiThread { progressDialog.dismiss() }
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LabourRegistrationEdit1,resources.getString(R.string.response_failed),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_edit,menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@LabourRegistrationEdit1)
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


    private fun initializeFields() {
        try {
            binding.etFullName.setText(labour.fullName)
            binding.etDob.setText(labour.dob)
            binding.etMobileNumber.setText(labour.mobile)
            binding.etLandLine.setText(labour.landline)
            binding.etMgnregaIdNumber.setText(labour.mgnregaId)
            genderId=prevSelectedGender.id.toString()
            villageId=prevSelectedVillage.location_id.toString()
            skillId=prevSelectedSkill.id.toString()
            districtId=prevselectedDistrict.location_id.toString()
            talukaId=prevSelectedTaluka.location_id.toString()
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
            binding.actSkill.setOnClickListener {
                binding.actSkill.showDropDown()
            }
            binding.actSkill.setOnFocusChangeListener { v, hasFocus ->
                binding.actSkill.showDropDown()
            }
            binding.etDob.setOnClickListener {

                showDatePickerDialog()
                //showDatePicker()
            }
            binding.actVillage.setOnFocusChangeListener { abaad, asd ->
                binding.actVillage.showDropDown()
            }
            binding.actVillage.setOnClickListener {
                binding.actVillage.showDropDown()
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
                        this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, talukaNames
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
                        this@LabourRegistrationEdit1, android.R.layout.simple_list_item_1, villageNames
                    )
                    Log.d("mytag",""+villageNames.size)
                    withContext(Dispatchers.Main){
                        binding.actVillage.setAdapter(villageAdapter)

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
            binding.actVillage.setOnFocusChangeListener { abaad, asd ->
                binding.actVillage.showDropDown()
            }
            binding.actVillage.setOnClickListener {
                binding.actVillage.showDropDown()
            }
        } catch (e: Exception) {
            Log.d("mytag", "LabourRegistrationEdit1: ${e.message}", e)
            e.printStackTrace()
        }


    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = formatDate(dayOfMonth, monthOfYear, year)
                binding.etDob.setText(selectedDate)
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

        // Mobile
        if (MyValidator.isValidMobileNumber(binding.etMobileNumber.text.toString())) {
            binding.etMobileNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMobileNumber.error = resources.getString(R.string.enter_valid_mobile)
            validationResults.add(false)
        }
        if (binding.etMgnregaIdNumber.text.toString().length ==10 && !binding.etMgnregaIdNumber.text.isNullOrBlank()) {
            binding.etMgnregaIdNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMgnregaIdNumber.error =
                resources.getString(R.string.enter_valid_id_card_number)
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
        return !validationResults.contains(false)
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
}