package com.sumagoinfotech.digicopy.webservice

import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusModel
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import com.sumagoinfotech.digicopy.model.apis.uploadeddocs.UploadedDocsModel
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

    // Search Labour By MGNREGA ID

    @POST("auth/list-labour")
    fun getLabourDataById(@Query("mgnrega_card_id")mgnrega_card_id:String): Call<LabourByMgnregaId>
    @POST("auth/particular-labour-details")
    fun getLabourDetailsById(@Query("mgnrega_card_id")mgnrega_card_id:String): Call<LabourByMgnregaId>

    @POST("auth/list-user-labours")
    fun getLaboursByProject(@Query("user_id")user_id:String): Call<LabourByMgnregaId>

    // GET project list for attendance page
    @POST("auth/filter-project-labour-list")
    fun getProjectList(): Call<ProjectLabourListForMarker>

    // Mark Attendance
    @POST("auth/add-attendance-mark")
    fun markAttendance(
        @Query("project_id") projectId: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("attendance_day") attendanceDay: String):Call<MastersModel>

    @POST("auth/update-attendance-mark")
    fun updateAttendance(
        @Query("project_id") projectId: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("attendance_day") attendanceDay: String):Call<MastersModel>



    // get list attendance marked
    @POST("auth/list-attendance-marked")
    fun getListOfMarkedAttendance(
        @Query("project_id") projectId: String,
    ):Call<AttendanceModel>




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

    @Multipart
    @POST("auth/add-document")
    suspend fun uploadDocument(
        @Query("document_type_id") documentId: String,
        @Query("document_name") documentName: String,
        @Part file: MultipartBody.Part
    ):Response<MastersModel>

    @POST("auth/list-document")
    fun getUploadedDocumentsList():Call<UploadedDocsModel>


    @POST("auth/list-send-approved-labour")
    fun getLaboursListSentForApproval():Call<LabourListModel>


    @POST("auth/list-not-approved-labour")
    fun getLaboursListNotApproved():Call<LabourListModel>

    @POST("auth/list-approved-labour")
    fun getLabourListApproved():Call<LabourListModel>


    @POST("auth/update-labour-status-approved")
    fun sendApprovedLabourResponseToServer(
        @Query("is_approved")isApproved:String,
        @Query("mgnrega_card_id")mgnrega_card_id:String,
    ):Call<LabourListModel>

    @POST("auth/update-labour-status-not-approved")
    fun sendNotApprovedLabourResponseToServer(
        @Query("mgnrega_card_id")mgnrega_card_id:String,
        @Query("is_approved")isApproved:String,
        @Query("reason_id")reason_id:String,
        @Query("other_remark")other_remark:String
        ):Call<LabourListModel>







    






}