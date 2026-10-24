package com.migrely.app.attack.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.attack.domain.model.AddCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.AddCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsScreenDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.RemoveIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.SelectAttackTagRequestDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeRequestDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeResultDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteResultDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.usecase.AddCustomTagUseCase
import com.migrely.app.attack.domain.usecase.AddIntensityPointUseCase
import com.migrely.app.attack.domain.usecase.ArchiveCustomTagUseCase
import com.migrely.app.attack.domain.usecase.DeleteAttackUseCase
import com.migrely.app.attack.domain.usecase.GetAttackDetailsUseCase
import com.migrely.app.attack.domain.usecase.RemoveIntensityPointUseCase
import com.migrely.app.attack.domain.usecase.SelectAttackTagUseCase
import com.migrely.app.attack.domain.usecase.SetFunctionalImpactUseCase
import com.migrely.app.attack.domain.usecase.UndoTimesChangeUseCase
import com.migrely.app.attack.domain.usecase.UpdateAttackNoteUseCase
import com.migrely.app.attack.domain.usecase.UpdateAttackTimesUseCase
import com.migrely.app.attack.presentation.mapper.AttackDetailsPresentationMapper
import com.migrely.app.attack.presentation.mapper.AttackTagDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInSymptomDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInSymptomPresentationMapper
import com.migrely.app.attack.presentation.mapper.BuiltInTriggerDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInTriggerPresentationMapper
import com.migrely.app.attack.presentation.mapper.CustomTagDomainMapper
import com.migrely.app.attack.presentation.mapper.CustomTagPresentationMapper
import com.migrely.app.attack.presentation.mapper.FunctionalImpactDomainMapper
import com.migrely.app.attack.presentation.mapper.FunctionalImpactPresentationMapper
import com.migrely.app.attack.presentation.mapper.HeadLocationDomainMapper
import com.migrely.app.attack.presentation.mapper.HeadLocationPresentationMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointDomainMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointPresentationMapper
import com.migrely.app.attack.presentation.mapper.TagKindDomainMapper
import com.migrely.app.attack.presentation.mapper.TagKindPresentationMapper
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.model.AttackTagPresentationModel
import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.presentation.model.PointRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.presentation.model.TimeRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TimesUndoPresentationModel
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val END: Instant = START.plusSeconds(7_200)
private val DETAILS = AttackDetailsDomainModel(
    id = "a",
    start = START,
    startZone = ZoneId.of("Asia/Tashkent"),
    end = END,
    intensityPoints = emptyList(),
    locations = emptySet(),
    symptoms = emptySet(),
    triggers = emptySet(),
    functionalImpact = null,
    note = ""
)
private val SCREEN = AttackDetailsScreenDomainModel(DETAILS, emptyList(), emptyList(), END)

@RunWith(MockitoJUnitRunner::class)
class AttackDetailsViewModelTest :
    BaseViewModelTest<
        AttackDetailsViewState,
        AttackDetailsPresentationNotification,
        AttackDetailsViewModel
        >() {
    @Mock private lateinit var getAttackDetailsUseCase: GetAttackDetailsUseCase

    @Mock private lateinit var updateAttackTimesUseCase: UpdateAttackTimesUseCase

    @Mock private lateinit var undoTimesChangeUseCase: UndoTimesChangeUseCase

    @Mock private lateinit var addIntensityPointUseCase: AddIntensityPointUseCase

    @Mock private lateinit var removeIntensityPointUseCase: RemoveIntensityPointUseCase

    @Mock private lateinit var selectAttackTagUseCase: SelectAttackTagUseCase

    @Mock private lateinit var setFunctionalImpactUseCase: SetFunctionalImpactUseCase

    @Mock private lateinit var updateAttackNoteUseCase: UpdateAttackNoteUseCase

    @Mock private lateinit var addCustomTagUseCase: AddCustomTagUseCase

    @Mock private lateinit var archiveCustomTagUseCase: ArchiveCustomTagUseCase

    @Mock private lateinit var deleteAttackUseCase: DeleteAttackUseCase

    private val presentationMapper = AttackDetailsPresentationMapper(
        HeadLocationPresentationMapper(),
        BuiltInSymptomPresentationMapper(),
        BuiltInTriggerPresentationMapper(),
        CustomTagPresentationMapper(TagKindPresentationMapper()),
        FunctionalImpactPresentationMapper(),
        IntensityPointPresentationMapper()
    )
    private val ready = AttackDetailsViewState.Ready(presentationMapper.toPresentation(SCREEN))

    @Before
    fun setUp() {
        classUnderTest = AttackDetailsViewModel(
            getAttackDetailsUseCase,
            updateAttackTimesUseCase,
            undoTimesChangeUseCase,
            addIntensityPointUseCase,
            removeIntensityPointUseCase,
            selectAttackTagUseCase,
            setFunctionalImpactUseCase,
            updateAttackNoteUseCase,
            addCustomTagUseCase,
            archiveCustomTagUseCase,
            deleteAttackUseCase,
            presentationMapper,
            AttackTagDomainMapper(
                HeadLocationDomainMapper(),
                BuiltInSymptomDomainMapper(),
                BuiltInTriggerDomainMapper(),
                CustomTagDomainMapper(TagKindDomainMapper())
            ),
            FunctionalImpactDomainMapper(),
            IntensityPointDomainMapper(),
            IntensityPointPresentationMapper(),
            TagKindDomainMapper(),
            useCaseExecutor
        )
    }

    private fun givenDetails(
        result: AttackDetailsResultDomainModel = AttackDetailsResultDomainModel.Loaded(SCREEN)
    ) {
        getAttackDetailsUseCase.givenSuccessfulExecution("a", result)
    }

    private fun givenTimes(
        start: Instant,
        end: Instant?,
        result: UpdateAttackTimesResultDomainModel
    ) {
        updateAttackTimesUseCase.givenSuccessfulExecution(
            UpdateAttackTimesRequestDomainModel("a", start, end),
            result
        )
    }

    @Test
    fun `When entering then shows loading and then the details`() = runTest {
        givenDetails()
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter("a")

        assertEquals(listOf(AttackDetailsViewState.Loading, ready), states.await())
    }

    @Test
    fun `Given the attack is gone when entering then not found`() = runTest {
        givenDetails(AttackDetailsResultDomainModel.NotFound)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter("a")

        assertEquals(AttackDetailsViewState.NotFound, states.await().last())
    }

    @Test
    fun `Given a valid start when changing times then reloads without a message`() = runTest {
        val newStart = START.minusSeconds(600)
        givenTimes(
            newStart,
            END,
            UpdateAttackTimesResultDomainModel.Updated(START, END, emptyList())
        )
        givenDetails()
        val state = nextViewState()

        classUnderTest.onTimesAction("a", TimeFieldPresentationModel.Start, newStart, END)

        assertEquals(ready, state.await())
    }

    @Test
    fun `Given points left outside when changing the end then offers to undo with the old times`() =
        runTest {
            val removed = IntensityPointDomainModel(END.minusSeconds(60), 8)
            val newEnd = END.minusSeconds(3_600)
            givenTimes(
                START,
                newEnd,
                UpdateAttackTimesResultDomainModel.Updated(START, END, listOf(removed))
            )
            givenDetails()
            val notification = nextNotification()

            classUnderTest.onTimesAction("a", TimeFieldPresentationModel.End, START, newEnd)

            assertEquals(
                AttackDetailsPresentationNotification.PointsRemovedByTimeChange(
                    TimesUndoPresentationModel(
                        "a",
                        START,
                        END,
                        listOf(IntensityPointPresentationModel(removed.at, 8))
                    )
                ),
                notification.await()
            )
        }

    @Test
    fun `Given an end in the future when changing times then rejects the end`() = runTest {
        givenTimes(
            START,
            END,
            UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.End)
        )
        val notification = nextNotification()

        classUnderTest.onTimesAction("a", TimeFieldPresentationModel.End, START, END)

        assertEquals(
            AttackDetailsPresentationNotification.TimeRejected(
                TimeFieldPresentationModel.End,
                TimeRejectionPresentationModel.InFuture
            ),
            notification.await()
        )
    }

    @Test
    fun `Given a start after the end when changing the start then rejects it under the start`() =
        runTest {
            givenTimes(END, END, UpdateAttackTimesResultDomainModel.EndBeforeStart)
            val notification = nextNotification()

            classUnderTest.onTimesAction("a", TimeFieldPresentationModel.Start, END, END)

            assertEquals(
                AttackDetailsPresentationNotification.TimeRejected(
                    TimeFieldPresentationModel.Start,
                    TimeRejectionPresentationModel.EndBeforeStart
                ),
                notification.await()
            )
        }

    @Test
    fun `Given an overlap when changing the start then names the other attack`() = runTest {
        val otherStart = START.minusSeconds(7_200)
        val otherEnd = START.minusSeconds(1_800)
        givenTimes(
            START.minusSeconds(3_600),
            END,
            UpdateAttackTimesResultDomainModel.Overlaps(otherStart, otherEnd)
        )
        val notification = nextNotification()

        classUnderTest.onTimesAction(
            "a",
            TimeFieldPresentationModel.Start,
            START.minusSeconds(3_600),
            END
        )

        assertEquals(
            AttackDetailsPresentationNotification.TimeRejected(
                TimeFieldPresentationModel.Start,
                TimeRejectionPresentationModel.Overlaps(otherStart, otherEnd)
            ),
            notification.await()
        )
    }

    @Test
    fun `When undoing a time change then restores and confirms`() = runTest {
        val point = IntensityPointPresentationModel(END, 8)
        undoTimesChangeUseCase.givenSuccessfulExecution(
            UndoTimesChangeRequestDomainModel(
                "a",
                START,
                END,
                listOf(IntensityPointDomainModel(END, 8))
            ),
            UndoTimesChangeResultDomainModel.Restored
        )
        givenDetails()
        val notification = nextNotification()

        classUnderTest.onUndoTimesChangeAction(
            TimesUndoPresentationModel("a", START, END, listOf(point))
        )

        assertEquals(AttackDetailsPresentationNotification.Undone, notification.await())
    }

    @Test
    fun `Given the first 10 when adding a point then shows the safety card`() = runTest {
        val at = START.plusSeconds(60)
        addIntensityPointUseCase.givenSuccessfulExecution(
            AddIntensityPointRequestDomainModel("a", IntensityPointDomainModel(at, 10)),
            AddIntensityPointResultDomainModel.Added(firstMaximum = true)
        )
        givenDetails()
        val notification = nextNotification()

        classUnderTest.onAddPointAction("a", IntensityPointPresentationModel(at, 10))

        assertEquals(
            AttackDetailsPresentationNotification.FirstMaximumRecorded,
            notification.await()
        )
    }

    @Test
    fun `Given a time outside the attack when adding a point then rejects it`() = runTest {
        val at = END.plusSeconds(60)
        addIntensityPointUseCase.givenSuccessfulExecution(
            AddIntensityPointRequestDomainModel("a", IntensityPointDomainModel(at, 5)),
            AddIntensityPointResultDomainModel.OutsideAttack
        )
        val notification = nextNotification()

        classUnderTest.onAddPointAction("a", IntensityPointPresentationModel(at, 5))

        assertEquals(
            AttackDetailsPresentationNotification.PointRejected(
                PointRejectionPresentationModel.OutsideAttack
            ),
            notification.await()
        )
    }

    @Test
    fun `When removing a point then offers to undo it`() = runTest {
        val point = IntensityPointPresentationModel(START, 6)
        removeIntensityPointUseCase.givenSuccessfulExecution(
            RemoveIntensityPointRequestDomainModel("a", IntensityPointDomainModel(START, 6)),
            WriteResultDomainModel.Written
        )
        givenDetails()
        val notification = nextNotification()

        classUnderTest.onRemovePointAction("a", point)

        assertEquals(
            AttackDetailsPresentationNotification.PointRemoved("a", point),
            notification.await()
        )
    }

    @Test
    fun `When selecting a location then stores the domain tag and reloads`() = runTest {
        selectAttackTagUseCase.givenSuccessfulExecution(
            SelectAttackTagRequestDomainModel(
                "a",
                AttackTagDomainModel.Location(HeadLocationDomainModel.Forehead),
                true
            ),
            WriteResultDomainModel.Written
        )
        givenDetails()
        val state = nextViewState()

        classUnderTest.onTagAction(
            "a",
            AttackTagPresentationModel.Location(HeadLocationPresentationModel.Forehead),
            selected = true
        )

        assertEquals(ready, state.await())
    }

    @Test
    fun `Given the list is full when adding a custom trigger then says so`() = runTest {
        addCustomTagUseCase.givenSuccessfulExecution(
            AddCustomTagRequestDomainModel("a", TagKindDomainModel.Trigger, "Perfume"),
            AddCustomTagResultDomainModel.LimitReached
        )
        val notification = nextNotification()

        classUnderTest.onAddCustomTagAction("a", TagKindPresentationModel.Trigger, "Perfume")

        assertEquals(AttackDetailsPresentationNotification.TagLimitReached, notification.await())
    }

    @Test
    fun `Given storage is full when saving the note then says so`() = runTest {
        updateAttackNoteUseCase.givenSuccessfulExecution(
            UpdateAttackNoteRequestDomainModel("a", "Note"),
            UpdateAttackNoteResultDomainModel.StorageFull
        )
        val notification = nextNotification()

        classUnderTest.onNoteAction("a", "Note")

        assertEquals(AttackDetailsPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When deleting then leaves for home`() = runTest {
        deleteAttackUseCase.givenSuccessfulExecution("a", WriteResultDomainModel.Written)
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onDeleteAction("a")

        assertEquals(AttackDetailsPresentationNavigationEvent.OnAttackDeleted, event.await())
    }

    @Test
    fun `Given storage is full when deleting then stays and says so`() = runTest {
        deleteAttackUseCase.givenSuccessfulExecution("a", WriteResultDomainModel.StorageFull)
        val notification = nextNotification()

        classUnderTest.onDeleteAction("a")

        assertEquals(AttackDetailsPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `When leaving then emits the leave event`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onLeaveAction()

        assertTrue(event.await() is AttackDetailsPresentationNavigationEvent.OnLeave)
    }

    private fun TestScope.nextViewState() = async(start = UNDISPATCHED) {
        classUnderTest.viewState.first()
    }

    private fun TestScope.nextNotification() = async(start = UNDISPATCHED) {
        classUnderTest.notification.first()
    }
}
