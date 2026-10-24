package com.migrely.app.datasource.attack.model

import androidx.room.Embedded
import androidx.room.Relation

data class AttackWithPointsLocalModel(
    @Embedded val attack: AttackLocalModel,
    @Relation(parentColumn = "id", entityColumn = "attack_id")
    val intensityPoints: List<IntensityPointLocalModel>
)
