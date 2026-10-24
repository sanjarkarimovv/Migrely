package com.migrely.app.datasource.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.tag.dao.TagDao
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

/**
 * Version history (each schema is committed under `schemas/` and every migration has an instrumented test):
 * 1. attacks and intensity points (change add-attack-logging);
 * 2. impact, note and deletion time on attacks, the `attack_tag` and `custom_tag` tables (change add-attack-details);
 * 3. the `medication` and `medication_intake` tables (change add-medication-tracking).
 */
@Database(
    entities = [
        AttackLocalModel::class,
        IntensityPointLocalModel::class,
        AttackTagLocalModel::class,
        CustomTagLocalModel::class,
        MedicationLocalModel::class,
        MedicationIntakeLocalModel::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)]
)
abstract class MigrelyDatabase : RoomDatabase() {
    abstract fun attackDao(): AttackDao

    abstract fun tagDao(): TagDao

    abstract fun medicationDao(): MedicationDao

    companion object {
        const val NAME = "migrely.db"
    }
}
