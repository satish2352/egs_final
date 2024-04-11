package com.sumagoinfotech.digicopy.ui.registration

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.GenderDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.SkillsDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Gender
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.entity.Skills
import com.sumagoinfotech.digicopy.databinding.ActivityLabourRegistrationEdit1Binding
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.MyValidator
import com.sumagoinfotech.digicopy.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

                binding.etMgnregaIdNumber.addTextChangedListener(object : TextWatcher {
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
                })
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
                val intent = Intent(this, LabourRegistrationEdit2::class.java)
                intent.putExtra("id",labour.id.toString())
                intent.putExtra("LabourInputData", labourInputData)
                startActivity(intent)
            } else {

                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }



        }
        binding.btnUpdateLabour.setOnClickListener {
            if (validateFieldsX())
            {



                Log.d("mytag","gender Id "+genderId)
                Log.d("mytag","skill Id "+skillId)
                Log.d("mytag","dist Id "+districtId)
                Log.d("mytag","taluka Id "+talukaId)
                Log.d("mytag","Village Id "+villageId)

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

                if(isInternetAvailable){
                    if(isMgnregaIdVerified==false)
                    {
                        CoroutineScope(Dispatchers.IO).launch {
                            checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString())
                        }
                    }else{
                        updateLabourDetails()
                    }
                }else{
                    updateLabourDetails()
                }

            } else {

                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@LabourRegistrationEdit1)
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

    private fun updateLabourDetails(){
        CoroutineScope(Dispatchers.IO).launch {
            var row=labourDao.updateLabour(labour)
            Log.d("mytag",""+row)
            if(row>0){
                runOnUiThread {
                    val toast= Toast.makeText(this@LabourRegistrationEdit1,"Labour updated successfully",
                        Toast.LENGTH_SHORT)
                    toast.show()
                }
            }else{
                runOnUiThread {
                    val toast= Toast.makeText(this@LabourRegistrationEdit1,"Labour not updated please try again ",
                        Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
        }
        Log.d("mytag","After")
    }
    private suspend fun checkIfMgnregaIdExists(mgnregaId: String) {
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
                        isMgnregaIdVerified=false
                        runOnUiThread {
                            binding.etMgnregaIdNumber.error="Mgnrega Card Id already exists with another user"
                        }
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@LabourRegistrationEdit1,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        isMgnregaIdVerified=true
                        runOnUiThread { binding.etMgnregaIdNumber.error=null }

                    }
                }else{
                    withContext(Dispatchers.Main){
                        progressDialog.dismiss()
                        Toast.makeText(this@LabourRegistrationEdit1,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                //runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { progressDialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LabourRegistrationEdit1,resources.getString(R.string.response_failed),
                        Toast.LENGTH_SHORT).show()
                }
                Log.d("mytag","checkIfAadharCardExists "+e.message)
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
        binding.etFullName.setText(labour.fullName)
        binding.etDob.setText(labour.dob)
        binding.etMobileNumber.setText(labour.mobile)
        binding.etLandLine.setText(labour.landline)
        binding.etMgnregaIdNumber.setText(labour.mgnregaId)
        genderId=prevSelectedGender.id.toString()
        villageId=prevSelectedVillage.id.toString()
        skillId=prevSelectedSkill.id.toString()
        districtId=prevselectedDistrict.id.toString()
        talukaId=prevSelectedTaluka.id.toString()
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
        if (binding.etMgnregaIdNumber.text.toString().length ==8 && !binding.etMgnregaIdNumber.text.isNullOrBlank()) {
            binding.etMgnregaIdNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMgnregaIdNumber.error =
                resources.getString(R.string.enter_valid_id_card_number)
            validationResults.add(false)
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