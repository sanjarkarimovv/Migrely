package com.migrely.app.settings.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.model.DeleteAllDataPresentationNotification
import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent
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

@RunWith(MockitoJUnitRunner::class)
class DeleteAllDataViewModelTest :
    BaseViewModelTest<
        DeleteAllDataViewState,
        DeleteAllDataPresentationNotification,
        DeleteAllDataViewModel
        >() {
    @Mock private lateinit var deleteAllDataUseCase: DeleteAllDataUseCase

    @Before
    fun setUp() {
        classUnderTest = DeleteAllDataViewModel(
            deleteAllDataUseCase,
            DeleteConfirmationResolver(),
            useCaseExecutor
        )
    }

    private fun verifyDeletions(count: Int) = verify(useCaseExecutor, times(count)).execute(
        useCase = eq(deleteAllDataUseCase),
        onResult = any(),
        onException = any()
    )

    @Test
    fun `When entering then the confirmation`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(DeleteAllDataViewState.Confirming, state.await())
    }

    @Test
    fun `Given the word is not typed exactly when confirming then nothing is deleted`() {
        listOf("delete", "DELETE ", "").forEach(classUnderTest::onDeleteAction)

        verify(useCaseExecutor, never()).execute(
            useCase = eq(deleteAllDataUseCase),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When all data is deleted then the progress shows, then the app starts again`() = runTest {
        deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Deleted)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onDeleteAction("DELETE")

        assertEquals(DeleteAllDataViewState.Deleting, state.await())
        assertEquals(DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted, event.await())
    }

    @Test
    fun `Given a deletion going on when the screen is rebuilt then it still shows the progress`() =
        runTest {
            // The deletion has not answered yet.
            classUnderTest.onDeleteAction("DELETE")
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter()

            assertEquals(DeleteAllDataViewState.Deleting, state.await())
        }

    @Test
    fun `Given all data was deleted when the screen is rebuilt then it still starts again`() =
        runTest {
            deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Deleted)
            classUnderTest.onDeleteAction("DELETE")
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

            classUnderTest.onEnter()
            // Neither deleting again nor leaving is possible now.
            classUnderTest.onDeleteAction("DELETE")
            classUnderTest.onBackAction()

            assertEquals(DeleteAllDataViewState.Deleting, state.await())
            assertEquals(DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted, event.await())
            verifyDeletions(1)
        }

    @Test
    fun `Given deleting fails then the confirmation again, and the user is told`() = runTest {
        deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Failed)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDeleteAction("DELETE")

        assertEquals(DeleteAllDataPresentationNotification.NotDeleted, notification.await())
        assertEquals(
            listOf(DeleteAllDataViewState.Deleting, DeleteAllDataViewState.Confirming),
            states.await()
        )
    }

    @Test
    fun `Given a failure not taken when the screen is rebuilt then it is told again, once`() =
        runTest {
            deleteAllDataUseCase.givenSuccessfulExecution(DeleteAllDataResultDomainModel.Failed)
            // Nobody listens: the screen is being rebuilt.
            classUnderTest.onDeleteAction("DELETE")

            val again = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            classUnderTest.onEnter()
            assertEquals(DeleteAllDataPresentationNotification.NotDeleted, again.await())

            classUnderTest.onFailureShownAction()
            val notifications = mutableListOf<DeleteAllDataPresentationNotification>()
            val listening = launch(UnconfinedTestDispatcher(testScheduler)) {
                classUnderTest.notification.toList(notifications)
            }
            classUnderTest.onEnter()
            listening.cancel()
            assertEquals(emptyList<DeleteAllDataPresentationNotification>(), notifications)
        }

    @Test
    fun `Given an unexpected failure while deleting then the user is told`() = runTest {
        deleteAllDataUseCase.givenFailedExecution(UnknownDomainException())
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onDeleteAction("DELETE")

        assertEquals(DeleteAllDataPresentationNotification.NotDeleted, notification.await())
    }

    @Test
    fun `When going back then back, but not while deleting`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
        classUnderTest.onBackAction()
        assertEquals(DeleteAllDataPresentationNavigationEvent.OnBack, event.await())

        val events = mutableListOf<Any>()
        val listening = launch(UnconfinedTestDispatcher(testScheduler)) {
            classUnderTest.navigationEvent.toList(events)
        }
        classUnderTest.onDeleteAction("DELETE")
        classUnderTest.onBackAction()
        listening.cancel()
        assertEquals(emptyList<Any>(), events)
    }
}
