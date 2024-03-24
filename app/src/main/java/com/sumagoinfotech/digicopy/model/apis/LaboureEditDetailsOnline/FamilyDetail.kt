package com.sumagoinfotech.digicopy.model.apis.LaboureEditDetailsOnline

data class FamilyDetail(
    var date_of_birth: String,
    var full_name: String,
    var gender_id: String,
    var id: Int?=null,
    var married_status_id: String,
    var relationship_id: String,
    var gender: String,
    var relation: String,
    var maritalStatus: String,
)