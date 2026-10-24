package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetEffectReminderEnabledRepository
import com.migrely.app.medication.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import com.migrely.app.medication.domain.repository.ShowEffectQuestionRepository
import java.time.Instant
import java.time.ZoneId
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-24T11:00:00Z")
private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

class RunEffectQuestionUseCaseTest {
    private val getEffectReminderEnabledRepository: GetEffectReminderEnabledRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository = mock()
    private val showEffectQuestionRepository: ShowEffectQuestionRepository = mock()
    private lateinit var classUnderTest: RunEffectQuestionUseCase

    @Before
    fun setUp() {
        classUnderTest = RunEffectQuestionUseCase(
            getEffectReminderEnabledRepository,
            getNotificationsAllowedRepository,
            getCurrentTimeRepository,
            getPendingEffectQuestionRepository,
            showEffectQuestionRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(true)
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)
        given(getPendingEffectQuestionRepository.pendingEffectQuestion(any(), any())).willReturn(
            PendingEffectQuestionResultDomainModel.Found(
                PendingEffectQuestionDomainModel(
                    "i",
                    "Ibuprofen",
                    "400 mg",
                    NOW.minusSeconds(7_200),
                    TASHKENT
                )
            )
        )
    }

    @Test
    fun `Given an open question when run then the card's question from 2 to 24 hours ago`() {
        classUnderTest.executeInBackground(Unit)

        verify(getPendingEffectQuestionRepository).pendingEffectQuestion(
            takenFrom = Instant.parse("2026-10-23T11:00:00Z"),
            takenTo = Instant.parse("2026-10-24T09:00:00Z")
        )
        verify(showEffectQuestionRepository).showEffectQuestion("i", "Ibuprofen")
    }

    @Test
    fun `Given the setting is off or notifications are off when run then nothing is asked`() {
        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(false)
        classUnderTest.executeInBackground(Unit)

        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(true)
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        classUnderTest.executeInBackground(Unit)

        verify(showEffectQuestionRepository, never()).showEffectQuestion(any(), any())
    }

    @Test
    fun `Given no open question when run then nothing is asked`() {
        given(getPendingEffectQuestionRepository.pendingEffectQuestion(any(), any()))
            .willReturn(PendingEffectQuestionResultDomainModel.None)

        classUnderTest.executeInBackground(Unit)

        verify(showEffectQuestionRepository, never()).showEffectQuestion(any(), any())
    }
}
