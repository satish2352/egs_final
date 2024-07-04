package com.sipl.egs2.model.apis.profile

data class ProfileModel(
    val `data`: List<Data>,
    val message: String,
    val status: String
)