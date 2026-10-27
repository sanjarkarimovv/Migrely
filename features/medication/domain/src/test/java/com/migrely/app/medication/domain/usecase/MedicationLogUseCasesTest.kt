package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.IntakeDomainModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import com.migrely.app.medication.domain.model.LogIntakeRequestDomainModel
import com.migrely.app.medication.domain.model.LogIntakeResultDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLogDomainModel
import com.migrely.app.medication.domain.model.MedicationLogResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.ScheduleEffectQuestionRepository
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val NOW: Instant = Instant.parse("2026-10-24T09:20:42Z")
private val START: Instant = Instant.parse("2026-10-24T05:10:00Z")
private val END: Instant = Instant.parse("2026-10-24T07:00:00Z")
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")

private val PREVENTIVE = MedicationDomainModel(
    "p",
    "Topiramate",
    MedicationKindDomainModel.Preventive,
    null,
    null,
    false
)
private val ACUTE = MedicationDomainModel(
    "a",
    "Ibuprofen",
    MedicationKindDomainModel.Acute,
    MedicationCategoryDomainModel.SimplePainkiller,
    "400 mg",
    false
)

class GetMedicationLogUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttackRepository: GetAttackRepository = mock()
    private val getActiveMedicationsRepository: GetActiveMedicationsRepository = mock()
    private lateinit var classUnderTest: GetMedicationLogUseCase

    @Before
    fun setUp() {
        classUnderTest = GetMedicationLogUseCase(
            getCurrentTimeRepository,
            getAttackRepository,
            getActiveMedicationsRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime()).willReturn(CurrentTimeDomainModel(NOW, ZONE))
        given(getActiveMedicationsRepository.activeMedications())
            .willReturn(MedicationsResultDomainModel.Loaded(listOf(PREVENTIVE, ACUTE)))
    }

    @Test
    fun `Given no attack when loading then now to the minute and acute first`() {
        assertEquals(
            MedicationLogResultDomainModel.Loaded(
                MedicationLogDomainModel(
                    Instant.parse("2026-10-24T09:20:00Z"),
                    ZONE,
                    null,
                    listOf(ACUTE, PREVENTIVE)
                )
            ),
            classUnderTest.executeInBackground(null)
        )
    }

    @Test
    fun `Given an attack in progress when loading then now and the attack`() {
        given(getAttackRepository.attack("x"))
            .willReturn(
                AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("x", START, null, ZONE))
            )

        val actual = classUnderTest.executeInBackground(
            "x"
        ) as MedicationLogResultDomainModel.Loaded

        assertEquals(Instant.parse("2026-10-24T09:20:00Z"), actual.log.defaultTime)
        assertEquals("x", actual.log.attackId)
    }

    @Test
    fun `Given an ended attack recorded in Tokyo when loading then its start in Tokyo`() {
        given(getAttackRepository.attack("x"))
            .willReturn(
                AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("x", START, END, TOKYO))
            )

        val actual = classUnderTest.executeInBackground(
            "x"
        ) as MedicationLogResultDomainModel.Loaded

        assertEquals(START, actual.log.defaultTime)
        assertEquals(TOKYO, actual.log.zone)
    }

    @Test
    fun `Given the attack no longer exists when loading then logged on its own at now`() {
        given(getAttackRepository.attack("x")).willReturn(AttackLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground(
            "x"
        ) as MedicationLogResultDomainModel.Loaded

        assertEquals(null, actual.log.attackId)
    }

    @Test
    fun `Given the medications cannot be read when loading then failed`() {
        given(getActiveMedicationsRepository.activeMedications())
            .willReturn(MedicationsResultDomainModel.Failed)

        assertEquals(
            MedicationLogResultDomainModel.Failed,
            classUnderTest.executeInBackground(null)
        )
    }

    @Test
    fun `Given the attack cannot be read when loading then failed`() {
        given(getAttackRepository.attack("x")).willReturn(AttackLookupResultDomainModel.Failed)

        assertEquals(MedicationLogResultDomainModel.Failed, classUnderTest.executeInBackground("x"))
    }
}

class LogIntakeUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttackRepository: GetAttackRepository = mock()
    private val findAttackAtRepository: FindAttackAtRepository = mock()
    private val addIntakeRepository: AddIntakeRepository = mock()
    private val scheduleEffectQuestionRepository: ScheduleEffectQuestionRepository = mock()
    private lateinit var classUnderTest: LogIntakeUseCase

    @Before
    fun setUp() {
        classUnderTest = LogIntakeUseCase(
            getCurrentTimeRepository,
            getAttackRepository,
            findAttackAtRepository,
            addIntakeRepository,
            scheduleEffectQuestionRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime()).willReturn(CurrentTimeDomainModel(NOW, ZONE))
        given(findAttackAtRepository.attackDuring(any(), any()))
            .willReturn(AttackLookupResultDomainModel.None)
        given(getAttackRepository.attack("x")).willReturn(
            AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("x", START, null, ZONE))
        )
    }

    private fun request(at: Instant, attackId: String? = null) =
        LogIntakeRequestDomainModel("a", "Ibuprofen", "400 mg", attackId, at)

    private fun givenAdded() {
        given(addIntakeRepository.addIntake(any(), anyOrNull(), any(), any(), anyOrNull()))
            .willReturn(AddIntakeResultDomainModel.Added("i"))
    }

    @Test
    fun `Given an attack when logging then linked to it without a lookup`() {
        givenAdded()

        val actual = classUnderTest.executeInBackground(request(NOW, attackId = "x"))

        assertEquals(
            LogIntakeResultDomainModel.Logged(
                IntakeDomainModel("i", "a", "Ibuprofen", "400 mg", NOW, ZONE, "x")
            ),
            actual
        )
        verify(findAttackAtRepository, never()).attackDuring(any(), any())
    }

    @Test
    fun `When an intake is logged then its question is planned two hours after it`() {
        givenAdded()

        classUnderTest.executeInBackground(request(NOW))

        verify(scheduleEffectQuestionRepository)
            .scheduleEffectQuestion("i", NOW.plus(java.time.Duration.ofHours(2)))
    }

    @Test
    fun `When an intake is logged more than two hours late then no notification is planned`() {
        givenAdded()

        classUnderTest.executeInBackground(request(NOW.minus(java.time.Duration.ofHours(3))))

        verify(scheduleEffectQuestionRepository, never()).scheduleEffectQuestion(any(), any())
    }

    @Test
    fun `Given an attack recorded in Tokyo when logging for it then stored in Tokyo`() {
        givenAdded()
        given(getAttackRepository.attack("x")).willReturn(
            AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("x", START, END, TOKYO))
        )

        classUnderTest.executeInBackground(request(START, attackId = "x"))

        verify(addIntakeRepository).addIntake("a", "x", START, TOKYO, "400 mg")
    }

    @Test
    fun `Given no attack when logging then the attack overlapping the picked minute`() {
        givenAdded()
        val minute = START.plusSeconds(60)
        given(findAttackAtRepository.attackDuring(START, minute))
            .willReturn(
                AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("y", START, END, TOKYO))
            )

        classUnderTest.executeInBackground(request(START))

        // Picked on home, in the device's zone, even when the attack started elsewhere.
        verify(addIntakeRepository).addIntake("a", "y", START, ZONE, "400 mg")
    }

    @Test
    fun `Given no attack at the time when logging then stored on its own`() {
        givenAdded()

        val actual = classUnderTest.executeInBackground(
            request(START)
        ) as LogIntakeResultDomainModel.Logged

        assertEquals(null, actual.intake.attackId)
    }

    @Test
    fun `Given a time 5 minutes ahead when logging then accepted, 6 minutes then refused`() {
        givenAdded()

        assertEquals(
            true,
            classUnderTest.executeInBackground(
                request(NOW.plusSeconds(300))
            ) is LogIntakeResultDomainModel.Logged
        )
        assertEquals(
            LogIntakeResultDomainModel.InFuture,
            classUnderTest.executeInBackground(request(NOW.plusSeconds(301)))
        )
    }

    @Test
    fun `Given the attack lookup fails when logging then failed`() {
        given(
            findAttackAtRepository.attackDuring(any(), any())
        ).willReturn(AttackLookupResultDomainModel.Failed)

        assertEquals(
            LogIntakeResultDomainModel.Failed,
            classUnderTest.executeInBackground(request(START))
        )
    }

    @Test
    fun `Given the disk is full when logging then storage full`() {
        given(addIntakeRepository.addIntake(any(), anyOrNull(), any(), any(), anyOrNull()))
            .willReturn(AddIntakeResultDomainModel.StorageFull)

        assertEquals(
            LogIntakeResultDomainModel.StorageFull,
            classUnderTest.executeInBackground(request(NOW))
        )
    }
}
