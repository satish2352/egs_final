package com.sumagoinfotech.digicopy.webservice

import com.sumagoinfotech.digicopy.model.apis.DocumentDownloadModel
import com.sumagoinfotech.digicopy.model.apis.LaboureEditDetailsOnline.LabourEditDetailsOnline
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceModel
import com.sumagoinfotech.digicopy.model.apis.documetqrdownload.QRDocumentDownloadModel
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.model.apis.login.LoginModel
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.model.apis.mapmarker.MapMarkerModel
import com.sumagoinfotech.digicopy.model.apis.maritalstatus.MaritalStatusModel
import com.sumagoinfotech.digicopy.model.apis.masters.MastersModel
import com.sumagoinfotech.digicopy.model.apis.projectlist.ProjectsFromLatLongModel
import com.sumagoinfotech.digicopy.model.apis.projectlistformap.ProjectListModel
import com.sumagoinfotech.digicopy.model.apis.projectlistmarker.ProjectLabourListForMarker
import com.sumagoinfotech.digicopy.model.apis.projectlistofficer.ProjectListForOfficerModel
import com.sumagoinfotech.digicopy.model.apis.reportscount.ReportsCount
import com.sumagoinfotech.digicopy.model.apis.skills.SkillsModel
import com.sumagoinfotech.digicopy.model.apis.update.LabourUpdateDetails
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
    fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("device_id") device_id: String,
    ): Call<LoginModel>

    @GET("auth/list-project")
    fun getProjectListForMap(): Call<ProjectListModel>

    @POST("auth/filter-project-labour-list")
    fun getProjectListForMarkerByNameSearch(
        @Query("project_name") projectName: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String

    ): Call<ProjectsFromLatLongModel>

    @POST("auth/filter-project-labour-list")
    fun getLabourDataForMarkerById(@Query("mgnrega_card_id") mgnrega_card_id: String): Call<ProjectLabourListForMarker>

    // Search Labour By MGNREGA ID

    @POST("auth/list-labour")
    fun getLabourDataByIdForAttendance(
        @Query("mgnrega_card_id") mgnrega_card_id: String,
        @Query("is_approved") param1: String = "approved"
    ): Call<LabourByMgnregaId>

    // changed to particular-labour-details = > list-labour
    @POST("auth/list-labour")
    fun getLabourDetailsById(@Query("mgnrega_card_id") mgnrega_card_id: String): Call<LabourByMgnregaId>

    // changed to list-user-labours=> list-labour
    @POST("auth/list-labour")
    fun getLaboursByProjectId(
        @Query("project_id") project_id: String,
        @Query("is_approved") param1: String = "approved"
    ): Call<LabourByMgnregaId>

    // GET project list for attendance page
    @POST("auth/filter-project-labour-list")
    fun getProjectList(): Call<ProjectLabourListForMarker>

    @POST("auth/project-list-for-officer")
    fun getProjectListForOfficer(): Call<ProjectListForOfficerModel>

    @POST("auth/filter-project-labour-list")
    fun getProjectList(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String
    ): Call<ProjectLabourListForMarker>

    @POST("auth/filter-project-labour-list")
    fun getProjectListForAttendance(
        @Query("want_project_data") param1: String = "yes",
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String
    ): Call<ProjectLabourListForMarker>


    // Mark Attendance
    @POST("auth/add-attendance-mark")
    fun markAttendance(
        @Query("project_id") projectId: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("attendance_day") attendanceDay: String
    ): Call<MastersModel>

    @POST("auth/update-attendance-mark")
    fun updateAttendance(
        @Query("project_id") projectId: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("attendance_day") attendanceDay: String
    ): Call<MastersModel>


    // get list attendance marked
    @POST("auth/list-attendance-marked")
    fun getListOfMarkedAttendance(
        @Query("project_id") projectId: String,
    ): Call<AttendanceModel>


    @POST("auth/list-attendance-marked-visible-for-officer")
    fun getAttendanceListForOfficer(
        @Query("project_id") projectId: String,
        @Query("user_taluka") talukaId: String,
        @Query("user_village") villageId: String,
        @Query("from_date") from_date: String,
        @Query("to_date") to_date: String,
    ): Call<AttendanceModel>


    @POST("auth/list-document-officer")
    fun getDocumentsListForOfficer(
        @Query("user_taluka") talukaId: String,
        @Query("user_village") villageId: String,
        @Query("from_date") from_date: String,
        @Query("to_date") to_date: String
    ): Call<UploadedDocsModel>


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
    ): Response<MastersModel>

    @POST("auth/update-labour-first-form")
    suspend fun updateLabourFirstForm(
        @Query("full_name") fullName: String,
        @Query("gender_id") genderId: String,
        @Query("date_of_birth") dateOfBirth: String,
        @Query("skill_id") skillId: String,
        @Query("village_id") villageId: String,
        @Query("taluka_id") talukaId: String,
        @Query("district_id") districtId: String,
        @Query("mobile_number") mobileNumber: String,
        @Query("mgnrega_card_id") mgnregaId: String,
        @Query("id") id: String,
        @Query("landline_number") landLineNumber: String
    ): Response<MastersModel>

    @Multipart
    @POST("auth/update-labour-second-form")
    suspend fun updateLabourFormTwo(
        @Query("id") id: String,
        @Query("family") family: String,
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
        @Part file1: MultipartBody.Part,
        @Part file2: MultipartBody.Part,
        @Part file3: MultipartBody.Part,
        @Part file4: MultipartBody.Part
    ): Response<MastersModel>

    @Multipart
    @POST("auth/add-document")
    suspend fun uploadDocument(
        @Query("document_type_id") documentId: String,
        @Query("document_name") documentName: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Part file: MultipartBody.Part
    ): Response<MastersModel>

    @POST("auth/list-document")
    fun getUploadedDocumentsList(): Call<UploadedDocsModel>

    // changed to list-send-approved-labour => list-labour
    @POST("auth/list-labour")
    fun getLaboursListSentForApproval(
        @Query("is_approved") param1: String = "added",
    ): Call<LabourListModel>

    // change to => list-not-approved-labour => list-labour
    @POST("auth/list-labour")
    fun getLaboursListNotApproved(
        @Query("is_approved") param1: String = "not_approved"
    ): Call<LabourListModel>

    @POST("auth/list-labour-rejected")
    fun getRejectedLabourList(): Call<LabourListModel>


    // changed to list-approved-labour => list-labour
    @POST("auth/list-labour")
    fun getLabourListApproved(
        @Query("is_approved") param1: String = "approved"
    ): Call<LabourListModel>


    @POST("auth/update-officer-labour-status-approved")
    fun sendApprovedLabourResponseToServer(
        @Query("is_approved") isApproved: String,
        @Query("labour_id") labour_id: String,
        @Query("mgnrega_card_id") mgnrega_card_id: String,
    ): Call<LabourListModel>

    @POST("auth/update-officer-labour-status-rejected")
    fun sendRejectedLabourStatusServer(
        @Query("is_approved") isApproved: String,
        @Query("mgnrega_card_id") mgnrega_card_id: String,
    ): Call<LabourListModel>


    @POST("auth/update-officer-labour-status-not-approved")
    fun sendNotApprovedLabourResponseToServer(
        @Query("labour_id") labour_id: String,
        @Query("is_approved") isApproved: String,
        @Query("reason_id") reason_id: String,
        @Query("other_remark") other_remark: String
    ): Call<LabourListModel>


    @POST("auth/list-labour-received-to-officer-for-approval")
    fun getListOfLaboursReceivedForApproval(): Call<LabourListModel>

    @POST("auth/list-labour-approved-by-officer")
    fun getListOfLaboursApprovedByOfficer(): Call<LabourListModel>

    @POST("auth/list-labour-not-approved-by-officer")
    fun getListOfLaboursNotApprovedByOfficer(): Call<LabourListModel>

    @POST("auth/list-labour-rejected-by-officer")
    fun getListOfLaboursRejectedByOfficer(): Call<LabourListModel>

    @POST("auth/list-particular-officer-labour-details")
    fun getLabourDetailsByIdForOfficer(@Query("mgnrega_card_id") mgnrega_card_id: String): Call<LabourByMgnregaId>

    // particular-labour-details-for-update => list-labour
    @POST("auth/list-labour")
    suspend fun getLabourDetailsForUpdate(@Query("mgnrega_card_id") mgnrega_card_id: String): Response<LabourUpdateDetails>

    // particular-labour-details-for-update => list-labour

    @POST("auth/list-labour")
    suspend fun getLabourDetailsForUpdate2(@Query("mgnrega_card_id") mgnrega_card_id: String): Response<LabourEditDetailsOnline>


    // project-list-lat-log => filter-project-labour-list
    @POST("auth/filter-project-labour-list")
    fun getProjectsListFromLatLong(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
    ): Call<ProjectsFromLatLongModel>

    @POST("auth/filter-project-labour-list")
    fun getMapsMarkersFromLatLong(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
    ): Call<MapMarkerModel>


    @POST("auth/officer-count-labour")
    fun getLaboursReportCount(): Call<ReportsCount>

    @POST("auth/gramsevak-count-labour")
    fun getReportCountInGramsevakLogin(): Call<ReportsCount>


    // pdf download from qr
    @POST("auth/download-document")
    fun downloadPDF(
        @Query("document_name") document_pdf: String,
    ): Call<QRDocumentDownloadModel>


    // get uploaded docs count for gramsevak
    @POST("auth/count-gramsevak-document")
    fun getUploadedDocsCountForGramsevak(): Call<ReportsCount>

    @POST("auth/list-document")
    fun getSentForApprovalDocsList(
        @Query("is_approved") param1: String = "added",
        ): Call<MainDocsModel>

    @POST("auth/list-document")
    fun getApprovalDocsListForGramsevak(
        @Query("is_approved") param1: String = "approved",
    ): Call<MainDocsModel>

    @POST("auth/list-document")
    fun getNotApprovedDocsListForGramsevak(
        @Query("is_approved") param1: String = "not_approved",
    ): Call<MainDocsModel>


}