package com.migrely.app.onboarding.ui.mapper

import com.migrely.app.onboarding.presentation.model.OnboardingPresentationNotification
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingNotificationUiMapperTest {
    @Test
    fun `When an answer was not saved then the presenter says so`() = runTest {
        val presenter = OnboardingEventPresenter()
        val shown = async(start = UNDISPATCHED) { presenter.notSaved.first() }

        OnboardingNotificationUiMapper(
            presenter
        ).toUi(OnboardingPresentationNotification.NotSaved).present()

        assertEquals(Unit, shown.await())
    }
}
