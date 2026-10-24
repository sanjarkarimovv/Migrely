package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetEffectReminderEnabledRepository
import com.migrely.app.medication.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionOfRepository
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
    private val findAttackAtRepository: FindAttackAtRepository = mock()
    private val getPendingEffectQuestionOfRepository: GetPendingEffectQuestionOfRepository = mock()
    private val showEffectQuestionRepository: ShowEffectQuestionRepository = mock()
    private lateinit var classUnderTest: RunEffectQuestionUseCase

    @Before
    fun setUp() {
        classUnderTest = RunEffectQuestionUseCase(
            getEffectReminderEnabledRepository,
            getNotificationsAllowedRepository,
            getCurrentTimeRepository,
            findAttackAtRepository,
            getPendingEffectQuestionOfRepository,
            showEffectQuestionRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(true)
        given(
            findAttackAtRepository.attackDuring(any(), any())
        ).willReturn(AttackLookupResultDomainModel.None)
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)
        given(
            getPendingEffectQuestionOfRepository.pendingEffectQuestionOf(any(), any(), any())
        ).willReturn(
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
    fun `Given its question is open when run then that intake is asked about`() {
        classUnderTest.executeInBackground("i")

        verify(getPendingEffectQuestionOfRepository).pendingEffectQuestionOf(
            intakeId = "i",
            takenFrom = Instant.parse("2026-10-23T11:00:00Z"),
            takenTo = Instant.parse("2026-10-24T09:00:00Z")
        )
        verify(showEffectQuestionRepository).showEffectQuestion("i", "Ibuprofen")
    }

    @Test
    fun `Given the setting is off or notifications are off when run then nothing is asked`() {
        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(false)
        classUnderTest.executeInBackground("i")

        given(getEffectReminderEnabledRepository.effectReminderEnabled()).willReturn(true)
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        classUnderTest.executeInBackground("i")

        verify(showEffectQuestionRepository, never()).showEffectQuestion(any(), any())
    }

    @Test
    fun `Given that intake's question is closed or not acute when run then nothing is asked`() {
        given(getPendingEffectQuestionOfRepository.pendingEffectQuestionOf(any(), any(), any()))
            .willReturn(PendingEffectQuestionResultDomainModel.None)

        classUnderTest.executeInBackground("i")

        verify(showEffectQuestionRepository, never()).showEffectQuestion(any(), any())
    }

    @Test
    fun `Given an attack in progress when run then nothing is sent`() {
        given(findAttackAtRepository.attackDuring(any(), any())).willReturn(
            AttackLookupResultDomainModel.Found(
                LinkedAttackDomainModel("a", NOW.minusSeconds(9_000), null, TASHKENT)
            )
        )

        classUnderTest.executeInBackground("i")

        verify(showEffectQuestionRepository, never()).showEffectQuestion(any(), any())
    }
}
