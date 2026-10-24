package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SettingsRepositoryTest {
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val classUnderTest = SettingsRepository(preferencesDataSource)

    @Test
    fun `When reading then Track periods as saved`() {
        given(preferencesDataSource.periodTracking()).willReturn(false)

        assertEquals(SettingsDomainModel(periodTracking = false), classUnderTest.settings())
    }

    @Test
    fun `When setting then done, or failed when it is not saved`() {
        assertEquals(SettingsWriteResultDomainModel.Done, classUnderTest.setPeriodTracking(true))

        whenever(preferencesDataSource.setPeriodTracking(false)).doThrow(FileDataException())
        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.setPeriodTracking(false))
    }
}
