package com.sipl.egs.model.apis.profile

data class ProfileModel(
    val `data`: List<Data>,
    val message: String,
    val status: String
)