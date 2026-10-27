package com.migrely.app.onboarding.presentation.mapper

import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.presentation.model.AttackFrequencyPresentationModel

class AttackFrequencyDomainMapper {
    fun toDomain(frequency: AttackFrequencyPresentationModel) = when (frequency) {
        AttackFrequencyPresentationModel.FewerThanFour -> AttackFrequencyDomainModel.FewerThanFour
        AttackFrequencyPresentationModel.FourToFourteen -> AttackFrequencyDomainModel.FourToFourteen
        AttackFrequencyPresentationModel.FifteenOrMore -> AttackFrequencyDomainModel.FifteenOrMore
    }
}
