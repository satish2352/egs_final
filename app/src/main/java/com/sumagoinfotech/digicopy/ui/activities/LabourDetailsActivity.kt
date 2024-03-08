package com.sumagoinfotech.digicopy.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetailsBinding
import com.sumagoinfotech.digicopy.databinding.ActivityMainBinding
import com.sumagoinfotech.digicopy.utils.LabourInputData
import com.sumagoinfotech.digicopy.utils.MyValidator
import org.checkerframework.framework.qual.DefaultQualifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LabourDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityLabourDetailsBinding
    private lateinit var districts: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeFields()


        binding.btnNext.setOnClickListener {

            //if (validateFieldsX()) {
            if (true) {

                LabourInputData.fullName= binding.etFullName.text.toString()
                LabourInputData.dateOfBirth= binding.etDob.text.toString()
                LabourInputData.gender= binding.actGender.text.toString()
                LabourInputData.district= binding.actDistrict.text.toString()
                LabourInputData.village= binding.actVillage.text.toString()
                LabourInputData.taluka= binding.actTaluka.text.toString()
                LabourInputData.mobile= binding.etMobileNumber.text.toString()
                LabourInputData.landline= binding.etLandLine.text.toString()
                LabourInputData.idCard= binding.etMgnregaIdNumber.text.toString()

                val intent = Intent(this, LabourDetailsActivity2::class.java)
                startActivity(intent)
            } else {

            }

        }
    }

    private fun initializeFields() {

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