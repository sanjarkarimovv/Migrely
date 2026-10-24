package com.migrely.app.datasource.reminder.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The one row of reminder settings (change add-reminders). */
@RunWith(AndroidJUnit4::class)
class ReminderSettingsDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: ReminderSettingsDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.reminderSettingsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenNothingSaved_whenReading_thenNoRow() {
        assertNull(classUnderTest.settings())
    }

    @Test
    fun givenNothingSaved_whenUpdating_thenStartsFromTheDefaults() {
        classUnderTest.update { it.copy(dailyEnabled = true) }

        assertEquals(
            ReminderSettingsLocalModel(
                id = 1,
                dailyEnabled = true,
                dailyMinuteOfDay = 1200,
                effectEnabled = true,
                permissionAskedEpochMillis = null,
                dailyLastShownDate = null
            ),
            classUnderTest.settings()
        )
    }

    @Test
    fun givenSettings_whenUpdatingAgain_thenOneRowKeepsTheOtherFields() {
        classUnderTest.update { it.copy(dailyEnabled = true, dailyMinuteOfDay = 1290) }

        classUnderTest.update { it.copy(effectEnabled = false, permissionAskedEpochMillis = 5000) }
        classUnderTest.update { it.copy(dailyLastShownDate = "2026-10-24") }

        assertEquals(
            ReminderSettingsLocalModel(
                id = 1,
                dailyEnabled = true,
                dailyMinuteOfDay = 1290,
                effectEnabled = false,
                permissionAskedEpochMillis = 5000,
                dailyLastShownDate = "2026-10-24"
            ),
            classUnderTest.settings()
        )
    }
}
