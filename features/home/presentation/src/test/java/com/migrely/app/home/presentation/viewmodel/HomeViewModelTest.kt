package com.migrely.app.home.presentation.viewmodel

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.HomeViewState.Ready
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
class HomeViewModelTest :
    BaseViewModelTest<HomeViewState, PresentationNotification, HomeViewModel>() {
    @Before
    fun setUp() {
        classUnderTest = HomeViewModel(useCaseExecutor)
    }

    @Test
    fun `When onEnter then presents Ready`() = runTest {
        // Given
        val expectedViewState = Ready
        val deferredViewState = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        // When
        classUnderTest.onEnter()

        // Then
        assertEquals(expectedViewState, deferredViewState.await())
    }
}
