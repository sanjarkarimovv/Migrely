package com.migrely.app.report.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.domain.usecase.CreateReportUseCase
import com.migrely.app.report.domain.usecase.GetReportOptionsUseCase
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val THREE = ReportPeriodPresentationModel.LastMonths(3)
private val THREE_DOMAIN = ReportPeriodDomainModel.LastMonths(3)
private val DATES = ReportDatesDomainModel(LocalDate.of(2026, 7, 26), LocalDate.of(2026, 10, 25))
private val READY = ReportViewState.Ready(
    ReportOptionsPresentationModel(
        LocalDate.of(2026, 7, 26),
        LocalDate.of(2026, 10, 25),
        anythingLogged = true
    )
)

@RunWith(MockitoJUnitRunner::class)
class ReportViewModelTest :
    BaseViewModelTest<ReportViewState, ReportPresentationNotification, ReportViewModel>() {
    @Mock private lateinit var getReportOptionsUseCase: GetReportOptionsUseCase

    @Mock private lateinit var createReportUseCase: CreateReportUseCase

    @Before
    fun setUp() {
        classUnderTest = ReportViewModel(
            getReportOptionsUseCase,
            createReportUseCase,
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
                ReportOptionsDomainModel.Ready(DATES, true)
            )
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter(THREE)

            assertEquals(READY, state.await())
        }

    @Test
    fun `Given the options cannot be read then unavailable`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Failed
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onPeriodAction(THREE)

        assertEquals(ReportViewState.Unavailable, state.await())
    }

    @Test
    fun `When creating then working, then share it and read the options again`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true)
        )
        createReportUseCase.givenSuccessfulExecution(
            CreateReportRequestDomainModel(THREE_DOMAIN, includeNotes = true),
            CreateReportResultDomainModel.Created("content://r")
        )
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            ReportPresentationNotification.Share("content://r"),
            notificationOf { classUnderTest.onCreateAction(THREE, includeNotes = true) }
        )
        assertEquals(listOf(ReportViewState.Working, READY), states.await())
    }

    @Test
    fun `Given the report cannot be made then why`() = runTest {
        getReportOptionsUseCase.givenSuccessfulExecution(
            THREE_DOMAIN,
            ReportOptionsDomainModel.Ready(DATES, true)
        )
        mapOf(
            CreateReportResultDomainModel.NothingLogged to
                ReportPresentationNotification.NothingLogged,
            CreateReportResultDomainModel.StorageFull to ReportPresentationNotification.StorageFull,
            CreateReportResultDomainModel.Failed to
                ReportPresentationNotification.SomethingWentWrong
        ).forEach { (result, expected) ->
            createReportUseCase.givenSuccessfulExecution(
                CreateReportRequestDomainModel(THREE_DOMAIN, false),
                result
            )
            assertEquals(
                expected,
                notificationOf {
                    classUnderTest.onCreateAction(THREE, includeNotes = false)
                }
            )
        }
    }

    @Test
    fun `Given an outcome the screen did not take when it listens again then it is sent again`() =
        runTest {
            getReportOptionsUseCase.givenSuccessfulExecution(
                THREE_DOMAIN,
                ReportOptionsDomainModel.Ready(DATES, true)
            )
            createReportUseCase.givenSuccessfulExecution(
                CreateReportRequestDomainModel(THREE_DOMAIN, false),
                CreateReportResultDomainModel.Created("content://r")
            )
            classUnderTest.onCreateAction(THREE, includeNotes = false)

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
}
