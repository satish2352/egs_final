package com.sipl.egs.model.apis.error

data class MyError(
    val code: Int,
    val message: String,
    val status: String
)