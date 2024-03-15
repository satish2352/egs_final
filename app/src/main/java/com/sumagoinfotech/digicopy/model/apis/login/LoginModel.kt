package com.sumagoinfotech.digicopy.model.apis.login

import com.sumagoinfotech.digicopy.model.apis.login.Data

data class LoginModel(
    val `data`: Data,
    val status: String,
    val token_type: String
)