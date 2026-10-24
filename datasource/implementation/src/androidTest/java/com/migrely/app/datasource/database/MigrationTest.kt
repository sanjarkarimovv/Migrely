package com.migrely.app.datasource.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
