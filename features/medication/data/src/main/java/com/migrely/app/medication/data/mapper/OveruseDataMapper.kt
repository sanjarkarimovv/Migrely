package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel

class OveruseDataMapper {
    fun toData(category: OveruseCategoryDomainModel): OveruseCategoryDataModel = when (category) {
        OveruseCategoryDomainModel.SimplePainkiller -> OveruseCategoryDataModel.SimplePainkiller
        OveruseCategoryDomainModel.Triptan -> OveruseCategoryDataModel.Triptan
        OveruseCategoryDomainModel.CombinationOrOther -> OveruseCategoryDataModel.CombinationOrOther
        OveruseCategoryDomainModel.Multiple -> OveruseCategoryDataModel.Multiple
    }

    fun toData(level: OveruseLevelDomainModel): OveruseLevelDataModel = when (level) {
        OveruseLevelDomainModel.Soft -> OveruseLevelDataModel.Soft
        OveruseLevelDomainModel.Doctor -> OveruseLevelDataModel.Doctor
    }
}
