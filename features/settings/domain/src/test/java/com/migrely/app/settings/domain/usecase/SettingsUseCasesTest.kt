package com.migrely.app.settings.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.repository.GetSettingsRepository
import com.migrely.app.settings.domain.repository.GetThemeRepository
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository
import com.migrely.app.settings.domain.repository.SetThemeRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class SettingsUseCasesTest {
    @Test
    fun `When reading and setting then the repositories do it`() {
        val get: GetSettingsRepository = mock()
        val set: SetPeriodTrackingRepository = mock()
        val settings = SettingsDomainModel(
            periodTracking = false,
            theme = ThemeDomainModel.Light,
            version = "1.2.0"
        )
        given(get.settings()).willReturn(settings)
        given(set.setPeriodTracking(true)).willReturn(SettingsWriteResultDomainModel.Failed)

        assertEquals(
            settings,
            GetSettingsUseCase(get, CoroutineContextProvider.Default).executeInBackground(Unit)
        )
        assertEquals(
            SettingsWriteResultDomainModel.Failed,
            SetPeriodTrackingUseCase(
                set,
                CoroutineContextProvider.Default
            ).executeInBackground(true)
        )
    }

    @Test
    fun `When choosing a theme then it is saved, with how that went`() {
        val set: SetThemeRepository = mock()
        given(set.setTheme(ThemeDomainModel.System)).willReturn(SettingsWriteResultDomainModel.Done)

        assertEquals(
            SettingsWriteResultDomainModel.Done,
            SetThemeUseCase(set, CoroutineContextProvider.Default)
                .executeInBackground(ThemeDomainModel.System)
        )
    }

    @Test
    fun `When following the theme then each one the repository gives`() = runTest {
        val get: GetThemeRepository = mock()
        given(get.theme()).willReturn(flowOf(ThemeDomainModel.Dark, ThemeDomainModel.Light))

        assertEquals(
            listOf(ThemeDomainModel.Dark, ThemeDomainModel.Light),
            GetThemeUseCase(get, CoroutineContextProvider.Default).executeInBackground(Unit)
                .toList()
        )
    }
}
