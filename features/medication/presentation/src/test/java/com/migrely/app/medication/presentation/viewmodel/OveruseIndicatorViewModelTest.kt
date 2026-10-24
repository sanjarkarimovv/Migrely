package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusResultDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusRowDomainModel
import com.migrely.app.medication.domain.usecase.GetOveruseStatusUseCase
import com.migrely.app.medication.presentation.mapper.OveruseCategoryPresentationMapper
import com.migrely.app.medication.presentation.mapper.OveruseIndicatorPresentationMapper
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorPresentationNotification
import com.migrely.app.medication.presentation.model.OveruseIndicatorRowPresentationModel
import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState
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
class OveruseIndicatorViewModelTest :
    BaseViewModelTest<
        OveruseIndicatorViewState,
        OveruseIndicatorPresentationNotification,
        OveruseIndicatorViewModel
        >() {
    @Mock private lateinit var getOveruseStatusUseCase: GetOveruseStatusUseCase

    @Before
    fun setUp() {
        classUnderTest = OveruseIndicatorViewModel(
            getOveruseStatusUseCase,
            OveruseIndicatorPresentationMapper(OveruseCategoryPresentationMapper()),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering then each row with whether its limit is reached`() = runTest {
        getOveruseStatusUseCase.givenSuccessfulExecution(
            OveruseStatusResultDomainModel.Loaded(
                listOf(
                    OveruseStatusRowDomainModel(OveruseCategoryDomainModel.SimplePainkiller, 6, 15),
                    OveruseStatusRowDomainModel(OveruseCategoryDomainModel.Triptan, 10, 10)
                )
            )
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(
            OveruseIndicatorViewState.Shown(
                listOf(
                    OveruseIndicatorRowPresentationModel(
                        OveruseCategoryPresentationModel.SimplePainkiller,
                        days = 6,
                        limit = 15,
                        reached = false
                    ),
                    OveruseIndicatorRowPresentationModel(
                        OveruseCategoryPresentationModel.Triptan,
                        days = 10,
                        limit = 10,
                        reached = true
                    )
                )
            ),
            state.await()
        )
    }

    @Test
    fun `Given the status cannot be read when entering then hidden`() = runTest {
        getOveruseStatusUseCase.givenSuccessfulExecution(OveruseStatusResultDomainModel.Failed)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(OveruseIndicatorViewState.Hidden, state.await())
    }

    @Test
    fun `Given no attack medication when entering then hidden`() = runTest {
        getOveruseStatusUseCase.givenSuccessfulExecution(
            OveruseStatusResultDomainModel.Loaded(emptyList())
        )
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(OveruseIndicatorViewState.Hidden, state.await())
    }
}
