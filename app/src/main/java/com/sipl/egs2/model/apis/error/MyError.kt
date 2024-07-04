package com.sipl.egs2.model.apis.error

data class MyError(
    val code: Int,
    val message: String,
    val status: String
)