package com.migrely.app.datasource.backup.model

import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

/** Every health row, read or written in one transaction. It is not a table. */
data class BackupLocalModel(
    val attacks: List<AttackLocalModel>,
    val intensityPoints: List<IntensityPointLocalModel>,
    val attackTags: List<AttackTagLocalModel>,
    val customTags: List<CustomTagLocalModel>,
    val medications: List<MedicationLocalModel>,
    val intakes: List<MedicationIntakeLocalModel>,
    val dayMarks: List<DayMarkLocalModel>
)
