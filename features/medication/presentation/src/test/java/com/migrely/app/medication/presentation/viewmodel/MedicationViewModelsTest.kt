package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.medication.domain.model.LogIntakeRequestDomainModel
import com.migrely.app.medication.domain.model.LogIntakeResultDomainModel
import com.migrely.app.medication.domain.model.MedicationLogDomainModel
import com.migrely.app.medication.domain.model.MedicationLogResultDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationRequestDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.usecase.ArchiveMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationLogUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationsUseCase
import com.migrely.app.medication.domain.usecase.LogIntakeUseCase
import com.migrely.app.medication.domain.usecase.RestoreMedicationUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.domain.usecase.UndoIntakeUseCase
import com.migrely.app.medication.presentation.FIXTURE_TIME
import com.migrely.app.medication.presentation.FIXTURE_ZONE
import com.migrely.app.medication.presentation.IBUPROFEN
import com.migrely.app.medication.presentation.IBUPROFEN_DOMAIN
import com.migrely.app.medication.presentation.INTAKE
import com.migrely.app.medication.presentation.INTAKE_DOMAIN
import com.migrely.app.medication.presentation.mapper.IntakePresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.model.EditMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationLogPresentationModel
import com.migrely.app.medication.presentation.model.MedicationsPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.presentation.model.SaveProblemPresentationModel
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent
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
class MedicationsViewModelTest :
    BaseViewModelTest<
        MedicationsViewState,
        MedicationsPresentationNotification,
        MedicationsViewModel
        >() {
    @Mock private lateinit var getMedicationsUseCase: GetMedicationsUseCase

    @Mock private lateinit var archiveMedicationUseCase: ArchiveMedicationUseCase

    @Mock private lateinit var restoreMedicationUseCase: RestoreMedicationUseCase

    @Before
    fun setUp() {
        classUnderTest = MedicationsViewModel(
            getMedicationsUseCase,
            archiveMedicationUseCase,
            restoreMedicationUseCase,
            MedicationPresentationMapper(),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then shows the medications`() = runTest {
        getMedicationsUseCase.givenSuccessfulExecution(
            MedicationsResultDomainModel.Loaded(listOf(IBUPROFEN_DOMAIN))
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(MedicationsViewState.Ready(listOf(IBUPROFEN)), state.await())
    }

    @Test
    fun `Given the list cannot be read when resuming then something went wrong`() = runTest {
        getMedicationsUseCase.givenSuccessfulExecution(MedicationsResultDomainModel.Failed)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onResume()

        assertEquals(MedicationsPresentationNotification.SomethingWentWrong, notification.await())
    }

    @Test
    fun `When archiving then offers undo and reloads`() = runTest {
        archiveMedicationUseCase.givenSuccessfulExecution("m", WriteResultDomainModel.Done)
        getMedicationsUseCase.givenSuccessfulExecution(
            MedicationsResultDomainModel.Loaded(emptyList())
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onArchiveAction("m")

        assertEquals(MedicationsPresentationNotification.Archived("m"), notification.await())
        assertEquals(MedicationsViewState.Ready(emptyList()), state.await())
    }

    @Test
    fun `When undoing an archive then undone`() = runTest {
        restoreMedicationUseCase.givenSuccessfulExecution("m", WriteResultDomainModel.Done)
        getMedicationsUseCase.givenSuccessfulExecution(
            MedicationsResultDomainModel.Loaded(emptyList())
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onUndoArchiveAction("m")

        assertEquals(MedicationsPresentationNotification.Undone, notification.await())
    }

    @Test
    fun `Given the disk is full when archiving then storage full`() = runTest {
        archiveMedicationUseCase.givenSuccessfulExecution("m", WriteResultDomainModel.StorageFull)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onArchiveAction("m")

        assertEquals(MedicationsPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When adding, editing or leaving then the matching navigation`() = runTest {
        val events = listOf(
            { classUnderTest.onAddAction() } to
                MedicationsPresentationNavigationEvent.OnAddMedication,
            { classUnderTest.onMedicationAction("m") } to
                MedicationsPresentationNavigationEvent.OnEditMedication("m"),
            { classUnderTest.onLeaveAction() } to MedicationsPresentationNavigationEvent.OnLeave
        )
        events.forEach { (action, expected) ->
            val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
            action()
            assertEquals(expected, event.await())
        }
    }
}

@RunWith(MockitoJUnitRunner::class)
class EditMedicationViewModelTest :
    BaseViewModelTest<
        EditMedicationViewState,
        EditMedicationPresentationNotification,
        EditMedicationViewModel
        >() {
    @Mock private lateinit var getMedicationUseCase: GetMedicationUseCase

    @Mock private lateinit var saveMedicationUseCase: SaveMedicationUseCase

    @Before
    fun setUp() {
        classUnderTest = EditMedicationViewModel(
            getMedicationUseCase,
            saveMedicationUseCase,
            MedicationPresentationMapper(),
            MedicationDomainMapper(),
            SaveProblemPresentationMapper(),
            useCaseExecutor
        )
    }

    private val request = SaveMedicationRequestDomainModel(
        null,
        "Ibuprofen",
        IBUPROFEN_DOMAIN.kind,
        IBUPROFEN_DOMAIN.category,
        "400 mg"
    )

    private fun save() = classUnderTest.onSaveAction(
        null,
        "Ibuprofen",
        MedicationKindPresentationModel.Acute,
        MedicationCategoryPresentationModel.SimplePainkiller,
        "400 mg"
    )

    @Test
    fun `Given a new medication when entering then an empty form`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter(null)

        assertEquals(EditMedicationViewState.Ready(null), state.await())
    }

    @Test
    fun `Given a stored medication when entering then its values`() = runTest {
        getMedicationUseCase.givenSuccessfulExecution(
            "m",
            MedicationLookupResultDomainModel.Found(IBUPROFEN_DOMAIN)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter("m")

        assertEquals(EditMedicationViewState.Ready(IBUPROFEN), state.await())
    }

    @Test
    fun `Given a missing medication when entering then not found`() = runTest {
        getMedicationUseCase.givenSuccessfulExecution(
            "m",
            MedicationLookupResultDomainModel.NotFound
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter("m")

        assertEquals(EditMedicationViewState.NotFound, state.await())
    }

    @Test
    fun `Given it is saved when saving then leaves`() = runTest {
        saveMedicationUseCase.givenSuccessfulExecution(
            request,
            SaveMedicationResultDomainModel.Saved(IBUPROFEN_DOMAIN)
        )
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        save()

        assertEquals(EditMedicationPresentationNavigationEvent.OnLeave, event.await())
    }

    @Test
    fun `Given a duplicate when saving then names the existing medication`() = runTest {
        saveMedicationUseCase.givenSuccessfulExecution(
            request,
            SaveMedicationResultDomainModel.Duplicate("Ibuprofen")
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        save()

        assertEquals(
            EditMedicationPresentationNotification.NotSaved(
                SaveProblemPresentationModel.Duplicate("Ibuprofen")
            ),
            notification.await()
        )
    }

    @Test
    fun `Given the disk is full when saving then storage full`() = runTest {
        saveMedicationUseCase.givenSuccessfulExecution(
            request,
            SaveMedicationResultDomainModel.StorageFull
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        save()

        assertEquals(EditMedicationPresentationNotification.StorageFull, notification.await())
    }
}

@RunWith(MockitoJUnitRunner::class)
class LogMedicationViewModelTest :
    BaseViewModelTest<
        LogMedicationViewState,
        LogMedicationPresentationNotification,
        LogMedicationViewModel
        >() {
    @Mock private lateinit var getMedicationLogUseCase: GetMedicationLogUseCase

    @Mock private lateinit var logIntakeUseCase: LogIntakeUseCase

    @Mock private lateinit var undoIntakeUseCase: UndoIntakeUseCase

    @Mock private lateinit var saveMedicationUseCase: SaveMedicationUseCase

    @Before
    fun setUp() {
        classUnderTest = LogMedicationViewModel(
            getMedicationLogUseCase,
            logIntakeUseCase,
            undoIntakeUseCase,
            saveMedicationUseCase,
            MedicationPresentationMapper(),
            MedicationDomainMapper(),
            IntakePresentationMapper(),
            SaveProblemPresentationMapper(),
            useCaseExecutor
        )
    }

    private val logRequest =
        LogIntakeRequestDomainModel("m", "Ibuprofen", "400 mg", "a", FIXTURE_TIME)

    @Test
    fun `When entering then the time, the attack and the medications`() = runTest {
        getMedicationLogUseCase.givenSuccessfulExecution(
            "a",
            MedicationLogResultDomainModel.Loaded(
                MedicationLogDomainModel(FIXTURE_TIME, FIXTURE_ZONE, "a", listOf(IBUPROFEN_DOMAIN))
            )
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter("a")

        assertEquals(
            LogMedicationViewState.Ready(
                MedicationLogPresentationModel(FIXTURE_TIME, FIXTURE_ZONE, "a", listOf(IBUPROFEN))
            ),
            state.await()
        )
    }

    @Test
    fun `When choosing a medication then logs it and reports the intake`() = runTest {
        logIntakeUseCase.givenSuccessfulExecution(
            logRequest,
            LogIntakeResultDomainModel.Logged(INTAKE_DOMAIN)
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onMedicationAction(IBUPROFEN, "a", FIXTURE_TIME)

        assertEquals(LogMedicationPresentationNotification.Logged(INTAKE), notification.await())
    }

    @Test
    fun `Given a time in the future when logging then in future`() = runTest {
        logIntakeUseCase.givenSuccessfulExecution(logRequest, LogIntakeResultDomainModel.InFuture)
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onMedicationAction(IBUPROFEN, "a", FIXTURE_TIME)

        assertEquals(LogMedicationPresentationNotification.InFuture, notification.await())
    }

    @Test
    fun `Given the first medication when adding and logging then saves it as acute and logs it`() =
        runTest {
            saveMedicationUseCase.givenSuccessfulExecution(
                SaveMedicationRequestDomainModel(
                    null,
                    "Ibuprofen",
                    IBUPROFEN_DOMAIN.kind,
                    IBUPROFEN_DOMAIN.category,
                    null
                ),
                SaveMedicationResultDomainModel.Saved(IBUPROFEN_DOMAIN)
            )
            logIntakeUseCase.givenSuccessfulExecution(
                logRequest,
                LogIntakeResultDomainModel.Logged(INTAKE_DOMAIN)
            )
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

            classUnderTest.onAddAndLogAction(
                "Ibuprofen",
                MedicationCategoryPresentationModel.SimplePainkiller,
                "a",
                FIXTURE_TIME
            )

            assertEquals(LogMedicationPresentationNotification.Logged(INTAKE), notification.await())
        }

    @Test
    fun `Given an invalid first name when adding and logging then not saved`() = runTest {
        saveMedicationUseCase.givenSuccessfulExecution(
            SaveMedicationRequestDomainModel(
                null,
                " ",
                IBUPROFEN_DOMAIN.kind,
                null,
                null
            ),
            SaveMedicationResultDomainModel.InvalidName
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onAddAndLogAction(" ", null, "a", FIXTURE_TIME)

        assertEquals(
            LogMedicationPresentationNotification.NotSaved(
                SaveProblemPresentationModel.InvalidName
            ),
            notification.await()
        )
    }

    @Test
    fun `When undoing then removes the intake and reloads`() = runTest {
        undoIntakeUseCase.givenSuccessfulExecution("i", WriteResultDomainModel.Done)
        getMedicationLogUseCase.givenSuccessfulExecution(
            "a",
            MedicationLogResultDomainModel.Loaded(
                MedicationLogDomainModel(FIXTURE_TIME, FIXTURE_ZONE, "a", emptyList())
            )
        )
        val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }

        classUnderTest.onUndoAction("i", "a")

        assertEquals(LogMedicationPresentationNotification.IntakeUndone, notification.await())
    }

    @Test
    fun `When managing or done then the matching navigation`() = runTest {
        val manage = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
        classUnderTest.onManageAction()
        assertEquals(LogMedicationPresentationNavigationEvent.OnManageMedications, manage.await())

        val done = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }
        classUnderTest.onDoneAction()
        assertEquals(LogMedicationPresentationNavigationEvent.OnDone, done.await())
    }
}
