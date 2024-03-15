package com.sumagoinfotech.digicopy.webservice

import android.content.Context
import com.sumagoinfotech.digicopy.utils.MySharedPref
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://egswebfinal.sumagotest.in/api/"
    private fun getAuthInterceptor(context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(getAuthInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}