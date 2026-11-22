package com.zhixue.lite.core.network.api

import com.zhixue.lite.core.model.network.AcademicYearResponse
import com.zhixue.lite.core.model.network.CasResponse
import com.zhixue.lite.core.model.network.ContactStudent
import com.zhixue.lite.core.model.network.ContactTeacher
import com.zhixue.lite.core.model.network.ErrorBookResponse
import com.zhixue.lite.core.model.network.HomeworkAnswerRequest
import com.zhixue.lite.core.model.network.HomeworkAnswerResponse
import com.zhixue.lite.core.model.network.HomeworkBankRequest
import com.zhixue.lite.core.model.network.HomeworkListResponse
import com.zhixue.lite.core.model.network.CheckSheetResponse
import com.zhixue.lite.core.model.network.LevelTrendResponse
import com.zhixue.lite.core.model.network.PageAllExamListResponse
import com.zhixue.lite.core.model.network.PaperAnalysisResponse
import com.zhixue.lite.core.model.network.ReportMainResponse
import com.zhixue.lite.core.model.network.SubjectDiagnosisResponse
import com.zhixue.lite.core.model.network.UserInfoResponse
import com.zhixue.lite.core.model.network.ZhixueResponse
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 2025-11 实测修正：
 * - 报告类接口已由 zxbReport 路径迁移到 zhixuebao 路径，并改为 GET + query 参数；
 * - 所有请求经 AuthInterceptor 自动附加 authguid/authtimestamp/authtoken/authbizcode(+XToken) 头；
 * - token 字段保留传递，兼容 parent 端 token 鉴权。
 */
interface ZhixueApi {

    @FormUrlEncoded
    @POST("container/app/login/casLogin")
    suspend fun casLogin(
        @Field("at") at: String,
        @Field("userId") userId: String
    ): ZhixueResponse<CasResponse>

    @FormUrlEncoded
    @POST("zxbReport/base/common/getUserInfo")
    suspend fun getUserInfo(
        @Field("token") token: String
    ): ZhixueResponse<UserInfoResponse>

    @FormUrlEncoded
    @POST("container/app/modifyOriginPWD")
    suspend fun modifyPassword(
        @Field("loginName") loginName: String,
        @Field("originPWD") originPassword: String,
        @Field("newPWD") newPassword: String,
        @Field("token") token: String
    ): ZhixueResponse<Unit>

    @FormUrlEncoded
    @POST("zxbReport/report/getPageAllExamList")
    suspend fun getPageAllExamList(
        @Field("reportType") reportType: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("token") token: String,
        @Field("startSchoolYear") startSchoolYear: String? = null,
        @Field("endSchoolYear") endSchoolYear: String? = null,
        @FieldMap fields: Map<String, String> = mapOf(
            "pageSize" to "10",
            "actualPosition" to "0"
        )
    ): ZhixueResponse<PageAllExamListResponse>

    @GET("zhixuebao/report/exam/getReportMain")
    suspend fun getReportMain(
        @Query("examId") examId: String,
        @Query("token") token: String
    ): ZhixueResponse<ReportMainResponse>

    @GET("zhixuebao/report/exam/getSubjectDiagnosis")
    suspend fun getSubjectDiagnosis(
        @Query("examId") examId: String,
        @Query("token") token: String
    ): ZhixueResponse<SubjectDiagnosisResponse>

    @GET("zhixuebao/report/paper/getLevelTrend")
    suspend fun getLevelTrend(
        @Query("examId") examId: String,
        @Query("paperId") paperId: String,
        @Query("token") token: String,
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 2
    ): ZhixueResponse<LevelTrendResponse>

    @GET("zhixuebao/report/checksheet/")
    suspend fun getCheckSheet(
        @Query("examId") examId: String,
        @Query("paperId") paperId: String,
        @Query("token") token: String
    ): ZhixueResponse<CheckSheetResponse>

    @GET("zhixuebao/report/paper/getPaperAnalysis")
    suspend fun getPaperAnalysis(
        @Query("paperId") paperId: String,
        @Query("token") token: String
    ): ZhixueResponse<PaperAnalysisResponse>

    @GET("zhixuebao/report/paper/getLostTopicAndAnalysis")
    suspend fun getErrorBook(
        @Query("examId") examId: String,
        @Query("paperId") paperId: String,
        @Query("token") token: String
    ): ZhixueResponse<ErrorBookResponse>

    @GET("zhixuebao/base/common/academicYear")
    suspend fun getAcademicYear(
        @Query("token") token: String
    ): ZhixueResponse<List<AcademicYearResponse>>

    @GET("zhixuebao/report/exam/getLevelTrend")
    suspend fun getExamLevelTrend(
        @Query("examId") examId: String,
        @Query("token") token: String,
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 1
    ): ZhixueResponse<LevelTrendResponse>

    @GET("container/contact/student/students")
    suspend fun getClassmates(
        @Query("r") r: String,
        @Query("clazzId") clazzId: String,
        @Query("token") token: String
    ): List<ContactStudent>

    @GET("container/contact/student/teachers")
    suspend fun getTeachers(
        @Query("r") r: String,
        @Query("clazzId") clazzId: String,
        @Query("token") token: String
    ): List<ContactTeacher>

    @GET("https://mhw.zhixue.com/homework_middle_service/stuapp/getStudentHomeWorkList")
    suspend fun getHomeworkList(
        @Query("pageIndex") pageIndex: Int,
        @Query("completeStatus") completeStatus: Int,
        @Query("pageSize") pageSize: Int,
        @Query("subjectCode") subjectCode: String = "-1",
        @Query("token") token: String,
        @Query("createTime") createTime: Int = 0
    ): ZhixueResponse<HomeworkListResponse>

    @POST("https://mhw.zhixue.com/hw/manage/homework/redeploy")
    suspend fun getHomeworkExerciseAnswer(
        @Body body: HomeworkAnswerRequest,
        @Header("Authorization") authorization: String
    ): ZhixueResponse<HomeworkAnswerResponse>

    @POST("https://mhw.zhixue.com/hwreport/question/listView")
    suspend fun getHomeworkBankAnswer(
        @Body body: HomeworkBankRequest,
        @Header("Authorization") authorization: String
    ): ZhixueResponse<HomeworkAnswerResponse>
}
