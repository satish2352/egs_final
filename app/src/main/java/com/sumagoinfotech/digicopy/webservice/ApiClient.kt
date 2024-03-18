package com.sumagoinfotech.digicopy.webservice

import android.content.Context
import com.sumagoinfotech.digicopy.utils.MySharedPref
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://egswebfinal.sumagotest.in/api/"

    val loggingInterceptor = HttpLoggingInterceptor()

    private fun getAuthInterceptor(context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    fun create(context: Context): ApiService {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(getAuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}