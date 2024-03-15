package com.sumagoinfotech.digicopy.webservice

import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    fun loginUser(@Query("email")email:String,
                  @Query("password")password:String,): Call<LoginModel>


    @GET("auth/list-project")
    fun getProjectListForMap(): Call<ProjectListModel>




}