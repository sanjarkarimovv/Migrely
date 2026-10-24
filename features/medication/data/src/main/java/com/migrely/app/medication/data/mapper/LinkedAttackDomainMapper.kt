package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/** The attack an intake is logged for, with the zone it started in (BRD EC-TIM-02). */
class LinkedAttackDomainMapper {
    fun toDomain(attack: AttackDataModel) = LinkedAttackDomainModel(
        id = attack.id,
        start = attack.start,
        end = attack.end,
        zone = zoneOf(attack.startZoneId)
    )

    // A zone id saved on another device may be unknown here; UTC keeps the instant correct.
    @Suppress("SwallowedException")
    private fun zoneOf(id: String): ZoneId = try {
        ZoneId.of(id)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }
}
