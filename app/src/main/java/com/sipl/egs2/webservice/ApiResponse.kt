package com.sipl.egs2.webservice

data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)