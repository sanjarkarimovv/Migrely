package com.migrely.app.medication.ui.view

import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.rememberNavController
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import com.migrely.app.medication.domain.usecase.GetMedicationUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.viewmodel.EditMedicationViewModel
import com.migrely.app.medication.ui.di.EditMedicationDependencies
import com.migrely.app.medication.ui.mapper.EditMedicationNotificationUiMapper
import com.migrely.app.medication.ui.mapper.EditMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.MedicationCategoryUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationKindUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.mapper.MedicationUiMapper
import com.migrely.app.medication.ui.mapper.SaveProblemUiMapper
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val ALPHA = "med-a"
private const val BETA = "med-b"
private const val ALPHA_NAME = "Alphaprofen"
private const val BETA_NAME = "Betatriptan"
private const val SAVE = "Save"
private const val NOT_FOUND = "This medication no longer exists."
private const val WAIT_MILLIS = 5_000L

/**
 * Whatever a route hands the editor, the form on screen and the row Save writes are the same
 * medication (#138). One ViewModel serves every editor, as in the app, so a read can come back late.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1200dp")
class EditMedicationIdTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val repository = FakeMedications()
    private val dependencies = editMedicationDependencies(repository)
    private var medicationId by mutableStateOf<String?>(ALPHA)
    private val emitted = CopyOnWriteArrayList<EditMedicationViewState>()
    private val listening = CoroutineScope(Dispatchers.Main.immediate)

    @Before
    fun listenToWhatTheViewModelEmits() {
        listening.launch {
            dependencies.editMedicationViewModel.viewState.collect { emitted += it }
        }
    }

    @After
    fun stopListening() {
        listening.cancel()
    }

    /** `waitUntil` runs the main thread's tasks only when its condition reads the semantics. */
    private fun waitFor(condition: () -> Boolean) {
        composeRule.waitUntil(WAIT_MILLIS) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    private fun awaitReadStarted(id: String) = waitFor { repository.readStarted(id) }

    private fun awaitEmitted(state: (EditMedicationViewState) -> Boolean) {
        waitFor { emitted.any(state) }
    }

    private fun ready(id: String?): (EditMedicationViewState) -> Boolean = {
        it is EditMedicationViewState.Ready && it.medication?.id == id
    }

    /** Switches the editor from a read still held for one medication to one held for another. */
    private fun switchWhileReading(
        from: String?,
        to: String?
    ): Pair<CountDownLatch?, CountDownLatch?> {
        val fromRead = from?.let(repository::holdRead)
        val toRead = to?.let(repository::holdRead)
        medicationId = from
        show()
        from?.let(::awaitReadStarted)
        composeRule.runOnUiThread { medicationId = to }
        composeRule.waitForIdle()
        to?.let(::awaitReadStarted)
        return fromRead to toRead
    }

    private fun show() {
        composeRule.setContent {
            with(dependencies) {
                EditMedication(rememberNavController(), medicationId)
            }
        }
    }

    @Test
    fun anotherIdLoadsThatMedicationAndSaveWritesWhatIsShown() {
        show()
        assertFormShows(ALPHA_NAME)

        composeRule.runOnUiThread { medicationId = BETA }

        assertFormShows(BETA_NAME)
        nameField(BETA_NAME).performTextReplacement("BetatriptanForte")
        composeRule.onNodeWithText(SAVE).performClick()
        assertUpdated(UpdatedRow(BETA, "BetatriptanForte", MedicationKindDomainModel.Acute))
    }

    @Test
    fun whatWasTypedForOneMedicationNeverReachesAnother() {
        show()
        assertFormShows(ALPHA_NAME)
        nameField(ALPHA_NAME).performTextReplacement("AlphaDraft")

        composeRule.runOnUiThread { medicationId = BETA }

        assertFormShows(BETA_NAME)
        composeRule.onNodeWithText("AlphaDraft").assertDoesNotExist()
    }

    @Test
    fun aLateReadOfAnotherMedicationIsNotShownOrSaved() {
        val (alphaRead, betaRead) = switchWhileReading(ALPHA, BETA)

        checkNotNull(alphaRead).countDown()
        awaitEmitted(ready(ALPHA))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SAVE).assertDoesNotExist()

        checkNotNull(betaRead).countDown()
        assertFormShows(BETA_NAME)
        composeRule.onNodeWithText(SAVE).performClick()
        assertUpdated(UpdatedRow(BETA, BETA_NAME, MedicationKindDomainModel.Acute))
    }

    @Test
    fun aLateReadInTheSameFrameAsTheRightOneDoesNotLeaveTheFormBlank() {
        val (alphaRead, betaRead) = switchWhileReading(ALPHA, BETA)
        composeRule.mainClock.autoAdvance = false

        checkNotNull(betaRead).countDown()
        awaitEmitted(ready(BETA))
        checkNotNull(alphaRead).countDown()
        awaitEmitted(ready(ALPHA))
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.autoAdvance = true

        assertFormShows(BETA_NAME)
        composeRule.onNodeWithText(SAVE).performClick()
        assertUpdated(UpdatedRow(BETA, BETA_NAME, MedicationKindDomainModel.Acute))
    }

    @Test
    fun aLateReadOfAMedicationDoesNotFillTheAddForm() {
        val (alphaRead, _) = switchWhileReading(ALPHA, null)
        awaitEmitted(ready(null))
        composeRule.waitUntil(WAIT_MILLIS) { saveIsShown() }

        checkNotNull(alphaRead).countDown()
        awaitEmitted(ready(ALPHA))
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction() and hasText(ALPHA_NAME)).assertDoesNotExist()
    }

    @Test
    fun aLateEmptyReadFromAddDoesNotFillTheEditForm() {
        val betaRead = repository.holdRead(BETA)
        medicationId = BETA
        show()
        awaitReadStarted(BETA)

        // What an Add editor sharing this ViewModel leaves behind.
        composeRule.runOnUiThread { dependencies.editMedicationViewModel.onEnter(null) }
        awaitEmitted(ready(null))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SAVE).assertDoesNotExist()

        betaRead.countDown()

        assertFormShows(BETA_NAME)
    }

    @Test
    fun aSecondReadOfTheSameMedicationDoesNotOverwriteWhatWasTyped() {
        show()
        assertFormShows(ALPHA_NAME)
        nameField(ALPHA_NAME).performTextReplacement("AlphaDraft")
        emitted.clear()

        composeRule.runOnUiThread { dependencies.editMedicationViewModel.onEnter(ALPHA) }
        awaitEmitted(ready(ALPHA))
        composeRule.waitForIdle()

        nameField("AlphaDraft").assertExists()
    }

    @Test
    fun aMedicationThatNoLongerExistsSaysSoAndOffersNoSave() {
        medicationId = "gone"
        show()

        composeRule.waitUntil(WAIT_MILLIS) {
            composeRule.onAllNodes(hasText(NOT_FOUND)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(SAVE).assertDoesNotExist()
    }

    @Test
    fun aLateNotFoundOfAnotherMedicationBeforeTheRightReadDoesNotStay() {
        val (goneRead, betaRead) = switchWhileReading("gone", BETA)

        checkNotNull(goneRead).countDown()
        awaitEmitted { it == EditMedicationViewState.NotFound }
        composeRule.waitForIdle()
        checkNotNull(betaRead).countDown()

        assertFormShows(BETA_NAME)
        composeRule.onNodeWithText(NOT_FOUND).assertDoesNotExist()
    }

    @Test
    fun aLateNotFoundOfAnotherMedicationAfterTheRightReadDoesNotHideTheForm() {
        val (goneRead, betaRead) = switchWhileReading("gone", BETA)

        checkNotNull(betaRead).countDown()
        assertFormShows(BETA_NAME)
        checkNotNull(goneRead).countDown()
        awaitEmitted { it == EditMedicationViewState.NotFound }
        composeRule.waitForIdle()

        assertFormShows(BETA_NAME)
        composeRule.onNodeWithText(SAVE).assertExists()
        composeRule.onNodeWithText(NOT_FOUND).assertDoesNotExist()
    }

    @Test
    fun aRestoredScreenKeepsWhatWasTypedAndItsMedication() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            with(dependencies) {
                EditMedication(rememberNavController(), medicationId)
            }
        }
        assertFormShows(ALPHA_NAME)
        nameField(ALPHA_NAME).performTextReplacement("AlphaDraft")

        restoration.emulateSavedInstanceStateRestore()

        nameField("AlphaDraft").assertIsDisplayed()
        composeRule.onNodeWithText(SAVE).performClick()
        assertUpdated(UpdatedRow(ALPHA, "AlphaDraft", MedicationKindDomainModel.Acute))
    }

    @Test
    fun addingStartsWithAnEmptyFormAndCreatesARow() {
        medicationId = null
        show()
        composeRule.waitUntil(WAIT_MILLIS) {
            composeRule.onAllNodes(hasText(SAVE)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasSetTextAction() and hasText("Name")).performTextReplacement("New")
        composeRule.onNodeWithText(SAVE).performClick()

        waitFor { repository.created.isNotEmpty() }
        assertEquals(listOf("New"), repository.created)
    }

    private fun saveIsShown() =
        composeRule.onAllNodes(hasText(SAVE)).fetchSemanticsNodes().isNotEmpty()

    private fun nameField(name: String) = composeRule.onNode(hasSetTextAction() and hasText(name))

    private fun assertFormShows(name: String) {
        composeRule.waitUntil(WAIT_MILLIS) {
            composeRule.onAllNodes(hasSetTextAction() and hasText(name))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertUpdated(row: UpdatedRow) {
        waitFor { repository.updated.isNotEmpty() }
        assertEquals(listOf(row), repository.updated)
    }
}

private data class UpdatedRow(val id: String, val name: String, val kind: MedicationKindDomainModel)

private fun medication(id: String, name: String) = MedicationDomainModel(
    id = id,
    name = name,
    kind = MedicationKindDomainModel.Acute,
    category = MedicationCategoryDomainModel.SimplePainkiller,
    defaultDose = "400 mg",
    archived = false
)

@Suppress("TooManyFunctions")
private class FakeMedications :
    GetMedicationRepository,
    FindMedicationByNameRepository,
    CountActiveMedicationsRepository,
    CreateMedicationRepository,
    UpdateMedicationRepository,
    SetMedicationArchivedRepository {
    private val rows = mapOf(
        ALPHA to medication(ALPHA, ALPHA_NAME),
        BETA to medication(BETA, BETA_NAME)
    )
    private val held = ConcurrentHashMap<String, CountDownLatch>()
    private val started = ConcurrentHashMap.newKeySet<String>()

    val updated = mutableListOf<UpdatedRow>()
    val created = mutableListOf<String>()

    fun holdRead(id: String): CountDownLatch = CountDownLatch(1).also { held[id] = it }

    fun readStarted(id: String) = id in started

    override fun medication(medicationId: String): MedicationLookupResultDomainModel {
        started += medicationId
        held[medicationId]?.await(WAIT_MILLIS, TimeUnit.MILLISECONDS)
        return rows[medicationId]?.let(MedicationLookupResultDomainModel::Found)
            ?: MedicationLookupResultDomainModel.NotFound
    }

    override fun medicationByNameKey(nameKey: String) =
        rows.values.firstOrNull { it.name.lowercase() == nameKey }
            ?.let(MedicationLookupResultDomainModel::Found)
            ?: MedicationLookupResultDomainModel.NotFound

    override fun activeMedicationCount() = ActiveCountResultDomainModel.Counted(rows.size)

    override fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ): MedicationWriteResultDomainModel {
        created += name
        return MedicationWriteResultDomainModel.Done(medication("new", name))
    }

    override fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ): MedicationWriteResultDomainModel {
        updated += UpdatedRow(medicationId, name, kind)
        return MedicationWriteResultDomainModel.Done(medication(medicationId, name))
    }

    override fun setMedicationArchived(medicationId: String, archived: Boolean) =
        WriteResultDomainModel.Done
}

private object TestContexts : CoroutineContextProvider {
    override val main: CoroutineContext = Dispatchers.Main
    override val io: CoroutineContext = Dispatchers.IO
}

private fun editMedicationDependencies(repository: FakeMedications): EditMedicationDependencies {
    val presenter = MedicationEventPresenter()
    return EditMedicationDependencies(
        editMedicationViewModel = EditMedicationViewModel(
            GetMedicationUseCase(repository, TestContexts),
            SaveMedicationUseCase(
                repository,
                repository,
                repository,
                repository,
                repository,
                TestContexts
            ),
            MedicationPresentationMapper(),
            MedicationDomainMapper(),
            SaveProblemPresentationMapper(),
            UseCaseExecutor()
        ),
        editMedicationViewStateUiMapper = EditMedicationViewStateUiMapper(MedicationUiMapper()),
        medicationKindUiToPresentationMapper = MedicationKindUiToPresentationMapper(),
        medicationCategoryUiToPresentationMapper = MedicationCategoryUiToPresentationMapper(),
        medicationEventPresenter = presenter,
        medicationMessageBehaviourUiMapper = MedicationMessageBehaviourUiMapper(),
        navigationMapper = NoDestination,
        notificationMapper = EditMedicationNotificationUiMapper(presenter, SaveProblemUiMapper())
    )
}

private object NoDestination :
    NavigationEventDestinationMapper<EditMedicationPresentationNavigationEvent>(
        EditMedicationPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: EditMedicationPresentationNavigationEvent) =
        UiDestination { }
}
