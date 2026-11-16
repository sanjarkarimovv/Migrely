package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import java.time.Instant
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-11-20T18:00:00Z")

class SafetyCardRepositoryTest {
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val clockDataSource: ClockDataSource = mock { on { now() } doReturn NOW }
    private val classUnderTest = SafetyCardRepository(preferencesDataSource, clockDataSource)

    @Test
    fun `When the card appears then now is kept as its moment`() {
        classUnderTest.markSafetyCardShown()

        verify(preferencesDataSource).markSafetyCardShown(NOW.toEpochMilli())
    }

    @Test
    fun `When the mark cannot be written then nothing is thrown`() {
        given(preferencesDataSource.markSafetyCardShown(NOW.toEpochMilli()))
            .willThrow(FileDataException("Preferences not saved"))

        classUnderTest.markSafetyCardShown()
    }
}
