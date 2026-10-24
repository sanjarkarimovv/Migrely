package com.migrely.app.onboarding.data.mapper

import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel

class AttackFrequencyDataMapper {
    fun toData(frequency: AttackFrequencyDomainModel) = when (frequency) {
        AttackFrequencyDomainModel.FewerThanFour -> AttackFrequencyDataModel.FewerThanFour
        AttackFrequencyDomainModel.FourToFourteen -> AttackFrequencyDataModel.FourToFourteen
        AttackFrequencyDomainModel.FifteenOrMore -> AttackFrequencyDataModel.FifteenOrMore
    }
}
