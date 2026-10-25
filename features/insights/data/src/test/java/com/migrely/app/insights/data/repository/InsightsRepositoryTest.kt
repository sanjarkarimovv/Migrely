package com.migrely.app.insights.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.model.BackupDayMarkDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.insights.data.mapper.InsightsRecordsDomainMapper
import com.migrely.app.insights.domain.model.CurrentTimeDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsResultDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private val NOW = Instant.parse("2026-10-25T07:00:00Z")
private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val OCTOBER_FIRST: LocalDate = LocalDate.of(2026, 10, 1)

class InsightsRepositoryTest {
    private val backupDataSource: BackupDataSource = mock()
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val clockDataSource: ClockDataSource = mock {
        on { now() } doReturn NOW
        on { zone() } doReturn ZONE
    }
    private val classUnderTest = InsightsRepository(
        backupDataSource,
        preferencesDataSource,
        clockDataSource,
        InsightsRecordsDomainMapper()
    )

    @Test
    fun `When reading the records then the diary's`() {
        given(backupDataSource.records()).willReturn(
            BackupRecordsDataModel(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                listOf(BackupDayMarkDataModel(OCTOBER_FIRST, true, false, NOW))
            )
        )

        assertEquals(
            InsightsRecordsResultDomainModel.Loaded(
                InsightsRecordsDomainModel(emptyList(), emptyList(), setOf(OCTOBER_FIRST))
            ),
            classUnderTest.insightsRecords()
        )
    }

    @Test
    fun `Given the records cannot be read then failed, not thrown`() {
        given(backupDataSource.records()).willThrow(DatabaseDataException())
        assertEquals(InsightsRecordsResultDomainModel.Failed, classUnderTest.insightsRecords())

        doThrow(StorageFullDataException()).whenever(backupDataSource).records()
        assertEquals(InsightsRecordsResultDomainModel.Failed, classUnderTest.insightsRecords())
    }

    @Test
    fun `When asking then Track periods and the time come from their sources`() {
        given(preferencesDataSource.periodTracking()).willReturn(true)
        assertTrue(classUnderTest.periodTracking())
        given(preferencesDataSource.periodTracking()).willReturn(false)
        assertFalse(classUnderTest.periodTracking())

        assertEquals(CurrentTimeDomainModel(NOW, ZONE), classUnderTest.currentTime())
    }
}
