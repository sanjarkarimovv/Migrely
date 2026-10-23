package com.migrely.app.home.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

class AttackDomainMapper(private val intensityPointDomainMapper: IntensityPointDomainMapper) {
    fun toDomain(attack: AttackDataModel) = AttackDomainModel(
        id = attack.id,
        start = attack.start,
        startZone = zoneOf(attack.startZoneId),
        end = attack.end,
        intensityPoints = attack.intensityPoints.map(intensityPointDomainMapper::toDomain),
        longAttackCheck = LongAttackCheckDomainModel(
            confirmedAt = attack.longCheckConfirmedAt,
            disabled = attack.longCheckDisabled
        )
    )

    // A zone id saved on another device may be unknown to this device's time-zone data; UTC keeps the instant correct.
    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }
}
