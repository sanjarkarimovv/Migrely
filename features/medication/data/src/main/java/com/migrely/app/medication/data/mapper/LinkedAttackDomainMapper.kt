package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel

/** The attack an intake is logged for, with the zone it started in (BRD EC-TIM-02). */
class LinkedAttackDomainMapper(private val zoneDomainMapper: ZoneDomainMapper) {
    fun toDomain(attack: AttackDataModel) = LinkedAttackDomainModel(
        id = attack.id,
        start = attack.start,
        end = attack.end,
        zone = zoneDomainMapper.toDomain(attack.startZoneId)
    )
}
