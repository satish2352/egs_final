package com.sumagoinfotech.digicopy.webservice

import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // masters initial
    @GET("auth/list-skills")
    fun getSkills(): Call<SkillsModel>

    @GET("auth/list-maritalstatus")
    fun getMaritalStatus(): Call<MaritalStatusModel>


    @POST("login")
    fun loginUser(@Query("email")email:String,
                  @Query("password")password:String,): Call<LoginModel>
    @GET("auth/list-project")
    fun getProjectListForMap(): Call<ProjectListModel>
    @POST("auth/filter-project-labour-list")
    fun getProjectListForMarker(@Query("project_name")projectName:String): Call<ProjectLabourListForMarker>

    @POST("auth/filter-project-labour-list")
    fun getLabourForMarker(@Query("mgnrega_card_id")mgnrega_card_id:String): Call<ProjectLabourListForMarker>

    @POST("auth/list-labour")
    fun getLabourDataById(@Query("mgnrega_card_id")mgnrega_card_id:String): Call<LabourByMgnregaId>

    @POST("auth/list-user-labours")
    fun getLaboursByProject(@Query("user_id")user_id:String): Call<LabourByMgnregaId>




}