package com.sumagoinfotech.digicopy.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetailsBinding
import com.sumagoinfotech.digicopy.ui.activities.registration.RegistrationViewModel
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.MyValidator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LabourRegistration1Activity : AppCompatActivity() {
    lateinit var binding: ActivityLabourDetailsBinding
    private lateinit var districts: List<String>
    private lateinit var labourInputData: LabourInputData
    private lateinit var registrationViewModel: RegistrationViewModel
    private  var isInternetAvailable=false
    private lateinit var appDatabase: AppDatabase
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.registration_step_1)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
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
        binding.btnNext.setOnClickListener {

            if (validateFieldsX()) {
                labourInputData=LabourInputData()
                labourInputData.fullName= binding.etFullName.text.toString()
                labourInputData.dateOfBirth= binding.etDob.text.toString()
                labourInputData.gender= binding.actGender.text.toString()
                //labourInputData.district= binding.actDistrict.text.toString()
                labourInputData.district=districtId
                //labourInputData.village= binding.actVillage.text.toString()
                labourInputData.village= villageId
                //labourInputData.village= binding.actVillage.text.toString()
                labourInputData.taluka= talukaId
                labourInputData.mobile= binding.etMobileNumber.text.toString()
                labourInputData.landline= binding.etLandLine.text.toString()
                labourInputData.idCard= binding.etMgnregaIdNumber.text.toString()
                registrationViewModel.setData(labourInputData)
                registrationViewModel.fullName= "binding.etFullName.text.toString()"
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
            } else {

                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }



    private fun initializeFields() {
        districtList=ArrayList<AreaItem>()
        CoroutineScope(Dispatchers.IO).launch {
            districtList=areaDao.getAllDistrict()
            for (district in districtList){
                districtNames.add(district.name)
            }
        }
        talukaList=ArrayList<AreaItem>()
        villageList=ArrayList<AreaItem>()

        val names = listOf("MALE", "FEMALE")
        val genderAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, names
        )
        binding.actGender.setAdapter(genderAdapter)
        binding.actGender.setOnFocusChangeListener { abaad, asd ->
            binding.actGender.showDropDown()
        }
        binding.actGender.setOnClickListener {
            binding.actGender.showDropDown()
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

        // Mobile
        if (MyValidator.isValidMobileNumber(binding.etMobileNumber.text.toString())) {
            binding.etMobileNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMobileNumber.error = resources.getString(R.string.enter_valid_mobile)
            validationResults.add(false)
        }
        if (binding.etMgnregaIdNumber.text.toString().length > 0 && !binding.etMgnregaIdNumber.text.isNullOrBlank()) {
            binding.etMgnregaIdNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMgnregaIdNumber.error =
                resources.getString(R.string.enter_valid_id_card_number)
            validationResults.add(false)
        }
        return !validationResults.contains(false)
    }
}