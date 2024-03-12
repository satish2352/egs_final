package com.sumagoinfotech.digicopy.ui.activities.registration

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.sumagoinfotech.digicopy.MainActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetailsBinding
import com.sumagoinfotech.digicopy.databinding.ActivityLabourRegistrationEdit1Binding
import com.sumagoinfotech.digicopy.ui.activities.LabourRegistration2Activity
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.MyValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LabourRegistrationEdit1 : AppCompatActivity() {
    lateinit var binding: ActivityLabourRegistrationEdit1Binding
    private lateinit var districts: List<String>
    private lateinit var labourInputData: LabourInputData
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labour:Labour
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourRegistrationEdit1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.update_details_step_1)
        var labourId=intent.extras?.getString("id")
        database= AppDatabase.getDatabase(this)
        labourDao=database.labourDao()
        CoroutineScope(Dispatchers.IO).launch {
            labour=labourDao.getLabourById(Integer.parseInt(labourId))

            runOnUiThread {
                initializeFields()
            }
        }
        binding.btnNext.setOnClickListener {
            if (validateFieldsX()) {

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
            if (validateFieldsX()) {
                labour.fullName= binding.etFullName.text.toString()
                labour.dob= binding.etDob.text.toString()
                labour.gender= binding.actGender.text.toString()
                labour.district= binding.actDistrict.text.toString()
                labour.village= binding.actVillage.text.toString()
                labour.taluka= binding.actTaluka.text.toString()
                labour.mobile= binding.etMobileNumber.text.toString()
                labour.landline= binding.etLandLine.text.toString()
                labour.mgnregaId= binding.etMgnregaIdNumber.text.toString()
                Log.d("mytag","Before")
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

//                    val intent = Intent(this, LabourRegistrationEdit2::class.java)
//                    intent.putExtra("id",labour.id.toString())
//                    intent.putExtra("LabourInputData", labourInputData)
//                    startActivity(intent)
            } else {

                val toast = Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_edit,menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        if(item.itemId==R.id.action_edit){


        }
        return super.onOptionsItemSelected(item)
    }


    private fun initializeFields() {

        binding.etFullName.setText(labour.fullName)
        binding.etDob.setText(labour.dob)
        binding.etMobileNumber.setText(labour.mobile)
        binding.etLandLine.setText(labour.landline)
        binding.etMgnregaIdNumber.setText(labour.mgnregaId)
        binding.actDistrict.setText(labour.district)
        binding.actGender.setText(labour.gender)
        binding.actTaluka.setText(labour.taluka)
        binding.actVillage.setText(labour.village)
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

            showDatePicker()
        }


        districts = listOf(
            "Ahmednagar",
            "Akola",
            "Amravati",
            "Aurangabad",
            "Beed",
            "Bhandara",
            "Buldhana",
            "Chandrapur",
            "Dhule",
            "Gadchiroli",
            "Gondia",
            "Hingoli",
            "Jalgaon",
            "Jalna",
            "Kolhapur",
            "Latur",
            "Mumbai City",
            "Mumbai Suburban",
            "Nagpur",
            "Nanded",
            "Nandurbar",
            "Nashik",
            "Osmanabad",
            "Palghar",
            "Parbhani",
            "Pune",
            "Raigad",
            "Ratnagiri",
            "Sangli",
            "Satara",
            "Sindhudurg",
            "Solapur",
            "Thane",
            "Wardha",
            "Washim",
            "Yavatmal"
        )
        val districtAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, districts
        )
        binding.actDistrict.setAdapter(districtAdapter)
        val talukaAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, talukas
        )
        binding.actTaluka.setAdapter(talukaAdapter)
        val villageAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, villages
        )
        binding.actVillage.setAdapter(villageAdapter)

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

    val talukas = listOf(
        // Sindhudurg District
        "Vaibhavwadi",
        "Devgad",
        "Malwan",
        "Sawantwadi",
        "Kudal",
        "Vengurla",
        "Dodamarg",

        // Ratnagiri District
        "Ratnagiri",
        "Sangameshwar",
        "Lanja",
        "Rajapur",
        "Chiplun",
        "Guhagar",
        "Dapoli",
        "Mandangad",
        "Khed",

        // Raigad District
        "Pen",
        "Alibag",
        "Murud",
        "Panvel",
        "Uran",
        "Karjat",
        "Khalapur",
        "Mangaon",
        "Tala",
        "Roha",
        "Sudhagad-Pali",
        "Mahad",
        "Poladpur",
        "Shrivardhan",
        "Mhasala",

        // Mumbai City and Suburban
        "Kurla",
        "Andheri",
        "Borivali",
        "Thane",
        "Kalyan",
        "Murbad",
        "Bhiwandi",
        "Shahapur",
        "Ulhasnagar",
        "Ambarnath",
        "Palghar",
        "Vasai",
        "Dahanu",
        "Talasari",
        "Jawhar",
        "Mokhada",
        "Vada",
        "Vikramgad",

        // Nashik District
        "Nashik",
        "Igatpuri",
        "Dindori",
        "Peth",
        "Trimbakeshwar",
        "Kalwan",
        "Deola",
        "Surgana",
        "Baglan",
        "Malegaon",
        "Nandgaon",
        "Chandwad",
        "Niphad",
        "Sinnar",
        "Yeola",

        // Nandurbar District
        "Nandurbar",
        "Navapur",
        "Shahada",
        "Talode",
        "Akkalkuwa",
        "Dhadgaon",

        // Dhule District
        "Dhule",
        "Sakri",
        "Sindkheda",
        "Shirpur",
        "Jalgaon",
        "Jamner",
        "Erandol",
        "Dharangaon",
        "Bhusawal",
        "Raver",
        "Muktainagar",
        "Bodwad",
        "Yawal",
        "Amalner",
        "Parola",
        "Chopda",
        "Pachora",
        "Bhadgaon",
        "Chalisgaon",

        // Jalgaon District
        "Buldhana",
        "Chikhli",
        "Deulgaon Raja",
        "Jalgaon Jamod",
        "Sangrampur",
        "Malkapur",
        "Motala",
        "Nandura",
        "Khamgaon",
        "Shegaon",
        "Mehkar",
        "Sindkhed Raja",
        "Lonar",

        // Akola District
        "Akola",
        "Akot",
        "Telhara",
        "Balapur",
        "Patur",
        "Murtajapur",
        "Barshitakli",
        "Washim",
        "Malegaon",
        "Risod",
        "Mangrulpir",
        "Karanja",
        "Manora",

        // Amravati District
        "Amravati",
        "Bhatukali",
        "Nandgaon Khandeshwar",
        "Dharni",
        "Chikhaldara",
        "Achalpur",
        "Chandurbazar",
        "Morshi",
        "Warud",
        "Daryapur",
        "Anjangaon-Surji",
        "Chandur",
        "Dhamangaon",
        "Tiosa",

        // Wardha District
        "Wardha",
        "Deoli",
        "Seloo",
        "Arvi",
        "Ashti",
        "Karanja",
        "Hinganghat",
        "Samudrapur",

        // Nagpur District
        "Nagpur Urban",
        "Nagpur Rural",
        "Kamptee",
        "Hingna",
        "Katol",
        "Narkhed",
        "Savner",
        "Kalameshwar",
        "Ramtek",
        "Mouda",
        "Parseoni",
        "Umred",
        "Kuhi",
        "Bhiwapur",

        // Bhandara District
        "Bhandara",
        "Tumsar",
        "Pauni",
        "Mohadi",
        "Sakoli",
        "Lakhani",
        "Lakhandur",
        "Gondia",
        "Goregaon",
        "Salekasa",
        "Tiroda",
        "Amgaon",
        "Deori",
        "Arjuni-Morgaon",
        "Sadak-Arjuni",

        // Gadchiroli District
        "Gadchiroli",
        "Dhanora",
        "Chamorshi",
        "Mulchera",
        "Desaiganj",
        "Armori",
        "Kurkheda",
        "Korchi",
        "Aheri",
        "Etapalli",
        "Bhamragad",
        "Sironcha",

        // Chandrapur District
        "Chandrapur",
        "Saoli",
        "Mul",
        "Ballarpur",
        "Pombhurna",
        "Gondpimpri",
        "Warora",
        "Chimur",
        "Bhadravati",
        "Bramhapuri",
        "Nagbhid",
        "Sindewahi",
        "Rajura",
        "Korpana",
        "Jiwati",

        // Yavatmal District
        "Yavatmal",
        "Arni",
        "Babhulgaon",
        "Kalamb",
        "Darwha",
        "Digras",
        "Ner",
        "Pusad",
        "Umarkhed",
        "Mahagaon",
        "Kelapur",
        "Ralegaon",
        "Ghatanji",
        "Wani",
        "Maregaon",
        "Zari Jamani",

        // Nanded District
        "Nanded",
        "Ardhapur",
        "Mudkhed",
        "Bhokar",
        "Umri",
        "Loha",
        "Kandhar",
        "Kinwat",
        "Himayatnagar",
        "Hadgaon",
        "Mahur",
        "Deglur",
        "Mukhed",
        "Dharmabad",
        "Biloli",
        "Naigaon",

        // Hingoli District
        "Hingoli",
        "Sengaon",
        "Kalamnuri",
        "Basmath",
        "Aundha Nagnath",

        // Parbhani District
        "Parbhani",
        "Sonpeth",
        "Gangakhed",
        "Palam",
        "Purna",
        "Sailu",
        "Jintur",
        "Manwath",
        "Pathri",

        // Jalna District
        "Jalna",
        "Bhokardan",
        "Jafrabad",
        "Badnapur",
        "Ambad",
        "Ghansawangi",
        "Partur",
        "Mantha",

        // Aurangabad District
        "Aurangabad",
        "Kannad",
        "Soegaon",
        "Sillod",
        "Phulambri",
        "Khuldabad",
        "Vaijapur",
        "Gangapur",
        "Paithan",

        // Beed District
        "Beed",
        "Ashti",
        "Patoda",
        "Shirur-Kasar",
        "Georai",
        "Majalgaon",
        "Wadwani",
        "Kaij",
        "Dharur",
        "Parli",
        "Ambajogai",

        // Latur District
        "Latur",
        "Renapur",
        "Ausa",
        "Ahmedpur",
        "Jalkot",
        "Chakur",
        "Shirur Anantpal",
        "Nilanga",
        "Deoni",
        "Udgir",

        // Osmanabad District
        "Osmanabad",
        "Tuljapur",
        "Bhum",
        "Paranda",
        "Washi",
        "Kalamb",
        "Lohara",
        "Umarga",

        // Solapur District
        "Solapur North",
        "Barshi",
        "Solapur South",
        "Akkalkot",
        "Madha",
        "Karmala",
        "Pandharpur",
        "Mohol",
        "Malshiras",
        "Sangole",
        "Mangalvedhe",
        "Nagar",
        "Shevgaon",
        "Pathardi",
        "Parner",
        "Sangamner",
        "Kopargaon",
        "Akole",
        "Shrirampur",
        "Nevasa",
        "Rahata",
        "Rahuri",
        "Shrigonda",
        "Karjat",
        "Jamkhed",
        "Pune City",
        "Haveli",
        "Khed",
        "Junnar",
        "Ambegaon",
        "Maval",
        "Mulshi",
        "Shirur",
        "Purandhar (Saswad)",
        "Velhe",
        "Bhor",
        "Baramati",
        "Indapur",
        "Daund",

        // Satara District
        "Satara",
        "Jaoli",
        "Koregaon",
    )

    val villages = listOf(
        "Ambegaon",
        "Ambejogai",
        "Anjangaon",
        "Ashti",
        "Babulgaon",
        "Badnapur",
        "Balapur",
        "Bhoom",
        "Bhor",
        "Biloli",
        "Brahmapuri",
        "Chandwad",
        "Chopda",
        "Dahiwadi",
        "Deolali",
        "Devrukh",
        "Gondia",
        "Hinganghat",
        "Ichalkaranji",
        "Indapur",
        "Jamkhed",
        "Jalna",
        "Jath",
        "Jaysingpur",
        "Junnar",
        "Kalamb",
        "Karjat",
        "Khamgaon",
        "Khuldabad",
        "Kolhapur",
        "Kopargaon",
        "Koregaon",
        "Kudal",
        "Kusumba",
        "Lanja",
        "Latur",
        "Lonar",
        "Mahad",
        "Malkapur",
        "Malsiras",
        "Manmad",
        "Mhasla",
        "Mudkhed",
        "Mumbai",
        "Murtijapur",
        "Nanded",
        "Nandgaon",
        "Nashik",
        "Nashirabad",
        "Navapur",
        "Neral",
        "Nilanga",
        "Osmanabad",
        "Pachora",
        "Paithan",
        "Palghar",
        "Pandharpur",
        "Paranda",
        "Parbhani",
        "Pathardi",
        "Pune",
        "Purna",
        "Rajura",
        "Ralegaon",
        "Ratnagiri",
        "Sakri",
        "Sangamner",
        "Satana",
        "Shahapur",
        "Shendurjana",
        "Shirpur",
        "Shrivardhan",
        "Solapur",
        "Sinnar",
        "Sangli",
        "Sangamner",
        "Satara",
        "Shahapur",
        "Shrigonda",
        "Shrirampur",
        "Sindhudurg",
        "Sinnar",
        "Taloda",
        "Tasgaon",
        "Tuljapur",
        "Tumsar",
        "Uran",
        "Umarkhed",
        "Umarga",
        "Vaijapur",
        "Vaduj",
        "Vaijapur",
        "Vasai",
        "Wada",
        "Wai",
        "Wardha",
        "Warora",
        "Washim",
        "Yawal",
        "Yavatmal",
        "Zari"
    )
}