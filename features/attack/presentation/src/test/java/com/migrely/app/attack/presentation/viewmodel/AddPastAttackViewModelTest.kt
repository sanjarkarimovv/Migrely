package com.migrely.app.attack.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackRequestDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksRequestDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksResultDomainModel
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.usecase.CreatePastAttackUseCase
import com.migrely.app.attack.domain.usecase.GetPastAttackDraftUseCase
import com.migrely.app.attack.domain.usecase.MergeAttacksUseCase
import com.migrely.app.attack.presentation.mapper.AttackRangePresentationMapper
import com.migrely.app.attack.presentation.mapper.PastAttackDraftPresentationMapper
import com.migrely.app.attack.presentation.mapper.TimeFieldPresentationMapper
import com.migrely.app.attack.presentation.model.AddPastAttackPresentationNotification
import com.migrely.app.attack.presentation.model.AddPastAttackViewState
import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.presentation.model.PastAttackDraftPresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val END: Instant = Instant.parse("2026-10-23T11:00:00Z")
private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

@RunWith(MockitoJUnitRunner::class)
class AddPastAttackViewModelTest :
    BaseViewModelTest<
        AddPastAttackViewState,
        AddPastAttackPresentationNotification,
        AddPastAttackViewModel
        >() {
    @Mock private lateinit var getPastAttackDraftUseCase: GetPastAttackDraftUseCase

    @Mock private lateinit var createPastAttackUseCase: CreatePastAttackUseCase

    @Mock private lateinit var mergeAttacksUseCase: MergeAttacksUseCase

    @Before
    fun setUp() {
        classUnderTest = AddPastAttackViewModel(
            getPastAttackDraftUseCase,
            createPastAttackUseCase,
            mergeAttacksUseCase,
            PastAttackDraftPresentationMapper(),
            AttackRangePresentationMapper(),
            TimeFieldPresentationMapper(),
            useCaseExecutor
        )
    }

    private fun givenCreate(result: CreatePastAttackResultDomainModel) {
        createPastAttackUseCase.givenSuccessfulExecution(
            CreatePastAttackRequestDomainModel(START, END),
            result
        )
    }

    private fun givenMerge(result: MergeAttacksResultDomainModel) {
        mergeAttacksUseCase.givenSuccessfulExecution(
            MergeAttacksRequestDomainModel(null, START, END, editedNote = null),
            result
        )
    }

    @Test
    fun `When entering then shows loading and then the default times`() = runTest {
        getPastAttackDraftUseCase.givenSuccessfulExecution(
            null,
            PastAttackDraftDomainModel(START, END, ZONE)
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter(day = null)

        assertEquals(
            listOf(
                AddPastAttackViewState.Loading,
                AddPastAttackViewState.Ready(PastAttackDraftPresentationModel(START, END, ZONE))
            ),
            states.await()
        )
    }

    @Test
    fun `Given a chosen day when entering then asks for that day's times`() = runTest {
        val day = LocalDate.parse("2026-10-20")
        getPastAttackDraftUseCase.givenSuccessfulExecution(
            day,
            PastAttackDraftDomainModel(START, END, ZONE)
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter(day)

        assertEquals(
            AddPastAttackViewState.Ready(PastAttackDraftPresentationModel(START, END, ZONE)),
            states.await().last()
        )
    }

    @Test
    fun `Given the attack is stored when adding then opens its details`() = runTest {
        givenCreate(CreatePastAttackResultDomainModel.Created("new"))
        val event = nextNavigationEvent()

        classUnderTest.onAddAction(START, END)

        assertEquals(
            AddPastAttackPresentationNavigationEvent.OnPastAttackAdded("new"),
            event.await()
        )
    }

    @Test
    fun `Given an end in the future when adding then rejects the end`() = runTest {
        givenCreate(CreatePastAttackResultDomainModel.InFuture(TimeFieldDomainModel.End))
        val notification = nextNotification()

        classUnderTest.onAddAction(START, END)

        assertEquals(
            AddPastAttackPresentationNotification.InFuture(TimeFieldPresentationModel.End),
            notification.await()
        )
    }

    @Test
    fun `Given an end not after the start when adding then rejects the end`() = runTest {
        givenCreate(CreatePastAttackResultDomainModel.EndBeforeStart)
        val notification = nextNotification()

        classUnderTest.onAddAction(START, END)

        assertEquals(AddPastAttackPresentationNotification.EndBeforeStart, notification.await())
    }

    @Test
    fun `Given overlapping attacks when adding then names them`() = runTest {
        givenCreate(
            CreatePastAttackResultDomainModel.Overlaps(
                listOf(AttackRangeDomainModel(START, END), AttackRangeDomainModel(END, null))
            )
        )
        val notification = nextNotification()

        classUnderTest.onAddAction(START, END)

        assertEquals(
            AddPastAttackPresentationNotification.Overlaps(
                listOf(
                    AttackRangePresentationModel(START, END),
                    AttackRangePresentationModel(END, null)
                )
            ),
            notification.await()
        )
    }

    @Test
    fun `Given storage is full when adding then says so`() = runTest {
        givenCreate(CreatePastAttackResultDomainModel.StorageFull)
        val notification = nextNotification()

        classUnderTest.onAddAction(START, END)

        assertEquals(AddPastAttackPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `Given adding fails when adding then something went wrong`() = runTest {
        givenCreate(CreatePastAttackResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onAddAction(START, END)

        assertEquals(
            AddPastAttackPresentationNotification.SomethingWentWrong,
            notification.await()
        )
    }

    @Test
    fun `When merging then opens the merged attack`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.Merged("old"))
        val event = nextNavigationEvent()

        classUnderTest.onMergeAction(START, END)

        assertEquals(
            AddPastAttackPresentationNavigationEvent.OnAttacksMerged("old"),
            event.await()
        )
    }

    @Test
    fun `Given nothing overlaps any more when merging then opens the new attack`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.Saved("new"))
        val event = nextNavigationEvent()

        classUnderTest.onMergeAction(START, END)

        assertEquals(
            AddPastAttackPresentationNavigationEvent.OnPastAttackAdded("new"),
            event.await()
        )
    }

    @Test
    fun `Given the start is now in the future when merging then rejects the start`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.Start))
        val notification = nextNotification()

        classUnderTest.onMergeAction(START, END)

        assertEquals(
            AddPastAttackPresentationNotification.InFuture(TimeFieldPresentationModel.Start),
            notification.await()
        )
    }

    @Test
    fun `Given an end not after the start when merging then rejects the end`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.EndBeforeStart)
        val notification = nextNotification()

        classUnderTest.onMergeAction(START, END)

        assertEquals(AddPastAttackPresentationNotification.EndBeforeStart, notification.await())
    }

    @Test
    fun `Given storage is full when merging then says so`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.StorageFull)
        val notification = nextNotification()

        classUnderTest.onMergeAction(START, END)

        assertEquals(AddPastAttackPresentationNotification.StorageFull, notification.await())
    }

    @Test
    fun `Given merging fails when merging then something went wrong`() = runTest {
        givenMerge(MergeAttacksResultDomainModel.Failed)
        val notification = nextNotification()

        classUnderTest.onMergeAction(START, END)

        assertEquals(
            AddPastAttackPresentationNotification.SomethingWentWrong,
            notification.await()
        )
    }

    @Test
    fun `When leaving then emits the leave event`() = runTest {
        val event = nextNavigationEvent()

        classUnderTest.onLeaveAction()

        assertEquals(AddPastAttackPresentationNavigationEvent.OnLeave, event.await())
    }

    private fun TestScope.nextNotification() = async(start = UNDISPATCHED) {
        classUnderTest.notification.first()
    }

    private fun TestScope.nextNavigationEvent() = async(start = UNDISPATCHED) {
        classUnderTest.navigationEvent.first()
    }
}
