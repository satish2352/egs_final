package com.sumagoinfotech.digicopy.webservice

import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusModel
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import javax.annotation.Nullable

interface ApiService {

    // masters initial
    @POST("list-masters")
    fun getAllMasters(): Call<MastersModel>


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


    @POST("auth/filter-project-labour-list")
    fun getProjectList(): Call<ProjectLabourListForMarker>




    @Multipart
    @POST("auth/add-labour")
    suspend fun uploadLaborInfo(
        @Query("full_name") fullName: String,
        @Query("gender_id") genderId: String,
        @Query("date_of_birth") dateOfBirth: String,
        @Query("skill_id") skillId: String,
        @Query("village_id") villageId: String,
        @Query("taluka_id") talukaId: String,
        @Query("district_id") districtId: String,
        @Query("mobile_number") mobileNumber: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("landline_number") landLineNumber: String,
        @Query("family") family: String,
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
        @Part file1: MultipartBody.Part,
        @Part file2: MultipartBody.Part,
        @Part file3: MultipartBody.Part,
        @Part file4: MultipartBody.Part
    ):Response<MastersModel>


    @POST("auth/add-labour")
    suspend fun uploadLaborInfo2(): Call<ApiResponse<Any>>



    @POST("auth/add-labour")
    suspend fun uploadLaborInfoNew(
        @Query("full_name") full_name: String,
        @Query("gender_id") gender_id: String,
        @Query("date_of_birth") date_of_birth: String,
        @Query("skill_id") skill_id: String,
        @Query("village_id") village_id: String,
        @Query("taluka_id") taluka_id: String,
        @Query("district_id") district_id: String,
        @Query("mobile_number") mobile_number: String,
        @Query("mgnrega_card_id") mgnrega_card_id: String,
        @Query("landline_number") landline_number: String,
        @Query("family") family: String,
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
    ):Call<MastersModel>





}