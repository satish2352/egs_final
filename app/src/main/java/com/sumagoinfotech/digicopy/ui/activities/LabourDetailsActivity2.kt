package com.sumagoinfotech.digicopy.ui.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetails2Binding
import com.sumagoinfotech.digicopy.model.FamilyDetails
import com.sumagoinfotech.digicopy.ui.adapters.FamilyDetailsAdapter
import com.sumagoinfotech.digicopy.utils.MyValidator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LabourDetailsActivity2 : AppCompatActivity() {
    private lateinit var binding:ActivityLabourDetails2Binding
    lateinit var  etDob:AutoCompleteTextView
    lateinit var  etFullName:EditText
    lateinit var  actMaritalStatus:AutoCompleteTextView
    lateinit var  actRelationship:AutoCompleteTextView
    lateinit var  btnSubmit:Button
    var validationResults = mutableListOf<Boolean>()
    var familyDetailsList=ArrayList<FamilyDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutAdd.setOnClickListener {
            showAddFamilyDetailsDialog()
        }
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewFamilyDetails.layoutManager=layoutManager;
        var adapter=FamilyDetailsAdapter(familyDetailsList)
        binding.recyclerViewFamilyDetails.adapter=adapter
        binding.btnSubmit.setOnClickListener {
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
        actMaritalStatus.setOnClickListener {
            actMaritalStatus.showDropDown()
        }
        btnSubmit.setOnClickListener {

            if(validateFields())
            {
                val familyMember=FamilyDetails(fullName = etFullName.text.toString(), dob = etDob.text.toString(), relationship = actRelationship.text.toString(), maritalStatus = actMaritalStatus.text.toString())
                familyDetailsList.add(familyMember)

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
}