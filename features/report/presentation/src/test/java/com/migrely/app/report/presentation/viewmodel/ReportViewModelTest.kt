package com.migrely.app.report.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.model.ReportCreditWriteResultDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.domain.usecase.CreateReportUseCase
import com.migrely.app.report.domain.usecase.GetReportOptionsUseCase
import com.migrely.app.report.domain.usecase.SetReportCreditUseCase
import com.migrely.app.report.presentation.mapper.ReportOptionsPresentationMapper
import com.migrely.app.report.presentation.mapper.ReportPeriodDomainMapper
import com.migrely.app.report.presentation.model.ReportOptionsPresentationModel
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel
import com.migrely.app.report.presentation.model.ReportPresentationNotification
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.presentation.navigation.ReportPresentationNavigationEvent
import java.time.LocalDate
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.willAnswer
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val THREE = ReportPeriodPresentationModel.LastMonths(3)
private val THREE_DOMAIN = ReportPeriodDomainModel.LastMonths(3)
private val DATES = ReportDatesDomainModel(LocalDate.of(2026, 7, 26), LocalDate.of(2026, 10, 25))
private val SIX = ReportPeriodPresentationModel.LastMonths(6)
private val SIX_DOMAIN = ReportPeriodDomainModel.LastMonths(6)
private val SIX_DATES =
    ReportDatesDomainModel(LocalDate.of(2026, 4, 26), LocalDate.of(2026, 10, 25))
private val READY_SIX = ReportViewState.Ready(
    ReportOptionsPresentationModel(
        SIX_DATES.from,
        SIX_DATES.to,
        anythingLogged = true,
        period = SIX,
        lastVisit = null,
        creditShown = true
    )
)
private val READY = ReportViewState.Ready(
    ReportOptionsPresentationModel(
        LocalDate.of(2026, 7, 26),
        LocalDate.of(2026, 10, 25),
        anythingLogged = true,
        period = THREE,
        lastVisit = null,
        creditShown = true
    )
)

private typealias OptionsAnswer = (ReportOptionsDomainModel) -> Unit

@RunWith(MockitoJUnitRunner::class)
class ReportViewModelTest :
    BaseViewModelTest<ReportViewState, ReportPresentationNotification, ReportViewModel>() {
    @Mock private lateinit var getReportOptionsUseCase: GetReportOptionsUseCase

    @Mock private lateinit var createReportUseCase: CreateReportUseCase

    @Mock private lateinit var setReportCreditUseCase: SetReportCreditUseCase

    @Before
    fun setUp() {
        classUnderTest = ReportViewModel(
            getReportOptionsUseCase,
            createReportUseCase,
            setReportCreditUseCase,
            ReportPeriodDomainMapper(),
            ReportOptionsPresentationMapper(),
            useCaseExecutor
        )
    }

    private suspend fun notificationOf(action: () -> Unit): ReportPresentationNotification =
        coroutineScope {
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            action()
            notification.await()
        }

    @Test
    fun `When entering then the period's dates, and whether there is anything to report`() =
        runTest {
            getReportOptionsUseCase.givenSuccessfulExecution(
                THREE_DOMAIN,
                ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
            )
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter(THREE)

            assertEquals(READY, state.await())
        }

    @Test
    fun `Given the options cannot be read then unavailable`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Failed(creditShown = false)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onPeriodAction(THREE)

        assertEquals(ReportViewState.Unavailable(creditShown = false), state.await())
    }

    @Test
    fun `When creating then working, then share it and read the options again`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
        )
        createReportUseCase.givenSuccessfulExecution(
            CreateReportRequestDomainModel(THREE_DOMAIN, includeNotes = true, includeCredit = true),
            CreateReportResultDomainModel.Created("content://r")
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            ReportPresentationNotification.Share("content://r"),
            notificationOf {
                classUnderTest.onCreateAction(THREE, includeNotes = true, includeCredit = true)
            }
        )
        assertEquals(listOf(ReportViewState.Working, READY), states.await())
    }

    @Test
    fun `Given the report cannot be made then why`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
        )
        mapOf(
            CreateReportResultDomainModel.NothingLogged to
                ReportPresentationNotification.NothingLogged,
            CreateReportResultDomainModel.StorageFull to ReportPresentationNotification.StorageFull,
            CreateReportResultDomainModel.Failed to
                ReportPresentationNotification.SomethingWentWrong
        ).forEach { (result, expected) ->
            createReportUseCase.givenSuccessfulExecution(
                CreateReportRequestDomainModel(THREE_DOMAIN, false, true),
                result
            )
            assertEquals(
                expected,
                notificationOf {
                    classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)
                }
            )
        }
    }

    @Test
    fun `Given an outcome the screen did not take when it listens again then it is sent again`() =
        runTest {
            getReportOptionsUseCase.givenSuccessfulExecution(
                THREE_DOMAIN,
                ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
            )
            createReportUseCase.givenSuccessfulExecution(
                CreateReportRequestDomainModel(THREE_DOMAIN, false, true),
                CreateReportResultDomainModel.Created("content://r")
            )
            classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)

            assertEquals(
                ReportPresentationNotification.Share("content://r"),
                notificationOf {
                    classUnderTest.onResume(THREE)
                }
            )
        }

    @Test
    fun `When going back then back`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onBackAction()

        assertEquals(ReportPresentationNavigationEvent.OnBack, event.await())
    }

    @Test
    fun `Given a report is being made when Create is tapped again then no second report starts`() {
        classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)
        classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)

        verify(useCaseExecutor, times(1)).execute(
            useCase = eq(createReportUseCase),
            value = any(),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `Given an outcome left from an earlier visit when entering again then it is not sent`() =
        runTest {
            getReportOptionsUseCase.givenSuccessfulExecution(
                THREE_DOMAIN,
                ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
            )
            createReportUseCase.givenSuccessfulExecution(
                CreateReportRequestDomainModel(THREE_DOMAIN, false, true),
                CreateReportResultDomainModel.Created("content://r")
            )
            classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)

            classUnderTest.onEnter(THREE)
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
            classUnderTest.onResume(THREE)

            assertEquals(READY, state.await())
        }

    @Test
    fun `Given a last visit when opened with the default period then since it is chosen`() =
        runTest {
            val august20 = LocalDate.of(2026, 8, 20)
            getReportOptionsUseCase.givenSuccessfulExecution(
                ReportPeriodDomainModel.Default,
                ReportOptionsDomainModel.Ready(
                    ReportDatesDomainModel(august20, LocalDate.of(2026, 10, 25)),
                    true,
                    ReportPeriodDomainModel.SinceLastVisit(august20),
                    august20
                )
            )
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter(ReportPeriodPresentationModel.Default)

            assertEquals(
                ReportViewState.Ready(
                    ReportOptionsPresentationModel(
                        august20,
                        LocalDate.of(2026, 10, 25),
                        anythingLogged = true,
                        period = ReportPeriodPresentationModel.SinceLastVisit(august20),
                        lastVisit = august20,
                        creditShown = true
                    )
                ),
                state.await()
            )
        }

    @Test
    fun `When the credit switch is turned off then saved and shown off`() = runTest {
        setReportCreditUseCase.givenSuccessfulExecution(
            false,
            ReportCreditWriteResultDomainModel.Saved
        )
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN, creditShown = false)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onCreditAction(false, THREE)

        assertEquals(
            ReportViewState.Ready(READY.options.copy(creditShown = false)),
            state.await()
        )
    }

    @Test
    fun `Given the credit switch is not saved then stored choice and message`() = runTest {
        setReportCreditUseCase.givenSuccessfulExecution(
            false,
            ReportCreditWriteResultDomainModel.Failed
        )
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN, creditShown = true)
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        assertEquals(
            ReportPresentationNotification.NotSaved,
            notificationOf { classUnderTest.onCreditAction(false, THREE) }
        )
        assertEquals(READY, state.await())
    }

    @Test
    fun `When creating with the credit switch off then no credit is asked for`() = runTest {
        createReportUseCase.givenSuccessfulExecution(
            CreateReportRequestDomainModel(
                THREE_DOMAIN,
                includeNotes = false,
                includeCredit = false
            ),
            CreateReportResultDomainModel.Created("content://r")
        )
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN)
        )

        assertEquals(
            ReportPresentationNotification.Share("content://r"),
            notificationOf {
                classUnderTest.onCreateAction(
                    THREE,
                    includeNotes = false,
                    includeCredit = false
                )
            }
        )
    }

    /** Each options read waits, with the period it asked for, until the test answers it. */
    private fun readsWait(): MutableList<Pair<ReportPeriodDomainModel, OptionsAnswer>> {
        val reads = mutableListOf<Pair<ReportPeriodDomainModel, OptionsAnswer>>()
        willAnswer {
            reads += it.getArgument<ReportPeriodDomainModel>(1) to
                it.getArgument<OptionsAnswer>(2)
        }.given(useCaseExecutor).execute(
            useCase = eq(getReportOptionsUseCase),
            value = any(),
            onResult = any(),
            onException = any()
        )
        return reads
    }

    private fun TestScope.statesDuring(action: () -> Unit): List<ReportViewState> {
        val states = mutableListOf<ReportViewState>()
        val collector = launch(start = UNDISPATCHED) { classUnderTest.viewState.toList(states) }
        action()
        runCurrent()
        collector.cancel()
        return states
    }

    @Test
    fun `Given an older read answers after a newer one then only the newer period shows`() =
        runTest {
            // A quick change of period never shows stale dates (trap "latest read wins").
            val reads = readsWait()

            val states = statesDuring {
                classUnderTest.onPeriodAction(THREE)
                classUnderTest.onPeriodAction(SIX)
                reads[1].second(ReportOptionsDomainModel.Ready(SIX_DATES, true, SIX_DOMAIN))
                reads[0].second(ReportOptionsDomainModel.Ready(DATES, true, THREE_DOMAIN))
            }

            assertEquals(listOf(READY_SIX), states)
        }

    @Test
    fun `Given another period is chosen while the credit saves then that period is read again`() =
        runTest {
            val reads = readsWait()
            val writes = mutableListOf<(ReportCreditWriteResultDomainModel) -> Unit>()
            willAnswer { writes += it.getArgument<(ReportCreditWriteResultDomainModel) -> Unit>(2) }
                .given(useCaseExecutor).execute(
                    useCase = eq(setReportCreditUseCase),
                    value = eq(false),
                    onResult = any(),
                    onException = any()
                )

            val states = statesDuring {
                classUnderTest.onCreditAction(false, THREE)
                classUnderTest.onPeriodAction(SIX)
                writes.single()(ReportCreditWriteResultDomainModel.Saved)
                reads.forEach { (period, answer) ->
                    val dates = if (period == SIX_DOMAIN) SIX_DATES else DATES
                    answer(ReportOptionsDomainModel.Ready(dates, true, period, creditShown = false))
                }
            }

            assertEquals(listOf(SIX_DOMAIN, SIX_DOMAIN), reads.map { it.first })
            assertEquals(
                ReportViewState.Ready(READY_SIX.options.copy(creditShown = false)),
                states.last()
            )
        }

    @Test
    fun `Given another period is chosen while a report is made then that period is read after`() =
        runTest {
            // The chips are disabled while working, but a tap can land in the frame before (code review 19b2e83d).
            val reads = readsWait()
            val creates = mutableListOf<(CreateReportResultDomainModel) -> Unit>()
            willAnswer { creates += it.getArgument<(CreateReportResultDomainModel) -> Unit>(2) }
                .given(useCaseExecutor).execute(
                    useCase = eq(createReportUseCase),
                    value = any(),
                    onResult = any(),
                    onException = any()
                )

            val states = statesDuring {
                classUnderTest.onCreateAction(THREE, includeNotes = false, includeCredit = true)
                classUnderTest.onPeriodAction(SIX)
                creates.single()(CreateReportResultDomainModel.Created("content://r"))
                reads.forEach { (_, answer) ->
                    answer(ReportOptionsDomainModel.Ready(SIX_DATES, true, SIX_DOMAIN))
                }
            }

            assertEquals(listOf(SIX_DOMAIN), reads.map { it.first })
            assertEquals(READY_SIX, states.last())
        }
}
