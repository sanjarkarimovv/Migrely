package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticeDomainModel

class OveruseNoticeDomainMapper {
    fun toDomain(notice: OveruseNoticeDataModel) = OveruseNoticeDomainModel(
        category = when (notice.category) {
            OveruseCategoryDataModel.SimplePainkiller -> OveruseCategoryDomainModel.SimplePainkiller
            OveruseCategoryDataModel.Triptan -> OveruseCategoryDomainModel.Triptan
            OveruseCategoryDataModel.CombinationOrOther ->
                OveruseCategoryDomainModel.CombinationOrOther
            OveruseCategoryDataModel.Multiple -> OveruseCategoryDomainModel.Multiple
        },
        level = when (notice.level) {
            OveruseLevelDataModel.Soft -> OveruseLevelDomainModel.Soft
            OveruseLevelDataModel.Doctor -> OveruseLevelDomainModel.Doctor
        },
        acknowledgedAt = notice.acknowledgedAt
    )
}
