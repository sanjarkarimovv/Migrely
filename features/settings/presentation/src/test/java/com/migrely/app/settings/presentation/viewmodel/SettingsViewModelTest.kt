package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest :
    BaseViewModelTest<SettingsViewState, SettingsPresentationNotification, SettingsViewModel>() {
    @Mock private lateinit var getSettingsUseCase: GetSettingsUseCase

    @Mock private lateinit var setPeriodTrackingUseCase: SetPeriodTrackingUseCase

    @Before
    fun setUp() {
        classUnderTest = SettingsViewModel(
            getSettingsUseCase,
            setPeriodTrackingUseCase,
            SettingsPresentationMapper(),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then the settings`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SettingsDomainModel(false, ThemeDomainModel.Dark, "1.0"))
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(
            SettingsViewState.Ready(SettingsPresentationModel(periodTracking = false)),
            state.await()
        )
    }

    @Test
    fun `Given Track periods is not saved when turning it off then the user is told`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SettingsDomainModel(true, ThemeDomainModel.Dark, "1.0"))
        setPeriodTrackingUseCase.givenSuccessfulExecution(
            false,
            SettingsWriteResultDomainModel.Failed
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onPeriodTrackingAction(false)

        assertEquals(SettingsPresentationNotification.NotSaved, notification.await())
    }

    @Test
    fun `When Track periods is saved then the settings are read again`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SettingsDomainModel(false, ThemeDomainModel.Dark, "1.0"))
        setPeriodTrackingUseCase.givenSuccessfulExecution(
            false,
            SettingsWriteResultDomainModel.Done
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onPeriodTrackingAction(false)

        assertEquals(
            SettingsViewState.Ready(SettingsPresentationModel(periodTracking = false)),
            state.await()
        )
    }

    @Test
    fun `When an item is chosen then its screen opens`() = runTest {
        mapOf(
            classUnderTest::onRemindersAction to SettingsPresentationNavigationEvent.OnReminders,
            classUnderTest::onYourDataAction to SettingsPresentationNavigationEvent.OnYourData,
            classUnderTest::onBackAction to SettingsPresentationNavigationEvent.OnBack
        ).forEach { (action, expected) ->
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
            action()
            assertEquals(expected, event.await())
        }
    }
}
