package com.migrely.app.datasource.medication.mapper

import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import java.time.Instant

private const val SIMPLE = "SIMPLE"
private const val TRIPTAN = "TRIPTAN"
private const val COMBINATION_OTHER = "COMBINATION_OTHER"
private const val MULTIPLE = "MULTIPLE"
private const val SOFT = "SOFT"
private const val DOCTOR = "DOCTOR"

/** Stored keys never change; a row with a key this version does not know is skipped (BRD EC-MED-06). */
class OveruseNoticeLocalMapper {
    fun toData(local: OveruseNoticeLocalModel): OveruseNoticeDataModel? {
        val category = when (local.category) {
            SIMPLE -> OveruseCategoryDataModel.SimplePainkiller
            TRIPTAN -> OveruseCategoryDataModel.Triptan
            COMBINATION_OTHER -> OveruseCategoryDataModel.CombinationOrOther
            MULTIPLE -> OveruseCategoryDataModel.Multiple
            else -> return null
        }
        val level = when (local.level) {
            SOFT -> OveruseLevelDataModel.Soft
            DOCTOR -> OveruseLevelDataModel.Doctor
            else -> return null
        }
        return OveruseNoticeDataModel(
            category,
            level,
            Instant.ofEpochMilli(local.acknowledgedEpochMillis)
        )
    }

    fun toLocal(notice: OveruseNoticeDataModel) = OveruseNoticeLocalModel(
        category = when (notice.category) {
            OveruseCategoryDataModel.SimplePainkiller -> SIMPLE
            OveruseCategoryDataModel.Triptan -> TRIPTAN
            OveruseCategoryDataModel.CombinationOrOther -> COMBINATION_OTHER
            OveruseCategoryDataModel.Multiple -> MULTIPLE
        },
        level = when (notice.level) {
            OveruseLevelDataModel.Soft -> SOFT
            OveruseLevelDataModel.Doctor -> DOCTOR
        },
        acknowledgedEpochMillis = notice.acknowledgedAt.toEpochMilli()
    )
}
