package com.zhixue.lite.core.domain

import com.zhixue.lite.core.data.repository.ReportRepository
import com.zhixue.lite.core.model.data.ReportMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.roundToInt

class GetReportMainUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    operator fun invoke(examId: String): Flow<ReportMain> {
        return combine(
            reportRepository.getReportMain(examId),
            reportRepository.getSubjectDiagnosis(examId)
        ) { (paperList), (subjectDiagnosisList) ->

            val trendList = withContext(Dispatchers.IO) {
                paperList.map {
                    async {
                        runCatching {
                            reportRepository.getLevelTrend(examId, it.paperId)
                        }.getOrNull()
                    }
                }.awaitAll()
            }

            var totalScore = BigDecimal.ZERO
            var totalStandardScore = BigDecimal.ZERO

            val reportMainTrends = mutableListOf<ReportMain.Trend>()
            val reportMainOverviews = mutableListOf<ReportMain.Overview>()

            for (index in paperList.indices) {
                val paper = paperList[index]
                val trend = trendList[index]?.list?.firstOrNull()

                val name = paper.subjectName
                val score = paper.userScore.toBigDecimal()
                val standardScore = paper.standardScore.toBigDecimal()

                val rank = paper.clazzRank ?: trend?.effectiveTotalNum?.takeIf { it > 0 }?.let { totalNum ->
                    subjectDiagnosisList
                        .find { it.subjectCode == paper.subjectCode }
                        ?.let { calculateRank(totalNum, it.myRank) }
                }

                if (!paper.paperId.contains("!")) {
                    totalScore += score
                    totalStandardScore += standardScore
                }

                reportMainTrends.add(
                    ReportMain.Trend(
                        name = name,
                        code = trend?.improveBar?.tag?.code,
                        rank = rank
                    )
                )

                reportMainOverviews.add(
                    ReportMain.Overview(
                        id = paper.paperId,
                        name = name,
                        level = paper.userLevel ?: trend?.improveBar?.levelScale,
                        score = score.stripTrailingZeros().toPlainString(),
                        standardScore = standardScore.stripTrailingZeros().toPlainString(),
                        rate = score.toFloat() / standardScore.toFloat()
                    )
                )
            }

            val reportMainTotal = ReportMain.Total(
                score = totalScore.stripTrailingZeros().toPlainString(),
                standardScore = totalStandardScore.stripTrailingZeros().toPlainString(),
                rate = totalScore.toFloat() / totalStandardScore.toFloat(),
                rank = calculateTotalRank(trendList, subjectDiagnosisList)
            )

            ReportMain(
                total = reportMainTotal,
                trends = reportMainTrends,
                overviews = reportMainOverviews
            ).also {
                reportRepository.saveReportMain(examId, it)
            }
        }.catch {
            emit(reportRepository.getLocalReportMain(examId))
        }
    }

    /**
     * 计算总分班级排名：
     * 参考智学网等级评估体系（A级前0-20% 每档3%，B级前20-50% 每档3%，…），
     * 按各科 myRank（班级排名百分比）映射回名次区间后累加：
     * rank = Σ ceil(totalNum × levelUpperPercent(该科等级)) ，总排名 ≈ Σ学科排名 / 2（取整）。
     * 若某科缺等级或人数，则回退为百分比均值估算：rank = round(avgPercent/100 × (totalNum-1)) + 1。
     */
    private fun calculateTotalRank(
        trendList: List<com.zhixue.lite.core.model.network.LevelTrendResponse?>,
        subjectDiagnosisList: com.zhixue.lite.core.model.network.SubjectDiagnosisResponse
    ): Int? {
        val valid = trendList.mapIndexedNotNull { index, trend ->
            val info = trend?.list?.firstOrNull() ?: return@mapIndexedNotNull null
            val totalNum = info.effectiveTotalNum.takeIf { it > 0 } ?: return@mapIndexedNotNull null
            val diagnosis = subjectDiagnosisList.list.getOrNull(index) ?: return@mapIndexedNotNull null
            Triple(totalNum, diagnosis.myRank, info.improveBar.levelScale)
        }
        if (valid.isEmpty()) return null

        // 方案A：按等级区间估算每科名次后平均
        val byLevel = valid.mapNotNull { (totalNum, myRank, levelScale) ->
            val upper = levelUpperPercent(levelScale)
            if (upper != null) {
                val subjectRank = (totalNum * upper / 100.0).coerceAtLeast(1.0)
                subjectRank
            } else {
                null
            }
        }
        if (byLevel.size >= valid.size / 2) {
            val totalRank = (byLevel.sum() / byLevel.size * valid.size / 2).roundToInt()
            if (totalRank >= 1) return totalRank
        }

        // 方案B：按百分比均值估算
        val avgPercent = valid.map { it.second }.average()
        val totalNum = valid.map { it.first }.average().roundToInt()
        if (totalNum <= 0) return null
        return (avgPercent / 100.0 * (totalNum - 1)).roundToInt() + 1
    }

    /**
     * levelScale 形如 "A5"/"B3"/"C2"，返回该等级对应的"水平前百分比"上界（%值）。
     * A 档从 0% 开始，每档 3%；B 档 20%-50%；C 档 50%-75%；D 档 75%-90%；E 档 90%-100%。
     */
    private fun levelUpperPercent(levelScale: String?): Double? {
        if (levelScale.isNullOrEmpty() || levelScale.length < 2) return null
        val letter = levelScale[0].uppercaseChar()
        val tier = levelScale[1].digitToIntOrNull() ?: return null
        return when (letter) {
            'A' -> tier * 3.0                       // A5→15%, A4→12% … A1→3%（水平前 tier*3%）
            'B' -> 20 + (5 - tier) * 3.0            // B5→20%, B4→23% … B1→32%
            'C' -> 50 + (4 - tier) * 5.0            // C4→50%, C3→55% … C1→65%
            'D' -> 75 + (3 - tier) * 5.0            // D3→75% … D1→85%
            'E' -> 90 + (2 - tier) * 5.0            // E2→90% … E1→95%
            else -> null
        }
    }
}

private fun calculateRank(totalNum: Int, myRank: Double): Int {
    return (totalNum - (totalNum - 1) * (100 - myRank) / 100).roundToInt()
}
