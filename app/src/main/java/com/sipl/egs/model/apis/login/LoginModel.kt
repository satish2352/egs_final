package com.sipl.egs.model.apis.login

data class LoginModel(
    val `data`: Data,
    val status: String,
    val message: String,
    val token_type: String
)