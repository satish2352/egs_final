package com.sumagoinfotech.digicopy.webservice

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import retrofit2.Call

class CallAdapterFactory : TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType == Call::class.java) {
            return CallAdapter() as TypeAdapter<T>
        }
        return null
    }
}