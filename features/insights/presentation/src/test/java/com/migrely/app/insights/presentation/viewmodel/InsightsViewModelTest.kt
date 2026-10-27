package com.migrely.app.insights.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.insights.domain.model.InsightsDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsResultDomainModel
import com.migrely.app.insights.domain.usecase.GetInsightsUseCase
import com.migrely.app.insights.presentation.mapper.InsightsPresentationMapper
import com.migrely.app.insights.presentation.mapper.InsightsTriggerPresentationMapper
import com.migrely.app.insights.presentation.model.InsightsPatternsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationModel
import com.migrely.app.insights.presentation.model.InsightsPresentationNotification
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.navigation.InsightsPresentationNavigationEvent
import java.time.LocalDate
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
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

private val TODAY: LocalDate = LocalDate.of(2026, 10, 25)

private fun result(months: Int, missingMigraines: Int) = InsightsResultDomainModel.Ready(
    InsightsDomainModel(
        months,
        InsightsDatesDomainModel(TODAY.minusMonths(months.toLong()).plusDays(1), TODAY),
        InsightsPatternsDomainModel.NotEnough(missingMigraines, 0),
        emptyList()
    )
)

private fun ready(months: Int, missingMigraines: Int) = InsightsViewState.Ready(
    InsightsPresentationModel(
        months,
        InsightsPatternsPresentationModel.NotEnough(missingMigraines, 0),
        emptyList()
    )
)

@RunWith(MockitoJUnitRunner::class)
class InsightsViewModelTest :
    BaseViewModelTest<InsightsViewState, InsightsPresentationNotification, InsightsViewModel>() {
    @Mock private lateinit var getInsightsUseCase: GetInsightsUseCase

    @Before
    fun setUp() {
        classUnderTest = InsightsViewModel(
            getInsightsUseCase,
            InsightsPresentationMapper(InsightsTriggerPresentationMapper()),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then the insights of the chosen months`() = runTest {
        getInsightsUseCase.givenSuccessfulExecution(6, result(6, missingMigraines = 2))
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter(6)

        assertEquals(ready(6, missingMigraines = 2), state.await())
    }

    @Test
    fun `When choosing another period then its insights`() = runTest {
        getInsightsUseCase.givenSuccessfulExecution(3, result(3, missingMigraines = 4))
        getInsightsUseCase.givenSuccessfulExecution(12, result(12, missingMigraines = 0))
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onPeriodAction(3)
        classUnderTest.onPeriodAction(12)

        assertEquals(
            listOf(ready(3, missingMigraines = 4), ready(12, missingMigraines = 0)),
            states.await()
        )
    }

    @Test
    fun `When the screen shows again then the insights are read again`() = runTest {
        getInsightsUseCase.givenSuccessfulExecution(6, result(6, missingMigraines = 1))
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onResume(6)

        assertEquals(ready(6, missingMigraines = 1), state.await())
    }

    @Test
    fun `Given the records cannot be read then unavailable`() = runTest {
        getInsightsUseCase.givenSuccessfulExecution(6, InsightsResultDomainModel.Failed)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter(6)

        assertEquals(InsightsViewState.Unavailable, state.await())
    }

    @Test
    fun `When going back then back`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onBackAction()

        assertEquals(InsightsPresentationNavigationEvent.OnBack, event.await())
    }
}
