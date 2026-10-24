package com.migrely.app.history.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.presentation.mapper.HistoryAttackPresentationMapper
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.history.presentation.model.MonthSkyPresentationNotification
import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val OCTOBER = HistoryMonthDomainModel(
    month = YearMonth.of(2026, 10),
    today = LocalDate.parse("2026-10-23"),
    earliestMonth = YearMonth.of(2026, 8),
    anyAttackLogged = true,
    days = emptyList(),
    attacks = emptyList()
)

@RunWith(MockitoJUnitRunner::class)
class MonthSkyViewModelTest :
    BaseViewModelTest<MonthSkyViewState, MonthSkyPresentationNotification, MonthSkyViewModel>() {
    @Mock private lateinit var getHistoryMonthUseCase: GetHistoryMonthUseCase

    private val historyMonthPresentationMapper = HistoryMonthPresentationMapper(
        HistoryAttackPresentationMapper(),
        IntensityLevelPresentationMapper()
    )

    @Before
    fun setUp() {
        classUnderTest = MonthSkyViewModel(
            getHistoryMonthUseCase,
            historyMonthPresentationMapper,
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then shows the current month`() = runTest {
        getHistoryMonthUseCase.givenSuccessfulExecution(
            null,
            HistoryMonthResultDomainModel.Loaded(OCTOBER)
        )
        val state = nextViewState()

        classUnderTest.onEnter()

        assertEquals(
            MonthSkyViewState.Ready(historyMonthPresentationMapper.toPresentation(OCTOBER)),
            state.await()
        )
    }

    @Test
    fun `Given the month cannot be read when entering then unavailable`() = runTest {
        getHistoryMonthUseCase.givenSuccessfulExecution(null, HistoryMonthResultDomainModel.Failed)
        val state = nextViewState()

        classUnderTest.onEnter()

        assertEquals(MonthSkyViewState.Unavailable, state.await())
    }

    @Test
    fun `When choosing the month then opens History`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onMonthAction()

        assertEquals(MonthSkyPresentationNavigationEvent.OnViewMonth, event.await())
    }

    @Test
    fun `When choosing a day then opens History on that day`() = runTest {
        val day = LocalDate.parse("2026-10-14")
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onDayAction(day)

        assertEquals(MonthSkyPresentationNavigationEvent.OnViewDay(day), event.await())
    }

    private fun TestScope.nextViewState() = async(start = UNDISPATCHED) {
        classUnderTest.viewState.first()
    }
}
