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
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.FamilyDetailsListOnlineAdapter
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.GenderDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.SkillsDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Gender
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.entity.Skills
import com.sumagoinfotech.digicopy.databinding.ActivityLabourUpdateOnline1Binding
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.update.LabourUpdateDetails
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LabourUpdateOnline1Activity : AppCompatActivity() {
    private lateinit var binding:ActivityLabourUpdateOnline1Binding
    private lateinit var districts: List<String>
    private lateinit var labourInputData: LabourInputData
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var appDatabase: AppDatabase
    private lateinit var labourDao: LabourDao
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
    private lateinit var prevselectedDistrict: AreaItem
    private lateinit var prevSelectedVillage: AreaItem
    private lateinit var prevSelectedTaluka: AreaItem
    private lateinit var prevSelectedGender: Gender
    private lateinit var prevSelectedSkill: Skills
    private lateinit var skillsDao: SkillsDao
    private lateinit var genderDao: GenderDao
    private lateinit var genderList:List<Gender>
    private lateinit var skillsList:List<Skills>
    private var genderNames= mutableListOf<String>()
    private var skillsNames= mutableListOf<String>()
    private var genderId=""
    private var skillId=""
    private var family=""
    private var labourId=""
    private lateinit var dialog:CustomProgressDialog
    private lateinit var progressDialog:CustomProgressDialog
    private var isMgnregaIdVerified=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourUpdateOnline1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("mytag","LabourUpdateOnline1Activity");
        dialog= CustomProgressDialog(this)
        progressDialog= CustomProgressDialog(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.update_details_step_1)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
        var mgnregaCardId=intent.extras?.getString("id")
        labourDao=appDatabase.labourDao()
        genderDao=appDatabase.genderDao()
        skillsDao=appDatabase.skillsDao()

            getDetailsFromServer(mgnregaCardId!!)

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
            if (validateFieldsX()) {

                labourInputData=LabourInputData()
                labourInputData.fullName= binding.etFullName.text.toString()
                labourInputData.dateOfBirth= binding.etDob.text.toString()
                labourInputData.gender= binding.actGender.text.toString()
                labourInputData.district= binding.actDistrict.text.toString()
                labourInputData.village= binding.actVillage.text.toString()
                labourInputData.taluka= binding.actTaluka.text.toString()
                labourInputData.mobile= binding.etMobileNumber.text.toString()
                labourInputData.landline= binding.etLandLine.text.toString()
                labourInputData.idCard= binding.etMgnregaIdNumber.text.toString()
                labourInputData.idCard= binding.etMgnregaIdNumber.text.toString()
                labourInputData.family= ""
                val intent = Intent(this, LabourUpdateOnline2Activity::class.java)
                intent.putExtra("id",mgnregaCardId)
                intent.putExtra("LabourInputData", labourInputData)
                startActivity(intent)
            } else
            {
                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        binding.btnUpdateLabour.setOnClickListener {
            if(isInternetAvailable){
                if (validateFieldsX())
                {
                    if(isMgnregaIdVerified){
                        dialog.show()
                        CoroutineScope(Dispatchers.IO).launch {
                            val apiService=ApiClient.create(this@LabourUpdateOnline1Activity)
                            var name=binding.etFullName.text.toString();
                            var dob= binding.etDob.text.toString()
                            var district= districtId
                            var village= villageId
                            var taluka= talukaId
                            var gender=genderId
                            var skill=skillId
                            var mobile= binding.etMobileNumber.text.toString()
                            var landline= binding.etLandLine.text.toString()
                            val mgnregaId= binding.etMgnregaIdNumber.text.toString()
                            val response=apiService.updateLabourFirstForm(
                                fullName = name,
                                genderId=gender,
                                dateOfBirth = dob,
                                districtId=districtId,
                                villageId = villageId,
                                talukaId = talukaId,
                                skillId = skillId,
                                id =labourId ,
                                mobileNumber = mobile,
                                landLineNumber =landline,
                                mgnregaId = mgnregaId!!
                            )
                            if(response.isSuccessful){

                                if(response.body()?.status.equals("true"))
                                {
                                    mgnregaCardId=binding.etMgnregaIdNumber.text.toString()
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(this@LabourUpdateOnline1Activity,"Information updated successsfully",Toast.LENGTH_LONG).show()
                                    }
                                }else{
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(this@LabourUpdateOnline1Activity,
                                            response.body()?.message,Toast.LENGTH_LONG).show()
                                    }
                                }
                                withContext(Dispatchers.Main){
                                    dialog.dismiss()
                                }
                            }else{
                                withContext(Dispatchers.Main){
                                    dialog.dismiss()
                                    Toast.makeText(this@LabourUpdateOnline1Activity,getString(R.string.error_while_updating_information_please_try_again),Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }else{
                        CoroutineScope(Dispatchers.IO).launch {
                            checkIfMgnregaIdExists(binding.etMgnregaIdNumber.text.toString())
                        }
                    }
                } else {

                    val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            else{
                val toast = Toast.makeText(applicationContext,
                    getString(R.string.internet_is_not_available_please_check), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@LabourUpdateOnline1Activity)
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
    private suspend fun checkIfMgnregaIdExists(mgnregaId: String) {
        runOnUiThread {
            progressDialog.show()
        }
        val apiService = ApiClient.create(this@LabourUpdateOnline1Activity)
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
                            Toast.makeText(this@LabourUpdateOnline1Activity,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        isMgnregaIdVerified=true
                        runOnUiThread { binding.etMgnregaIdNumber.error=null }

                    }
                }else{
                    withContext(Dispatchers.Main){
                        progressDialog.dismiss()
                        Toast.makeText(this@LabourUpdateOnline1Activity,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                //runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { progressDialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LabourUpdateOnline1Activity,resources.getString(R.string.response_failed),
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
            val builder = AlertDialog.Builder(this@LabourUpdateOnline1Activity)
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
                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, talukaNames
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
                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, villageNames
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
        if (binding.etMgnregaIdNumber.text.toString().length == 8 && !binding.etMgnregaIdNumber.text.isNullOrBlank()) {
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
    private  fun getDetailsFromServer(mgnregaCardId:String){
        Log.d("mytag","getDetailsFromServer")
        try {
            dialog.show()
            val apiService= ApiClient.create(this@LabourUpdateOnline1Activity)
            CoroutineScope(Dispatchers.IO).launch {
                val response=apiService.getLabourDetailsForUpdate(mgnregaCardId)
                dialog.dismiss()
                Log.d("mytag","getDetailsFromServer")
                if(response.isSuccessful){
                    Log.d("mytag","getDetailsFromServer isSuccessful")
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        if(response.body()?.status.equals("true"))
                        {
                            Log.d("mytag","getDetailsFromServer isSuccessful true")
                            //labour=labourDao.getLabourById(Integer.parseInt(mgnregaCardId))
                            val labourInfo=list?.get(0);
                            labourId=labourInfo?.id.toString()
                            Log.d("mytag","===>"+labourInfo?.district_id.toString());
                            Log.d("mytag","===>"+labourInfo?.taluka_id.toString());
                            Log.d("mytag","===>"+labourInfo?.village_id.toString());
                            districtId=labourInfo?.district_id.toString();
                            talukaId=labourInfo?.taluka_name.toString();
                            villageId=labourInfo?.village_id.toString();
                            prevselectedDistrict=areaDao.getAreaByLocationId(labourInfo?.district_id.toString())
                            prevSelectedTaluka=areaDao.getAreaByLocationId(labourInfo?.taluka_id.toString())
                            prevSelectedVillage=areaDao.getAreaByLocationId(labourInfo?.village_id.toString())


                            Log.d("mytag","prevselectedDistrict===>"+prevselectedDistrict.name);
                            Log.d("mytag","prevselectedDistrict===>"+prevselectedDistrict.location_id);
                            Log.d("mytag","prevSelectedTaluka===>"+prevSelectedTaluka.name);
                            Log.d("mytag","prevselectedDistrict===>"+prevSelectedTaluka.id);
                            Log.d("mytag","prevSelectedVillage===>"+prevSelectedVillage.name);
                            Log.d("mytag","prevSelectedVillage===>"+prevSelectedVillage.id);



                            prevSelectedGender=genderDao.getGenderById(labourInfo?.gender_id.toString())
                            prevSelectedSkill=skillsDao.getSkillById(labourInfo?.skill_id.toString())
                            talukaList=areaDao.getAllTalukas(labourInfo?.district_id.toString())
                            villageList=areaDao.getVillageByTaluka(labourInfo?.taluka_id.toString())
                            skillsList=skillsDao.getAllSkills()
                            genderList=genderDao.getAllGenders()

                            withContext(Dispatchers.Main) {
                                binding.actVillage.setText(prevSelectedVillage.name)
                                binding.actTaluka.setText(prevSelectedTaluka.name)
                                binding.actDistrict.setText(prevselectedDistrict.name)
                                binding.actGender.setText(prevSelectedGender.gender_name)
                                binding.actSkill.setText(prevSelectedSkill.skills)
                                binding.etFullName.setText(labourInfo?.full_name)
                                binding.etDob.setText(labourInfo?.date_of_birth)
                                binding.etMobileNumber.setText(labourInfo?.mobile_number)
                                binding.etLandLine.setText(labourInfo?.landline_number)
                                binding.etMgnregaIdNumber.setText(labourInfo?.mgnrega_card_id)
                                family=labourInfo?.family_details.toString()
                                for (taluka in talukaList)
                                {
                                    talukaNames.add(taluka.name)
                                }
                                Log.d("mytag",""+talukaNames.size);
                                val talukaAdapter = ArrayAdapter(
                                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, talukaNames
                                )
                                binding.actTaluka.setAdapter(talukaAdapter)
                                for (village in villageList){
                                    villageNames.add(village.name)
                                }
                                Log.d("mytag",""+villageNames.size);
                                val villageAdapter = ArrayAdapter(
                                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, villageNames
                                )
                                binding.actVillage.setAdapter(villageAdapter)
                                for (skill in skillsList){
                                    skillsNames.add(skill.skills)
                                }
                                val skillsAdapter = ArrayAdapter(
                                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, skillsNames
                                )
                                binding.actSkill.setAdapter(skillsAdapter)
                                for (gender in genderList){
                                    genderNames.add(gender.gender_name)
                                }
                                val genderAdapter = ArrayAdapter(
                                    this@LabourUpdateOnline1Activity, android.R.layout.simple_list_item_1, genderNames
                                )
                                binding.actGender.setAdapter(genderAdapter)

                                initializeFields()
                            }
                        }else{
                            Log.d("mytag","getDetailsFromServer isSuccessful false")
                        }
                    }else {
                        runOnUiThread {
                            Toast.makeText(this@LabourUpdateOnline1Activity, "No records found", Toast.LENGTH_SHORT).show()
                        }

                    }
                } else{
                    runOnUiThread {
                        Toast.makeText(this@LabourUpdateOnline1Activity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        } catch (e: Exception) {
            Log.d("mytag","getDetailsFromServer : Exception => "+e.message)
            dialog.dismiss()
            e.printStackTrace()
        }
    }
}