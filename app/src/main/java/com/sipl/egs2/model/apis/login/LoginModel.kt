package com.sipl.egs2.model.apis.login

data class LoginModel(
    val `data`: Data,
    val status: String,
    val message: String,
    val token_type: String
)