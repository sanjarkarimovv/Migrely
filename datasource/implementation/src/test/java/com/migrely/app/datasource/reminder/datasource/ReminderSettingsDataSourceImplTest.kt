package com.migrely.app.datasource.reminder.datasource

import android.database.sqlite.SQLiteException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.reminder.dao.ReminderSettingsDao
import com.migrely.app.datasource.reminder.mapper.ReminderSettingsLocalMapper
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class ReminderSettingsDataSourceImplTest {
    private val reminderSettingsDao: ReminderSettingsDao = mock()
    private val classUnderTest =
        ReminderSettingsDataSourceImpl(reminderSettingsDao, ReminderSettingsLocalMapper())

    @Test
    fun `Given nothing saved when reading then the defaults`() {
        given(reminderSettingsDao.settings()).willReturn(null)

        assertEquals(
            ReminderSettingsDataModel(
                dailyEnabled = false,
                1200,
                effectEnabled = true,
                permissionAskedAt = null
            ),
            classUnderTest.settings()
        )
    }

    @Test
    fun `Given saved settings when reading then them`() {
        given(reminderSettingsDao.settings())
            .willReturn(
                ReminderSettingsLocalModel(
                    1,
                    dailyEnabled = true,
                    1290,
                    effectEnabled = false,
                    5000
                )
            )

        assertEquals(
            ReminderSettingsDataModel(true, 1290, false, Instant.ofEpochMilli(5000)),
            classUnderTest.settings()
        )
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given storage fails when reading then a data exception`() {
        given(reminderSettingsDao.settings()).willThrow(SQLiteException())

        classUnderTest.settings()
    }
}
