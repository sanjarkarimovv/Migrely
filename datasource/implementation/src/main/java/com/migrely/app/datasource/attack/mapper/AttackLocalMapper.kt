package com.migrely.app.datasource.attack.mapper

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import java.time.Instant

class AttackLocalMapper(
    private val attackTagLocalMapper: AttackTagLocalMapper = AttackTagLocalMapper()
) {
    fun toData(local: AttackWithPointsLocalModel): AttackDataModel = AttackDataModel(
        id = local.attack.id,
        start = Instant.ofEpochMilli(local.attack.startEpochMillis),
        startZoneId = local.attack.startZoneId,
        end = local.attack.endEpochMillis?.let(Instant::ofEpochMilli),
        intensityPoints = local.intensityPoints
            .sortedWith(compareBy({ it.atEpochMillis }, { it.id }))
            .map { point ->
                IntensityPointDataModel(Instant.ofEpochMilli(point.atEpochMillis), point.value)
            },
        longCheckConfirmedAt = local.attack.longCheckConfirmedEpochMillis?.let(
            Instant::ofEpochMilli
        ),
        longCheckDisabled = local.attack.longCheckDisabled,
        tags = local.tags.mapNotNull(attackTagLocalMapper::toData),
        functionalImpact = local.attack.functionalImpact,
        note = local.attack.note
    )
}
