package com.migrely.app.settings.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.AppearanceDomainModel
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.repository.GetAttackInProgressRepository
import com.migrely.app.settings.domain.repository.GetSettingsRepository
import com.migrely.app.settings.domain.repository.GetThemeRepository
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository
import com.migrely.app.settings.domain.repository.SetThemeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `When following the appearance then the theme and the attack, after each change`() =
        runTest {
            val theme = MutableStateFlow(ThemeDomainModel.Light)
            val attack = MutableStateFlow(false)
            val getTheme: GetThemeRepository = mock()
            val getAttack: GetAttackInProgressRepository = mock()
            given(getTheme.theme()).willReturn(theme)
            given(getAttack.attackInProgress()).willReturn(attack)
            val appearances = mutableListOf<AppearanceDomainModel>()
            val following = launch(UnconfinedTestDispatcher(testScheduler)) {
                GetAppearanceUseCase(getTheme, getAttack, CoroutineContextProvider.Default)
                    .executeInBackground(Unit)
                    .toList(appearances)
            }

            attack.value = true
            attack.value = false
            theme.value = ThemeDomainModel.System
            following.cancel()

            assertEquals(
                listOf(
                    AppearanceDomainModel(ThemeDomainModel.Light, attackInProgress = false),
                    AppearanceDomainModel(ThemeDomainModel.Light, attackInProgress = true),
                    AppearanceDomainModel(ThemeDomainModel.Light, attackInProgress = false),
                    AppearanceDomainModel(ThemeDomainModel.System, attackInProgress = false)
                ),
                appearances
            )
        }
}
