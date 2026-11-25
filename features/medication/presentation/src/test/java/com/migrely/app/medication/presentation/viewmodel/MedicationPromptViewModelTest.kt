package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.medication.domain.model.AcknowledgeOveruseRequestDomainModel
import com.migrely.app.medication.domain.model.AnswerEffectRequestDomainModel
import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.usecase.AcknowledgeOveruseUseCase
import com.migrely.app.medication.domain.usecase.AnswerEffectUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationPromptUseCase
import com.migrely.app.medication.domain.usecase.MarkSafetyCardShownUseCase
import com.migrely.app.medication.domain.usecase.SkipEffectQuestionUseCase
import com.migrely.app.medication.presentation.mapper.MedicationPromptDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPromptPresentationMapper
import com.migrely.app.medication.presentation.mapper.OveruseCategoryPresentationMapper
import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.presentation.model.OveruseWarningPresentationModel
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class MedicationPromptViewModelTest :
    BaseViewModelTest<
        MedicationPromptViewState,
        MedicationPromptPresentationNotification,
        MedicationPromptViewModel
        >() {
    @Mock private lateinit var getMedicationPromptUseCase: GetMedicationPromptUseCase

    @Mock private lateinit var answerEffectUseCase: AnswerEffectUseCase

    @Mock private lateinit var skipEffectQuestionUseCase: SkipEffectQuestionUseCase

    @Mock private lateinit var acknowledgeOveruseUseCase: AcknowledgeOveruseUseCase

    @Mock private lateinit var markSafetyCardShownUseCase: MarkSafetyCardShownUseCase

    private val warning = MedicationPromptDomainModel.Overuse(
        OveruseCategoryDomainModel.Triptan,
        OveruseLevelDomainModel.Doctor,
        days = 11
    )

    @Before
    fun setUp() {
        classUnderTest = MedicationPromptViewModel(
            getMedicationPromptUseCase,
            answerEffectUseCase,
            skipEffectQuestionUseCase,
            acknowledgeOveruseUseCase,
            markSafetyCardShownUseCase,
            MedicationPromptPresentationMapper(OveruseCategoryPresentationMapper()),
            MedicationPromptDomainMapper(),
            useCaseExecutor
        )
    }

    @Test
    fun `When entering with a warning due then shows it with its threshold`() = runTest {
        getMedicationPromptUseCase.givenSuccessfulExecution(warning)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(
            MedicationPromptViewState.OveruseWarning(
                OveruseWarningPresentationModel(
                    OveruseCategoryPresentationModel.Triptan,
                    OveruseLevelPresentationModel.Doctor,
                    days = 11
                )
            ),
            state.await()
        )
    }

    @Test
    fun `When a load shows an overuse warning then its moment is kept (D188)`() {
        getMedicationPromptUseCase.givenSuccessfulExecution(warning)

        classUnderTest.onEnter()

        verify(useCaseExecutor).execute(
            useCase = eq(markSafetyCardShownUseCase),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When a load shows no card then no safety card is marked`() {
        getMedicationPromptUseCase.givenSuccessfulExecution(MedicationPromptDomainModel.None)

        classUnderTest.onEnter()

        verify(useCaseExecutor, never()).execute(
            useCase = eq(markSafetyCardShownUseCase),
            onResult = any(),
            onException = any()
        )
    }

    @Test
    fun `When answering then stores the answer and loads the next card`() = runTest {
        answerEffectUseCase.givenSuccessfulExecution(
            AnswerEffectRequestDomainModel("i", IntakeEffectDomainModel.Completely),
            WriteResultDomainModel.Done
        )
        getMedicationPromptUseCase.givenSuccessfulExecution(MedicationPromptDomainModel.None)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEffectAction("i", IntakeEffectPresentationModel.Completely)

        assertEquals(MedicationPromptViewState.Hidden, state.await())
    }

    @Test
    fun `Given the answer fails when answering then loads the same card again`() = runTest {
        answerEffectUseCase.givenSuccessfulExecution(
            AnswerEffectRequestDomainModel("i", IntakeEffectDomainModel.NotAtAll),
            WriteResultDomainModel.Failed
        )
        getMedicationPromptUseCase.givenSuccessfulExecution(warning)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEffectAction("i", IntakeEffectPresentationModel.NotAtAll)

        assertEquals(
            MedicationPromptViewState.OveruseWarning(
                OveruseWarningPresentationModel(
                    OveruseCategoryPresentationModel.Triptan,
                    OveruseLevelPresentationModel.Doctor,
                    days = 11
                )
            ),
            state.await()
        )
    }

    @Test
    fun `When skipping then closes the question and loads the next card`() = runTest {
        skipEffectQuestionUseCase.givenSuccessfulExecution("i", WriteResultDomainModel.Done)
        getMedicationPromptUseCase.givenSuccessfulExecution(MedicationPromptDomainModel.None)
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onSkipEffectAction("i")

        assertEquals(MedicationPromptViewState.Hidden, state.await())
    }

    @Test
    fun `When tapping Got it then acknowledges that category and level and loads again`() =
        runTest {
            acknowledgeOveruseUseCase.givenSuccessfulExecution(
                AcknowledgeOveruseRequestDomainModel(
                    OveruseCategoryDomainModel.Multiple,
                    OveruseLevelDomainModel.Soft
                ),
                WriteResultDomainModel.Done
            )
            getMedicationPromptUseCase.givenSuccessfulExecution(MedicationPromptDomainModel.None)
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onOveruseSeenAction(
                OveruseCategoryPresentationModel.Multiple,
                OveruseLevelPresentationModel.Soft
            )

            assertEquals(MedicationPromptViewState.Hidden, state.await())
        }
}
