package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.datasource.UnsavedMarksDataSource
import java.time.Instant
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

private val NOW: Instant = Instant.parse("2026-11-20T18:00:00Z")

class SafetyCardRepositoryTest {
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val clockDataSource: ClockDataSource = mock { on { now() } doReturn NOW }
    private val unsavedMarksDataSource: UnsavedMarksDataSource = mock()
    private val classUnderTest =
        SafetyCardRepository(preferencesDataSource, unsavedMarksDataSource, clockDataSource)

    @Test
    fun `When the card appears then now is kept as its moment`() {
        classUnderTest.markSafetyCardShown()

        verify(preferencesDataSource).markSafetyCardShown(NOW.toEpochMilli())
        verifyNoInteractions(unsavedMarksDataSource)
    }

    @Test
    fun `When the mark cannot be written then it counts for this process, and nothing is thrown`() {
        given(preferencesDataSource.markSafetyCardShown(NOW.toEpochMilli()))
            .willThrow(FileDataException("Preferences not saved"))

        classUnderTest.markSafetyCardShown()

        // No rating within 3 days of the card while the storage is full (#278, BRD D188, D197).
        verify(unsavedMarksDataSource).keepSafetyCardShown(NOW.toEpochMilli())
    }
}
