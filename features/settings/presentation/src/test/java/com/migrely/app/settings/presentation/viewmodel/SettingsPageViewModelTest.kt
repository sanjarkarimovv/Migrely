package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel
import com.migrely.app.settings.presentation.model.SettingsPagePresentationNotification
import com.migrely.app.settings.presentation.model.SettingsPageViewState
import com.migrely.app.settings.presentation.navigation.SettingsPagePresentationNavigationEvent
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
class SettingsPageViewModelTest :
    BaseViewModelTest<
        SettingsPageViewState,
        SettingsPagePresentationNotification,
        SettingsPageViewModel
        >() {
    @Before
    fun setUp() {
        classUnderTest = SettingsPageViewModel(useCaseExecutor)
    }

    @Test
    fun `When going back from a page then that page is left`() = runTest {
        SettingsPagePresentationModel.entries.forEach { page ->
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

            classUnderTest.onBackAction(page)

            assertEquals(SettingsPagePresentationNavigationEvent.OnBack(page), event.await())
        }
    }
}
