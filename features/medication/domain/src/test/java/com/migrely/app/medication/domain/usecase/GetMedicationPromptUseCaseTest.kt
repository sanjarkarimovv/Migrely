package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticeDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticesResultDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetOveruseNoticesRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

// 16:00 on Saturday 31 October 2026 in Tashkent (UTC+5).
private val NOW: Instant = Instant.parse("2026-10-31T11:00:00Z")
private val TODAY: LocalDate = LocalDate.of(2026, 10, 31)

class GetMedicationPromptUseCaseTest {
    private lateinit var classUnderTest: GetMedicationPromptUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAcuteIntakesRepository: GetAcuteIntakesRepository = mock()
    private val getOveruseNoticesRepository: GetOveruseNoticesRepository = mock()
    private val getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository = mock()

    private val question = PendingEffectQuestionDomainModel(
        intakeId = "i",
        medicationName = "Ibuprofen",
        dose = "400 mg",
        takenAt = NOW.minus(Duration.ofHours(3)),
        zone = TASHKENT
    )

    @Before
    fun setUp() {
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
        givenIntakes()
        givenNotices()
        given(getPendingEffectQuestionRepository.pendingEffectQuestion(any(), any()))
            .willReturn(PendingEffectQuestionResultDomainModel.None)
        classUnderTest = GetMedicationPromptUseCase(
            getCurrentTimeRepository,
            getAcuteIntakesRepository,
            getOveruseNoticesRepository,
            getPendingEffectQuestionRepository,
            CoroutineContextProvider.Default
        )
    }

    @Test
    fun `Given nothing due when executed then no card`() {
        assertEquals(MedicationPromptDomainModel.None, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `When executed then asks for intakes taken 2 to 24 hours ago`() {
        classUnderTest.executeInBackground(Unit)

        verify(getPendingEffectQuestionRepository).pendingEffectQuestion(
            NOW.minus(Duration.ofHours(24)),
            NOW.minus(Duration.ofHours(2))
        )
    }

    @Test
    fun `Given an intake waiting and no warning when executed then the question`() {
        givenQuestion()

        assertEquals(
            MedicationPromptDomainModel.EffectQuestion(question),
            classUnderTest.executeInBackground(Unit)
        )
    }

    @Test
    fun `When executed then reads intakes of the 30 days with two days on each side`() {
        classUnderTest.executeInBackground(Unit)

        // From 30 September 00:00 to 3 November 00:00 in Tashkent.
        verify(getAcuteIntakesRepository).acuteIntakesTakenBetween(
            Instant.parse("2026-09-29T19:00:00Z"),
            Instant.parse("2026-11-02T19:00:00Z")
        )
    }

    @Test
    fun `Given simple painkillers on 13 days when executed then the soft warning`() {
        givenIntakes(days(13, MedicationCategoryDomainModel.SimplePainkiller))

        assertEquals(
            overuse(OveruseCategoryDomainModel.SimplePainkiller, soft = true, days = 13),
            execute()
        )
    }

    @Test
    fun `Given triptans on 10 days when executed then the doctor warning`() {
        givenIntakes(days(10, MedicationCategoryDomainModel.Triptan))

        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given triptans on 7 days only when executed then no warning`() {
        givenIntakes(days(7, MedicationCategoryDomainModel.Triptan))

        assertEquals(MedicationPromptDomainModel.None, execute())
    }

    @Test
    fun `Given three intakes on one day when executed then it counts once`() {
        givenIntakes(
            days(7, MedicationCategoryDomainModel.Triptan) +
                List(3) { intake(TODAY.minusDays(20), MedicationCategoryDomainModel.Triptan) }
        )

        assertEquals(overuse(OveruseCategoryDomainModel.Triptan, soft = true, days = 8), execute())
    }

    @Test
    fun `Given Not sure medications on 8 days when executed then combination or other`() {
        givenIntakes(days(8, category = null))

        assertEquals(
            overuse(OveruseCategoryDomainModel.CombinationOrOther, soft = true, days = 8),
            execute()
        )
    }

    @Test
    fun `Given two categories on the same days when executed then each counts its days`() {
        givenIntakes(
            days(10, MedicationCategoryDomainModel.Triptan) +
                days(10, MedicationCategoryDomainModel.SimplePainkiller)
        )

        // Triptans reach their doctor level; simple painkillers (10 of 15) are below soft, and all together (10) are not
        // higher than the triptans' level.
        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given painkillers on 6 days and triptans on 4 others then all together`() {
        givenIntakes(
            days(6, MedicationCategoryDomainModel.SimplePainkiller) +
                days(4, MedicationCategoryDomainModel.Triptan, firstDaysAgo = 10)
        )

        assertEquals(
            overuse(OveruseCategoryDomainModel.Multiple, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given painkillers on 15 days and triptans on 2 then the painkiller card`() {
        givenIntakes(
            days(15, MedicationCategoryDomainModel.SimplePainkiller) +
                days(2, MedicationCategoryDomainModel.Triptan, firstDaysAgo = 20)
        )

        assertEquals(
            overuse(OveruseCategoryDomainModel.SimplePainkiller, soft = false, days = 15),
            execute()
        )
    }

    @Test
    fun `Given a soft and a doctor level when executed then only the doctor level`() {
        givenIntakes(
            days(13, MedicationCategoryDomainModel.SimplePainkiller) +
                days(10, MedicationCategoryDomainModel.Triptan)
        )

        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given two doctor levels when executed then the one with more days`() {
        givenIntakes(
            days(10, MedicationCategoryDomainModel.Triptan) +
                days(11, category = null)
        )

        assertEquals(
            overuse(OveruseCategoryDomainModel.CombinationOrOther, soft = false, days = 11),
            execute()
        )
    }

    @Test
    fun `Given intakes on the 30th and 31st day back when executed then only the 30th counts`() {
        // 2 October is the first day of the window ending 31 October; 1 October is outside it.
        givenIntakes(
            days(9, MedicationCategoryDomainModel.Triptan) +
                intake(LocalDate.of(2026, 10, 1), MedicationCategoryDomainModel.Triptan)
        )
        assertEquals(overuse(OveruseCategoryDomainModel.Triptan, soft = true, days = 9), execute())

        givenIntakes(
            days(9, MedicationCategoryDomainModel.Triptan) +
                intake(LocalDate.of(2026, 10, 2), MedicationCategoryDomainModel.Triptan)
        )
        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given an intake late in another zone then it counts on its own date`() {
        // 23:30 on 1 October in Berlin is 02:30 on 2 October in Tashkent: it is 1 October, outside the window.
        val berlin = ZoneId.of("Europe/Berlin")
        givenIntakes(
            days(9, MedicationCategoryDomainModel.Triptan) +
                AcuteIntakeDomainModel(
                    LocalDate.of(2026, 10, 1).atTime(23, 30).atZone(berlin).toInstant(),
                    berlin,
                    MedicationCategoryDomainModel.Triptan
                )
        )

        assertEquals(overuse(OveruseCategoryDomainModel.Triptan, soft = true, days = 9), execute())
    }

    @Test
    fun `Given a doctor warning and a waiting question when executed then the warning first`() {
        givenIntakes(days(10, MedicationCategoryDomainModel.Triptan))
        givenQuestion()

        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given a soft warning and a waiting question when executed then the warning first`() {
        givenIntakes(days(8, MedicationCategoryDomainModel.Triptan))
        givenQuestion()

        assertEquals(overuse(OveruseCategoryDomainModel.Triptan, soft = true, days = 8), execute())
    }

    @Test
    fun `Given Got it on the doctor warning 10 days ago when executed then the question instead`() {
        givenIntakes(days(12, MedicationCategoryDomainModel.Triptan))
        givenNotices(notice(OveruseCategoryDomainModel.Triptan, OveruseLevelDomainModel.Doctor, 10))
        givenQuestion()

        assertEquals(MedicationPromptDomainModel.EffectQuestion(question), execute())
    }

    @Test
    fun `Given Got it on the doctor warning when the level is only soft then still quiet`() {
        givenIntakes(days(8, MedicationCategoryDomainModel.Triptan))
        givenNotices(notice(OveruseCategoryDomainModel.Triptan, OveruseLevelDomainModel.Doctor, 1))

        assertEquals(MedicationPromptDomainModel.None, execute())
    }

    @Test
    fun `Given Got it on soft when the doctor level is reached then the doctor card`() {
        givenIntakes(days(15, MedicationCategoryDomainModel.SimplePainkiller))
        givenNotices(
            notice(OveruseCategoryDomainModel.SimplePainkiller, OveruseLevelDomainModel.Soft, 2)
        )

        assertEquals(
            overuse(OveruseCategoryDomainModel.SimplePainkiller, soft = false, days = 15),
            execute()
        )
    }

    @Test
    fun `Given Got it exactly 30 days ago when executed then the warning again`() {
        givenIntakes(days(10, MedicationCategoryDomainModel.Triptan))
        givenNotices(notice(OveruseCategoryDomainModel.Triptan, OveruseLevelDomainModel.Doctor, 30))

        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given Got it on another category when executed then the warning`() {
        givenIntakes(days(10, MedicationCategoryDomainModel.Triptan))
        givenNotices(notice(OveruseCategoryDomainModel.Multiple, OveruseLevelDomainModel.Doctor, 1))

        assertEquals(
            overuse(OveruseCategoryDomainModel.Triptan, soft = false, days = 10),
            execute()
        )
    }

    @Test
    fun `Given the doctor level is quiet when a soft level applies elsewhere then nothing`() {
        // Only the highest level shows, even when it was acknowledged (BRD D45).
        givenIntakes(
            days(10, MedicationCategoryDomainModel.Triptan) +
                days(13, MedicationCategoryDomainModel.SimplePainkiller)
        )
        givenNotices(notice(OveruseCategoryDomainModel.Triptan, OveruseLevelDomainModel.Doctor, 3))

        assertEquals(MedicationPromptDomainModel.None, execute())
    }

    @Test
    fun `Given the intakes cannot be read when executed then the question`() {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Failed)
        givenQuestion()

        assertEquals(MedicationPromptDomainModel.EffectQuestion(question), execute())
    }

    @Test
    fun `Given the notices cannot be read when executed then no warning`() {
        givenIntakes(days(10, MedicationCategoryDomainModel.Triptan))
        given(getOveruseNoticesRepository.overuseNotices())
            .willReturn(OveruseNoticesResultDomainModel.Failed)

        assertEquals(MedicationPromptDomainModel.None, execute())
    }

    @Test
    fun `Given the question cannot be read when executed then no card`() {
        given(getPendingEffectQuestionRepository.pendingEffectQuestion(any(), any()))
            .willReturn(PendingEffectQuestionResultDomainModel.Failed)

        assertEquals(MedicationPromptDomainModel.None, execute())
    }

    private fun execute() = classUnderTest.executeInBackground(Unit)

    private fun overuse(category: OveruseCategoryDomainModel, soft: Boolean, days: Int) =
        MedicationPromptDomainModel.Overuse(
            category,
            if (soft) OveruseLevelDomainModel.Soft else OveruseLevelDomainModel.Doctor,
            days
        )

    /** One intake at noon on each of [count] consecutive days, starting [firstDaysAgo] days before today. */
    private fun days(count: Int, category: MedicationCategoryDomainModel?, firstDaysAgo: Long = 0) =
        (0 until count).map { intake(TODAY.minusDays(firstDaysAgo + it), category) }

    private fun intake(date: LocalDate, category: MedicationCategoryDomainModel?) =
        AcuteIntakeDomainModel(date.atTime(12, 0).atZone(TASHKENT).toInstant(), TASHKENT, category)

    private fun notice(
        category: OveruseCategoryDomainModel,
        level: OveruseLevelDomainModel,
        daysAgo: Long
    ) = OveruseNoticeDomainModel(category, level, NOW.minus(Duration.ofDays(daysAgo)))

    private fun givenIntakes(intakes: List<AcuteIntakeDomainModel> = emptyList()) {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Loaded(intakes))
    }

    private fun givenNotices(vararg notices: OveruseNoticeDomainModel) {
        given(getOveruseNoticesRepository.overuseNotices())
            .willReturn(OveruseNoticesResultDomainModel.Loaded(notices.toList()))
    }

    private fun givenQuestion() {
        given(getPendingEffectQuestionRepository.pendingEffectQuestion(any(), any()))
            .willReturn(PendingEffectQuestionResultDomainModel.Found(question))
    }
}
