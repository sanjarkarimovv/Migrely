package com.migrely.app.settings.data.repository

import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import com.migrely.app.settings.data.mapper.ThemeDataMapper
import com.migrely.app.settings.data.mapper.ThemeDomainMapper
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SettingsRepositoryTest {
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val appInfoDataSource: AppInfoDataSource = mock()
    private val classUnderTest = SettingsRepository(
        preferencesDataSource,
        appInfoDataSource,
        ThemeDomainMapper(),
        ThemeDataMapper()
    )

    @Test
    fun `When reading then Track periods and the theme as saved, and the installed version`() {
        given(preferencesDataSource.periodTracking()).willReturn(false)
        given(preferencesDataSource.theme()).willReturn(ThemeDataModel.System)
        given(appInfoDataSource.versionName()).willReturn("1.2.0")

        assertEquals(
            SettingsDomainModel(
                periodTracking = false,
                theme = ThemeDomainModel.System,
                version = "1.2.0"
            ),
            classUnderTest.settings()
        )
    }

    @Test
    fun `When setting then done, or failed when it is not saved`() {
        assertEquals(SettingsWriteResultDomainModel.Done, classUnderTest.setPeriodTracking(true))

        whenever(preferencesDataSource.setPeriodTracking(false)).doThrow(FileDataException())
        assertEquals(SettingsWriteResultDomainModel.Failed, classUnderTest.setPeriodTracking(false))
    }

    @Test
    fun `When choosing a theme then it is saved, or failed when it is not`() {
        assertEquals(
            SettingsWriteResultDomainModel.Done,
            classUnderTest.setTheme(ThemeDomainModel.Light)
        )
        verify(preferencesDataSource).setTheme(ThemeDataModel.Light)

        whenever(preferencesDataSource.setTheme(ThemeDataModel.Dark)).doThrow(FileDataException())
        assertEquals(
            SettingsWriteResultDomainModel.Failed,
            classUnderTest.setTheme(ThemeDomainModel.Dark)
        )
    }

    @Test
    fun `When following the theme then each saved one`() = runTest {
        given(preferencesDataSource.themeChanges())
            .willReturn(flowOf(ThemeDataModel.Dark, ThemeDataModel.Light))

        assertEquals(
            listOf(ThemeDomainModel.Dark, ThemeDomainModel.Light),
            classUnderTest.theme().toList()
        )
    }
}
