package com.zhixue.lite.core.network

import com.zhixue.lite.core.model.network.AcademicYearResponse
import com.zhixue.lite.core.model.network.CasResponse
import com.zhixue.lite.core.model.network.ContactStudent
import com.zhixue.lite.core.model.network.ContactTeacher
import com.zhixue.lite.core.model.network.ErrorBookResponse
import com.zhixue.lite.core.model.network.ExamPointsResponse
import com.zhixue.lite.core.model.network.HwBankParams
import com.zhixue.lite.core.model.network.HwBase
import com.zhixue.lite.core.model.network.HwParams
import com.zhixue.lite.core.model.network.HomeworkAnswerRequest
import com.zhixue.lite.core.model.network.HomeworkAnswerResponse
import com.zhixue.lite.core.model.network.HomeworkBankRequest
import com.zhixue.lite.core.model.network.HomeworkListResponse
import com.zhixue.lite.core.model.network.CheckSheetResponse
import com.zhixue.lite.core.model.network.LevelTrendResponse
import com.zhixue.lite.core.model.network.NewVersionCodeResponse
import com.zhixue.lite.core.model.network.PageAllExamListResponse
import com.zhixue.lite.core.model.network.PaperAnalysisResponse
import com.zhixue.lite.core.model.network.ReportMainResponse
import com.zhixue.lite.core.model.network.SsoResponse
import com.zhixue.lite.core.model.network.SubjectDiagnosisResponse
import com.zhixue.lite.core.model.network.UserInfoResponse
import com.zhixue.lite.core.network.api.ChangYanApi
import com.zhixue.lite.core.network.api.GiteeApi
import com.zhixue.lite.core.network.api.ZhixueApi
import javax.inject.Inject

class ApiNetworkDataSource @Inject constructor(
    private val giteeApi: GiteeApi,
    private val zhixueApi: ZhixueApi,
    private val changYanApi: ChangYanApi,
    private val authInterceptor: AuthInterceptor
) : NetworkDataSource {

    @Volatile
    var tokenRefreshListener: TokenRefreshListener? = null

    /**
     * 带失效续期的请求包装：errorCode 3002/50001 类未登录错误时静默重登录后重试一次。
     */
    private suspend fun <T> withAutoRelogin(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: com.zhixue.lite.core.model.network.TokenExpiredError) {
            val refreshed = tokenRefreshListener?.onTokenExpired() ?: false
            if (refreshed) {
                block()
            } else {
                throw e
            }
        }
    }

    override suspend fun getNewVersionCode(): NewVersionCodeResponse {
        return giteeApi.getNewVersionCode()
    }

    override suspend fun ssoLogin(username: String, password: String): SsoResponse {
        return changYanApi.ssoLogin(username, password).data
    }

    override suspend fun casLogin(at: String, userId: String): CasResponse {
        return zhixueApi.casLogin(at, userId).result!!
    }

    override suspend fun getUserInfo(token: String): UserInfoResponse {
        return withAutoRelogin { zhixueApi.getUserInfo(token).result!! }
    }

    override suspend fun modifyPassword(
        loginName: String, originPassword: String, newPassword: String, token: String
    ) {
        zhixueApi.modifyPassword(loginName, originPassword, newPassword, token)
    }

    override suspend fun getPageAllExamList(
        reportType: String, pageIndex: Int, token: String,
        startSchoolYear: String?, endSchoolYear: String?
    ): PageAllExamListResponse {
        return withAutoRelogin {
            zhixueApi.getPageAllExamList(
                reportType, pageIndex, token,
                startSchoolYear, endSchoolYear
            ).result!!
        }
    }

    override suspend fun getReportMain(examId: String, token: String): ReportMainResponse {
        return withAutoRelogin { zhixueApi.getReportMain(examId, token).result!! }
    }

    override suspend fun getSubjectDiagnosis(
        examId: String, token: String
    ): SubjectDiagnosisResponse {
        return zhixueApi.getSubjectDiagnosis(examId, token).result!!
    }

    override suspend fun getLevelTrend(
        examId: String, paperId: String, token: String
    ): LevelTrendResponse {
        return zhixueApi.getLevelTrend(examId, paperId, token).result!!
    }

    override suspend fun getCheckSheet(
        examId: String, paperId: String, token: String
    ): CheckSheetResponse {
        return zhixueApi.getCheckSheet(examId, paperId, token).result!!
    }

    override suspend fun getPaperAnalysis(paperId: String, token: String): PaperAnalysisResponse {
        return zhixueApi.getPaperAnalysis(paperId, token).result!!
    }


    override suspend fun getErrorBook(
        examId: String, paperId: String, token: String
    ): ErrorBookResponse {
        return zhixueApi.getErrorBook(examId, paperId, token).result
            ?: throw IllegalStateException("错题本数据为空")
    }

    override suspend fun getAcademicYear(token: String): List<AcademicYearResponse> {
        return zhixueApi.getAcademicYear(token).result ?: emptyList()
    }

    override suspend fun getExamLevelTrend(examId: String, token: String): LevelTrendResponse {
        return zhixueApi.getExamLevelTrend(examId, token).result
            ?: throw IllegalStateException("年级趋势数据为空")
    }

    override suspend fun getClassmates(
        userId: String, clazzId: String, token: String
    ): List<ContactStudent> {
        return zhixueApi.getClassmates(r = "${userId}student", clazzId = clazzId, token = token)
    }

    override suspend fun getTeachers(
        userId: String, clazzId: String, token: String
    ): List<ContactTeacher> {
        return zhixueApi.getTeachers(r = "${userId}student", clazzId = clazzId, token = token)
    }

    override suspend fun getHomeworkList(
        pageIndex: Int, completeStatus: Int, pageSize: Int, token: String
    ): HomeworkListResponse {
        val xToken = authInterceptor.getXToken()
        return zhixueApi.getHomeworkList(
            pageIndex = pageIndex,
            completeStatus = completeStatus,
            pageSize = pageSize,
            token = xToken
        ).result ?: HomeworkListResponse()
    }

    override suspend fun getHomeworkAnswer(
        hwId: String, classId: String, typeCode: Int, userId: String
    ): HomeworkAnswerResponse {
        val xToken = authInterceptor.getXToken()
        return when (typeCode) {
            105 -> zhixueApi.getHomeworkExerciseAnswer(
                body = HomeworkAnswerRequest(
                    base = HwBase(appId = "WNLOIVE", udid = userId),
                    params = HwParams(hwId = hwId)
                ),
                authorization = xToken
            ).result ?: HomeworkAnswerResponse()
            102 -> zhixueApi.getHomeworkBankAnswer(
                body = HomeworkBankRequest(
                    base = HwBase(appId = "OAXI57PG", udid = userId),
                    params = HwBankParams(classId = classId, hwId = hwId)
                ),
                authorization = xToken
            ).result ?: HomeworkAnswerResponse()
            else -> throw IllegalStateException("该作业类型暂不支持查看答案")
        }
    }


    override suspend fun getExamPoints(
        examId: String, paperId: String, token: String
    ): ExamPointsResponse {
        return runCatching {
            zhixueApi.getExamPointsAndScoringAbility(examId, paperId, token).result
        }.getOrNull() ?: ExamPointsResponse()
    }
}
