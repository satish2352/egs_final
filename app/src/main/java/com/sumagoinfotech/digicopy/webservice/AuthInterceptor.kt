package com.sumagoinfotech.digicopy.webservice

import android.content.Context
import com.sumagoinfotech.digicopy.utils.MySharedPref
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getTokenFromSharedPreferences()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }

    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = MySharedPref(context)
        return sharedPreferences.getRememberToken()
    }
}