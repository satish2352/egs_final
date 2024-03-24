package com.sumagoinfotech.digicopy.webservice

data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)