package com.sipl.egs2.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sipl.egs2.utils.LabourInputData

class RegistrationViewModel : ViewModel() {
    var fullName: String = "defaultttttttttttt"
    var gender: String = ""
    var dateOfBirth: String = ""
    var district: String = ""
    var taluka: String = ""
    var village: String = ""
    var mobile: String = ""
    var landline: String = ""
    var idCard: String = ""

    private val _dataObject = MutableLiveData<LabourInputData>()
    val dataObject: LiveData<LabourInputData> = _dataObject

    // Define other fields similarly

    fun setData(data: LabourInputData) {
        _dataObject.value = data
    }

}