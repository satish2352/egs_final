package com.sumagoinfotech.digicopy.model.apis.getlabour

import androidx.room.PrimaryKey

data class FamilyDetail(
    var created_at: String?=null,
    var date_of_birth: String,
    var full_name: String,
    var gender_id: String,
    var id: Int?=null,
    var is_active: Int?=null,
    var is_deleted: Int?=null,
    var labour_id: Int?=null,
    var married_status_id: String,
    var relationship_id: String,
    var updated_at: String?=null,
    var gender: String? = null,
    var relation: String? = null,
    var maritalStatus: String? = null

    )