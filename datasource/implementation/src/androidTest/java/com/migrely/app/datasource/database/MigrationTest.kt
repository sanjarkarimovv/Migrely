package com.migrely.app.datasource.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DATABASE = "migration-test.db"

/** Every schema step keeps the user's data (BRD EC-STO-01). */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MigrelyDatabase::class.java
    )

    @Test
    fun migrating1To2_keepsAttacksAndPoints_andAddsEmptyDetails() {
        // Given a version 1 database with an ended attack and two points
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO attack (id, start_epoch_millis, start_zone_id, end_epoch_millis, " +
                    "long_check_confirmed_epoch_millis, " +
                    "long_check_disabled, created_epoch_millis, " +
                    "updated_epoch_millis) VALUES ('a', 1000, 'Asia/Tashkent', 5000, NULL, 0, 1000, 5000)"
            )
            execSQL(
                "INSERT INTO intensity_point (attack_id, at_epoch_millis, value) VALUES ('a', 2000, 6)"
            )
            execSQL(
                "INSERT INTO intensity_point (attack_id, at_epoch_millis, value) VALUES ('a', 3000, 9)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 2, true)

        // Then the attack and its points are intact and the new columns are empty
        migrated.query(
            "SELECT start_epoch_millis, end_epoch_millis, " +
                "functional_impact, note, deleted_epoch_millis " +
                "FROM attack WHERE id = 'a'"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(1000L, cursor.getLong(0))
            assertEquals(5000L, cursor.getLong(1))
            assertNull(cursor.getString(2))
            assertNull(cursor.getString(3))
            assertNull(cursor.getString(4))
        }
        migrated.query("SELECT COUNT(*) FROM intensity_point WHERE attack_id = 'a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM attack_tag").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migrating1To3_keepsAttacks_throughEveryStep() {
        // Given a version 1 database with an ended attack
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO attack (id, start_epoch_millis, start_zone_id, end_epoch_millis, " +
                    "long_check_confirmed_epoch_millis, long_check_disabled, " +
                    "created_epoch_millis, updated_epoch_millis) " +
                    "VALUES ('a', 1000, 'Asia/Tashkent', 5000, NULL, 0, 1000, 5000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 3, true)

        // Then
        migrated.query("SELECT end_epoch_millis FROM attack WHERE id = 'a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(5000L, cursor.getLong(0))
        }
        migrated.close()
    }

    @Test
    fun migrating2To3_keepsAttacks_andAddsEmptyMedicationTables() {
        // Given a version 2 database with an attack and a tag
        helper.createDatabase(TEST_DATABASE, 2).apply {
            execSQL(
                "INSERT INTO attack (id, start_epoch_millis, start_zone_id, end_epoch_millis, " +
                    "long_check_confirmed_epoch_millis, long_check_disabled, " +
                    "created_epoch_millis, updated_epoch_millis, functional_impact, note, " +
                    "deleted_epoch_millis) " +
                    "VALUES ('a', 1000, 'Asia/Tashkent', 5000, NULL, 0, 1000, 5000, 2, " +
                    "'Dark room', NULL)"
            )
            execSQL(
                "INSERT INTO attack_tag (attack_id, kind, tag_key) VALUES ('a', 'SYMPTOM', 'aura')"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 3, true)

        // Then the attack is intact and the medication tables exist and are empty
        migrated.query("SELECT functional_impact, note FROM attack WHERE id = 'a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
            assertEquals("Dark room", cursor.getString(1))
        }
        migrated.query("SELECT COUNT(*) FROM attack_tag").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        listOf("medication", "medication_intake").forEach { table ->
            migrated.query("SELECT COUNT(*) FROM $table").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
        migrated.close()
    }

    @Test
    fun migrating3To4_keepsIntakes_unanswered_andAddsAnEmptyNoticeTable() {
        // Given a version 3 database with a medication and an intake
        helper.createDatabase(TEST_DATABASE, 3).apply {
            execSQL(
                "INSERT INTO medication (id, name, name_key, kind, category, default_dose, " +
                    "archived, created_epoch_millis, updated_epoch_millis) " +
                    "VALUES ('m', 'Ibuprofen', 'ibuprofen', 'ACUTE', 'SIMPLE', '400 mg', 0, 1, 1)"
            )
            execSQL(
                "INSERT INTO medication_intake (id, medication_id, attack_id, " +
                    "taken_epoch_millis, taken_zone_id, dose, created_epoch_millis) " +
                    "VALUES ('i', 'm', NULL, 2000, 'Asia/Tashkent', '400 mg', 2000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 4, true)

        // Then the intake is kept, has no answer and its question is still open
        migrated.query(
            "SELECT dose, effect, effect_closed FROM medication_intake WHERE id = 'i'"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("400 mg", cursor.getString(0))
            assertTrue(cursor.isNull(1))
            assertEquals(0, cursor.getInt(2))
        }
        migrated.query("SELECT COUNT(*) FROM overuse_notice").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migrating1To5_keepsAttacks_throughEveryStep() {
        // Given a version 1 database with an ended attack
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO attack (id, start_epoch_millis, start_zone_id, end_epoch_millis, " +
                    "long_check_confirmed_epoch_millis, long_check_disabled, " +
                    "created_epoch_millis, updated_epoch_millis) " +
                    "VALUES ('a', 1000, 'Asia/Tashkent', 5000, NULL, 0, 1000, 5000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 5, true)

        // Then
        migrated.query("SELECT end_epoch_millis FROM attack WHERE id = 'a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(5000L, cursor.getLong(0))
        }
        migrated.close()
    }

    @Test
    fun migrating4To5_keepsMedications_andAddsAnEmptyDayMarkTable() {
        // Given a version 4 database with a medication, an answered intake and a notice
        helper.createDatabase(TEST_DATABASE, 4).apply {
            execSQL(
                "INSERT INTO medication (id, name, name_key, kind, category, default_dose, " +
                    "archived, created_epoch_millis, updated_epoch_millis) " +
                    "VALUES ('m', 'Ibuprofen', 'ibuprofen', 'ACUTE', 'SIMPLE', '400 mg', 0, 1, 1)"
            )
            execSQL(
                "INSERT INTO medication_intake (id, medication_id, attack_id, " +
                    "taken_epoch_millis, taken_zone_id, dose, created_epoch_millis, effect, " +
                    "effect_closed) VALUES ('i', 'm', NULL, 2000, 'Asia/Tashkent', NULL, 2000, " +
                    "'A_LOT', 1)"
            )
            execSQL(
                "INSERT INTO overuse_notice (category, level, acknowledged_epoch_millis) " +
                    "VALUES ('TRIPTAN', 'SOFT', 3000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 5, true)

        // Then
        migrated.query("SELECT effect FROM medication_intake WHERE id = 'i'").use { cursor ->
            cursor.moveToFirst()
            assertEquals("A_LOT", cursor.getString(0))
        }
        listOf("overuse_notice" to 1, "day_mark" to 0).forEach { (table, count) ->
            migrated.query("SELECT COUNT(*) FROM $table").use { cursor ->
                cursor.moveToFirst()
                assertEquals(count, cursor.getInt(0))
            }
        }
        migrated.close()
    }

    @Test
    fun migrating5To6_keepsDayMarks_andAddsAnEmptyReminderSettingsTable() {
        // Given a version 5 database with a pain-free day
        helper.createDatabase(TEST_DATABASE, 5).apply {
            execSQL(
                "INSERT INTO day_mark (date, period, pain_free, updated_epoch_millis) " +
                    "VALUES ('2026-10-20', 0, 1, 1000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 6, true)

        // Then
        migrated.query("SELECT pain_free FROM day_mark WHERE date = '2026-10-20'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM reminder_settings").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migrating1To6_keepsAttacks_throughEveryStep() {
        // Given a version 1 database with an ended attack
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO attack (id, start_epoch_millis, start_zone_id, end_epoch_millis, " +
                    "long_check_confirmed_epoch_millis, long_check_disabled, " +
                    "created_epoch_millis, updated_epoch_millis) " +
                    "VALUES ('a', 1000, 'Asia/Tashkent', 5000, NULL, 0, 1000, 5000)"
            )
            close()
        }

        // When
        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 6, true)

        // Then
        migrated.query("SELECT end_epoch_millis FROM attack WHERE id = 'a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(5000L, cursor.getLong(0))
        }
        migrated.close()
    }
}
