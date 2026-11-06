package com.migrely.app.settings.ui.view

import android.os.Looper
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.CancelRemindersRepository
import com.migrely.app.settings.domain.repository.DeleteFilesRepository
import com.migrely.app.settings.domain.repository.DeletePreferencesRepository
import com.migrely.app.settings.domain.repository.DeleteRecordsRepository
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent
import com.migrely.app.settings.presentation.viewmodel.DeleteAllDataViewModel
import com.migrely.app.settings.ui.di.DeleteAllDataDependencies
import com.migrely.app.settings.ui.mapper.DeleteAllDataEventPresenter
import com.migrely.app.settings.ui.mapper.DeleteAllDataNotificationUiMapper
import com.migrely.app.settings.ui.mapper.DeleteAllDataViewStateUiMapper
import com.migrely.app.settings.ui.mapper.DeleteProgressUiResolver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val DELETE = "delete"
private const val WORD = "DELETE"
private const val FADE_STARTED_MILLIS = 100L
private const val AFTER_THE_FADE_MILLIS = 1_000L
private const val WAIT_MILLIS = 5_000L

/**
 * A real `NavHost` with the observer beside it, as `AppNavHost` has it. Once the data is wiped the
 * app must restart, whichever screen is on top and whether or not the delete screen still exists.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DeleteAllDataObserverTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private var restarts = 0
    private val records = HeldRecords()
    private val viewModel = DeleteAllDataViewModel(
        DeleteAllDataUseCase(records, records, records, records, TestContexts),
        DeleteConfirmationResolver(),
        UseCaseExecutor()
    )
    private val dependencies = dependencies(viewModel) { restarts++ }

    private fun showTheDeleteScreen() {
        composeRule.setContent {
            navController = rememberNavController()
            dependencies.DeleteAllDataObserver(navController)
            NavHost(navController, startDestination = HOME) {
                composable(HOME) { Text(HOME) }
                composable(DELETE) { dependencies.DeleteAllData() }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnUiThread { navController.navigate(DELETE) }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        advance(AFTER_THE_FADE_MILLIS)
    }

    @Test
    fun aDeletionEndingOnTheScreenRestartsOnce() {
        showTheDeleteScreen()
        startDeleting()

        records.finish()

        assertRestartedOnce()
    }

    @Test
    fun aDeletionEndingWhileTheScreenFadesOutRestarts() {
        showTheDeleteScreen()
        startDeleting()
        leaveTheDeleteScreen()
        advance(FADE_STARTED_MILLIS)

        records.finish()

        assertRestartedOnce()
    }

    @Test
    fun aDeletionEndingAfterTheScreenIsGoneRestarts() {
        showTheDeleteScreen()
        startDeleting()
        leaveTheDeleteScreen()
        advance(AFTER_THE_FADE_MILLIS)

        records.finish()

        assertRestartedOnce()
    }

    @Test
    fun backBeforeDeletingStillLeavesTheScreen() {
        showTheDeleteScreen()
        composeRule.runOnUiThread { viewModel.onBackAction() }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(HOME, navController.currentBackStackEntry?.destination?.route)
            assertEquals(0, restarts)
        }
    }

    /** After a recreation nothing was listening when it ended: the observer itself must ask. */
    @Test
    fun anObserverComposedAfterTheDeletionEndedRestarts() {
        var composed by mutableStateOf(false)
        composeRule.setContent {
            if (composed) dependencies.DeleteAllDataObserver(rememberNavController())
        }
        composeRule.runOnUiThread { viewModel.onDeleteAction(WORD) }
        records.finish()
        waitFor { records.allStepsRan() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(0, restarts) }

        composeRule.runOnUiThread { composed = true }

        assertRestartedOnce()
    }

    /** `waitUntil` runs the main thread's tasks only when its condition reads the semantics. */
    private fun waitFor(condition: () -> Boolean) {
        composeRule.waitUntil(WAIT_MILLIS) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    private fun startDeleting() {
        composeRule.runOnUiThread { viewModel.onDeleteAction(WORD) }
        waitFor { records.started() }
    }

    private fun leaveTheDeleteScreen() {
        composeRule.runOnUiThread { navController.popBackStack(DELETE, inclusive = true) }
        composeRule.waitForIdle()
    }

    private fun advance(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis)
        composeRule.waitForIdle()
    }

    private fun assertRestartedOnce() {
        waitFor { restarts > 0 }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(1, restarts) }
    }
}

private class HeldRecords :
    CancelRemindersRepository,
    DeleteRecordsRepository,
    DeletePreferencesRepository,
    DeleteFilesRepository {
    private val started = CountDownLatch(1)
    private val finished = CountDownLatch(1)
    private val lastStep = CountDownLatch(1)

    fun started() = started.count == 0L

    fun allStepsRan() = lastStep.count == 0L

    fun finish() = finished.countDown()

    override fun deleteRecords(): SettingsWriteResultDomainModel {
        started.countDown()
        finished.await(WAIT_MILLIS, TimeUnit.MILLISECONDS)
        return SettingsWriteResultDomainModel.Done
    }

    override fun cancelReminders() = SettingsWriteResultDomainModel.Done

    override fun deletePreferences(): SettingsWriteResultDomainModel {
        lastStep.countDown()
        return SettingsWriteResultDomainModel.Done
    }

    override fun deleteFiles() = SettingsWriteResultDomainModel.Done
}

private object TestContexts : CoroutineContextProvider {
    override val main: CoroutineContext = Dispatchers.Main
    override val io: CoroutineContext = Dispatchers.IO
}

private fun dependencies(
    viewModel: DeleteAllDataViewModel,
    onRestart: () -> Unit
): DeleteAllDataDependencies {
    val presenter = DeleteAllDataEventPresenter()
    return DeleteAllDataDependencies(
        deleteAllDataViewModel = viewModel,
        deleteAllDataViewStateUiMapper = DeleteAllDataViewStateUiMapper(),
        deleteConfirmationResolver = DeleteConfirmationResolver(),
        deleteProgressUiResolver = DeleteProgressUiResolver(),
        deleteAllDataEventPresenter = presenter,
        navigationMapper = FakeDestinations(onRestart),
        notificationMapper = DeleteAllDataNotificationUiMapper(presenter)
    )
}

private class FakeDestinations(private val onRestart: () -> Unit) :
    NavigationEventDestinationMapper<DeleteAllDataPresentationNavigationEvent>(
        DeleteAllDataPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: DeleteAllDataPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        DeleteAllDataPresentationNavigationEvent.OnBack -> UiDestination(::leave)
        DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted -> UiDestination { onRestart() }
    }

    private fun leave(navController: NavController) {
        navController.popBackStack(DELETE, inclusive = true)
    }
}
