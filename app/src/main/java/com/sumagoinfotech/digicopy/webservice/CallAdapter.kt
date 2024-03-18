package com.sumagoinfotech.digicopy.webservice

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import retrofit2.Call

class CallAdapter : TypeAdapter<Call<*>>() {
    override fun write(out: JsonWriter, value: Call<*>?) {
        // Not implemented
    }

    override fun read(input: JsonReader): Call<*>? {
        // Implement your logic here
        // For example, you can use OkHttpClient to execute the call
        return null
    }
}