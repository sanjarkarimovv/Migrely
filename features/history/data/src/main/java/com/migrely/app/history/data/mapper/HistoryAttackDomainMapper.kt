package com.migrely.app.history.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel

/** The peak is the highest pain entry, or null when none was recorded (BRD ATK-F4, EC-ATK-08). */
class HistoryAttackDomainMapper(private val zoneDomainMapper: ZoneDomainMapper) {
    fun toDomain(attack: AttackDataModel) = HistoryAttackDomainModel(
        id = attack.id,
        start = attack.start,
        startZone = zoneDomainMapper.toDomain(attack.startZoneId),
        end = attack.end,
        peakIntensity = attack.intensityPoints.maxOfOrNull { it.value }
    )
}
