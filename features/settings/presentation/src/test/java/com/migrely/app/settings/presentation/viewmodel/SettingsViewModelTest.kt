package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
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
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest :
    BaseViewModelTest<SettingsViewState, SettingsPresentationNotification, SettingsViewModel>() {
    @Before
    fun setUp() {
        classUnderTest = SettingsViewModel(useCaseExecutor)
    }

    @Test
    fun `When entering then ready`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(SettingsViewState.Ready, state.await())
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
