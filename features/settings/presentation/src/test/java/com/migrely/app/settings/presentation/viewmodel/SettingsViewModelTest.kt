package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.domain.usecase.SetThemeUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.mapper.ThemeDomainMapper
import com.migrely.app.settings.presentation.mapper.ThemePresentationMapper
import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val SETTINGS = SettingsDomainModel(false, ThemeDomainModel.Light, "1.2.0")
private val SHOWN = SettingsPresentationModel(false, ThemePresentationModel.Light, "1.2.0")

@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest :
    BaseViewModelTest<SettingsViewState, SettingsPresentationNotification, SettingsViewModel>() {
    @Mock private lateinit var getSettingsUseCase: GetSettingsUseCase

    @Mock private lateinit var setPeriodTrackingUseCase: SetPeriodTrackingUseCase

    @Mock private lateinit var setThemeUseCase: SetThemeUseCase

    @Mock private lateinit var deleteAllDataUseCase: DeleteAllDataUseCase

    @Before
    fun setUp() {
        classUnderTest = SettingsViewModel(
            getSettingsUseCase,
            setPeriodTrackingUseCase,
            setThemeUseCase,
            deleteAllDataUseCase,
            SettingsPresentationMapper(ThemePresentationMapper()),
            ThemeDomainMapper(),
            DeleteConfirmationResolver(),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering or coming back then the settings`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter()
        classUnderTest.onResume()

        assertEquals(
            List(2) { SettingsViewState.Ready(SHOWN, deleting = false) },
            states.await()
        )
    }

    @Test
    fun `Given Track periods is not saved when turning it off then the user is told`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
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
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        setPeriodTrackingUseCase.givenSuccessfulExecution(
            false,
            SettingsWriteResultDomainModel.Done
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onPeriodTrackingAction(false)

        assertEquals(SettingsViewState.Ready(SHOWN, deleting = false), state.await())
    }

    @Test
    fun `When a theme is chosen then it is saved and the settings are read again`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        setThemeUseCase.givenSuccessfulExecution(
            ThemeDomainModel.System,
            SettingsWriteResultDomainModel.Done
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onThemeAction(ThemePresentationModel.System)

        assertEquals(SettingsViewState.Ready(SHOWN, deleting = false), state.await())
    }

    @Test
    fun `Given the theme is not saved when choosing it then the user is told`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        setThemeUseCase.givenSuccessfulExecution(
            ThemeDomainModel.Dark,
            SettingsWriteResultDomainModel.Failed
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onThemeAction(ThemePresentationModel.Dark)

        assertEquals(SettingsPresentationNotification.NotSaved, notification.await())
    }

    @Test
    fun `When an item is chosen then its screen opens`() = runTest {
        mapOf(
            classUnderTest::onRemindersAction to SettingsPresentationNavigationEvent.OnReminders,
            classUnderTest::onYourDataAction to SettingsPresentationNavigationEvent.OnYourData,
            classUnderTest::onWarningSignsAction to
                SettingsPresentationNavigationEvent.OnWarningSigns,
            classUnderTest::onAboutMigrelyAction to
                SettingsPresentationNavigationEvent.OnAboutMigrely,
            classUnderTest::onPrivacyAction to SettingsPresentationNavigationEvent.OnPrivacy,
            classUnderTest::onLicencesAction to SettingsPresentationNavigationEvent.OnLicences,
            classUnderTest::onBackAction to SettingsPresentationNavigationEvent.OnBack
        ).forEach { (action, expected) ->
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
            action()
            assertEquals(expected, event.await())
        }
    }

    @Test
    fun `When all data is deleted then the progress shows, then the app starts again`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Deleted)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onDeleteAllDataAction("DELETE")

        assertEquals(SettingsViewState.Ready(SHOWN, deleting = true), state.await())
        assertEquals(SettingsPresentationNavigationEvent.OnAllDataDeleted, event.await())
    }

    @Test
    fun `Given all data was deleted when the screen comes back then it still starts again`() =
        runTest {
            getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
            deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Deleted)
            classUnderTest.onDeleteAllDataAction("DELETE")
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

            classUnderTest.onResume()
            // Deleting again is never asked for.
            classUnderTest.onDeleteAllDataAction("DELETE")

            assertEquals(SettingsViewState.Ready(SHOWN, deleting = true), state.await())
            assertEquals(SettingsPresentationNavigationEvent.OnAllDataDeleted, event.await())
            verify(useCaseExecutor, times(1)).execute(
                useCase = eq(deleteAllDataUseCase),
                onResult = any(),
                onException = any()
            )
        }

    @Test
    fun `Given the word is not typed exactly when confirming then nothing is deleted`() {
        listOf("delete", "DELETE ", "").forEach(classUnderTest::onDeleteAllDataAction)

        verify(useCaseExecutor, never()).execute(
            useCase = eq(deleteAllDataUseCase),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `Given deleting fails then the user is told and can try again`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Failed)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDeleteAllDataAction("DELETE")

        assertEquals(SettingsPresentationNotification.NotDeleted, notification.await())
        assertEquals(
            listOf(
                SettingsViewState.Ready(SHOWN, deleting = true),
                SettingsViewState.Ready(SHOWN, deleting = false)
            ),
            states.await()
        )
    }

    @Test
    fun `Given a deletion failure not taken when the screen comes back then it is told again`() =
        runTest {
            getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
            deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Failed)
            // Nobody listens: the screen is being rebuilt.
            classUnderTest.onDeleteAllDataAction("DELETE")

            val again = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            classUnderTest.onResume()
            assertEquals(SettingsPresentationNotification.NotDeleted, again.await())

            classUnderTest.onDeleteFailureShownAction()
            val notifications = mutableListOf<SettingsPresentationNotification>()
            val listening = launch(UnconfinedTestDispatcher(testScheduler)) {
                classUnderTest.notification.toList(notifications)
            }
            classUnderTest.onResume()
            listening.cancel()
            assertEquals(emptyList<SettingsPresentationNotification>(), notifications)
        }

    @Test
    fun `Given an unexpected failure while deleting then the user is told`() = runTest {
        getSettingsUseCase.givenSuccessfulExecution(SETTINGS)
        deleteAllDataUseCase.givenFailedExecution(UnknownDomainException())
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDeleteAllDataAction("DELETE")

        assertEquals(SettingsPresentationNotification.NotDeleted, notification.await())
    }
}
