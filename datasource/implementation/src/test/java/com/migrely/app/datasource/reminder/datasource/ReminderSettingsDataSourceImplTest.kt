package com.migrely.app.datasource.reminder.datasource

import android.database.sqlite.SQLiteException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.reminder.dao.ReminderSettingsDao
import com.migrely.app.datasource.reminder.mapper.ReminderSettingsLocalMapper
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

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
                dailyMinuteOfDay = 1200,
                effectEnabled = true,
                permissionAskedAt = null,
                dailyLastShownOn = null
            ),
            classUnderTest.settings()
        )
    }

    @Test
    fun `Given saved settings when reading then them`() {
        given(reminderSettingsDao.settings()).willReturn(
            ReminderSettingsLocalModel(
                id = 1,
                dailyEnabled = true,
                dailyMinuteOfDay = 1290,
                effectEnabled = false,
                permissionAskedEpochMillis = 5000,
                dailyLastShownDate = "2026-10-24"
            )
        )

        assertEquals(
            ReminderSettingsDataModel(
                dailyEnabled = true,
                dailyMinuteOfDay = 1290,
                effectEnabled = false,
                permissionAskedAt = Instant.ofEpochMilli(5000),
                dailyLastShownOn = LocalDate.of(2026, 10, 24)
            ),
            classUnderTest.settings()
        )
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given storage fails when reading then a data exception`() {
        given(reminderSettingsDao.settings()).willThrow(SQLiteException())

        classUnderTest.settings()
    }

    @Test
    fun `Given the visit state saved when reading then its three dates`() {
        given(reminderSettingsDao.settings()).willReturn(
            ReminderSettingsLocalModel(
                1, false, 1200, true, null, null, "2026-11-20", "2026-11-19", "bad"
            )
        )

        val settings = classUnderTest.settings()

        assertEquals(LocalDate.of(2026, 11, 20), settings.visitRemindedFor)
        assertEquals(LocalDate.of(2026, 11, 19), settings.visitCardClosedFor)
        assertEquals(null, settings.visitPostponedTo)
    }

    @Test
    fun `When the visit state is written then only its own column changes`() {
        val before = ReminderSettingsLocalModel(1, true, 1290, false, 5000, "2026-10-24")
        val changes = argumentCaptor<(ReminderSettingsLocalModel) -> ReminderSettingsLocalModel>()

        classUnderTest.setVisitReminded(LocalDate.of(2026, 11, 20))
        classUnderTest.setVisitCardClosed(LocalDate.of(2026, 11, 19))
        classUnderTest.setVisitPostponed(LocalDate.of(2026, 11, 18))

        verify(reminderSettingsDao, times(3)).update(changes.capture())
        assertEquals(before.copy(visitRemindedFor = "2026-11-20"), changes.allValues[0](before))
        assertEquals(before.copy(visitCardClosedFor = "2026-11-19"), changes.allValues[1](before))
        assertEquals(before.copy(visitPostponedTo = "2026-11-18"), changes.allValues[2](before))
    }
}
