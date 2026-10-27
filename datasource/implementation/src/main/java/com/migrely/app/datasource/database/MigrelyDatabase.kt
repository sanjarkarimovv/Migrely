package com.migrely.app.datasource.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.backup.dao.BackupDao
import com.migrely.app.datasource.daymark.dao.DayMarkDao
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import com.migrely.app.datasource.reminder.dao.ReminderSettingsDao
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel
import com.migrely.app.datasource.tag.dao.TagDao
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

/**
 * Version history (each schema is committed under `schemas/` and every migration has an instrumented test):
 * 1. attacks and intensity points (change add-attack-logging);
 * 2. impact, note and deletion time on attacks, the `attack_tag` and `custom_tag` tables (change add-attack-details);
 * 3. the `medication` and `medication_intake` tables (change add-medication-tracking);
 * 4. `effect` and `effect_closed` on intakes, the `overuse_notice` table (change add-medication-effect-and-overuse);
 * 5. the `day_mark` table (change add-day-markers).
 * 6. the `reminder_settings` table (change add-reminders).
 */
@Database(
    entities = [
        AttackLocalModel::class,
        IntensityPointLocalModel::class,
        AttackTagLocalModel::class,
        CustomTagLocalModel::class,
        MedicationLocalModel::class,
        MedicationIntakeLocalModel::class,
        OveruseNoticeLocalModel::class,
        DayMarkLocalModel::class,
        ReminderSettingsLocalModel::class
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6)
    ]
)
abstract class MigrelyDatabase : RoomDatabase() {
    abstract fun attackDao(): AttackDao

    abstract fun tagDao(): TagDao

    abstract fun medicationDao(): MedicationDao

    abstract fun dayMarkDao(): DayMarkDao

    abstract fun reminderSettingsDao(): ReminderSettingsDao

    abstract fun backupDao(): BackupDao

    /**
     * Deletes every row of every table, including tables added later, and resets the AUTOINCREMENT counters, which
     * would otherwise still tell how many intensity points were ever logged (BRD D96). The counters go first: should
     * clearing the tables then fail, the next IDs still follow the rows left. Blocks; never on the main thread.
     */
    fun deleteEverything() {
        with(openHelper.writableDatabase) {
            // The table exists only once a table with AUTOINCREMENT does.
            val hasSequences = query(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'sqlite_sequence'"
            ).use { it.moveToFirst() }
            if (hasSequences) execSQL("DELETE FROM sqlite_sequence")
        }
        // In one transaction, then compacted, so the deleted rows do not linger in the file.
        clearAllTables()
    }

    companion object {
        const val NAME = "migrely.db"
    }
}
