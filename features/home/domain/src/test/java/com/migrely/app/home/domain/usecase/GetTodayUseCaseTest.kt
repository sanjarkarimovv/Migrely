package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.AttackSummaryDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.DeletedAttackDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.TASHKENT
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.GetTodayRepository
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-23T15:00:00Z")
private val BERLIN: ZoneId = ZoneId.of("Europe/Berlin")
private val DUBAI: ZoneId = ZoneId.of("Asia/Dubai")

class GetTodayUseCaseTest {
    private lateinit var classUnderTest: GetTodayUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getTodayRepository: GetTodayRepository = mock()

    @Before
    fun setUp() {
        givenClock(NOW, TASHKENT)
        classUnderTest = GetTodayUseCase(
            getCurrentTimeRepository,
            getTodayRepository,
            CoroutineContextProvider.Default
        )
    }

    @Test
    fun `When executed then reads today with two days on each side in the device zone`() {
        givenSnapshot(inProgress = null)

        classUnderTest.executeInBackground(Unit)

        // 20:00 on 23 October in Tashkent (UTC+5): from 21 October 00:00 to 26 October 00:00 there.
        verify(getTodayRepository).today(
            Instant.parse("2026-10-20T19:00:00Z"),
            Instant.parse("2026-10-25T19:00:00Z")
        )
    }

    @Test
    fun `Given an attack logged late in Berlin when opened after flying east then Calm`() {
        // 23:15 to 23:45 on 24 October in Berlin (UTC+2); opened at 02:50 on 25 October in Tashkent.
        givenClock(Instant.parse("2026-10-24T21:50:00Z"), TASHKENT)
        givenSnapshot(
            inProgress = null,
            ended = listOf(
                attack(
                    start = Instant.parse("2026-10-24T21:15:00Z"),
                    end = Instant.parse("2026-10-24T21:45:00Z"),
                    startZone = BERLIN
                )
            )
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Loaded(TodayDomainModel.Calm), actual)
    }

    @Test
    fun `Given an attack logged early in Tashkent when opened after flying west then Calm`() {
        // 01:00 to 02:00 on 25 October in Tashkent; opened at 23:30 on 24 October in Berlin.
        givenClock(Instant.parse("2026-10-24T21:30:00Z"), BERLIN)
        givenSnapshot(
            inProgress = null,
            ended = listOf(
                attack(
                    start = Instant.parse("2026-10-24T20:00:00Z"),
                    end = Instant.parse("2026-10-24T21:00:00Z")
                )
            )
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Loaded(TodayDomainModel.Calm), actual)
    }

    @Test
    fun `Given an attack logged in another zone on the same date then it counts for today`() {
        // 08:00 to 09:00 on 24 October in Tashkent; opened at 14:00 the same day in Dubai.
        givenClock(Instant.parse("2026-10-24T10:00:00Z"), DUBAI)
        val elsewhere = attack(
            start = Instant.parse("2026-10-24T03:00:00Z"),
            end = Instant.parse("2026-10-24T04:00:00Z")
        )
        givenSnapshot(inProgress = null, ended = listOf(elsewhere))

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 1,
                    highestRecordedIntensity = null,
                    anyIntensityNotRecorded = true,
                    attacks = listOf(summary(elsewhere))
                )
            ),
            actual
        )
    }

    @Test
    fun `Given an attack that ended exactly at midnight then it does not count for today`() {
        // 22:00 on 22 October to 00:00 on 23 October in Tashkent.
        givenSnapshot(
            inProgress = null,
            ended = listOf(
                attack(
                    start = Instant.parse("2026-10-22T17:00:00Z"),
                    end = Instant.parse("2026-10-22T19:00:00Z")
                )
            )
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Loaded(TodayDomainModel.Calm), actual)
    }

    @Test
    fun `Given nothing touches today when executed then Calm`() {
        givenSnapshot(inProgress = null)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Loaded(TodayDomainModel.Calm), actual)
    }

    @Test
    fun `Given an attack in progress for one hour when executed then in progress without check`() {
        val inProgress = attack(start = NOW.minus(Duration.ofHours(1)))
        givenSnapshot(inProgress = inProgress)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttackInProgress(inProgress, longAttackCheckDue = false)
            ),
            actual
        )
    }

    @Test
    fun `Given an attack in progress for exactly 72 hours when executed then the check is due`() {
        val inProgress = attack(start = NOW.minus(Duration.ofHours(72)))
        givenSnapshot(inProgress = inProgress)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttackInProgress(inProgress, longAttackCheckDue = true)
            ),
            actual
        )
    }

    @Test
    fun `Given still going 10 hours ago on a 100-hour attack then not due`() {
        val inProgress = attack(
            start = NOW.minus(Duration.ofHours(100)),
            longAttackCheck = LongAttackCheckDomainModel(
                confirmedAt = NOW.minus(Duration.ofHours(10)),
                disabled = false
            )
        )
        givenSnapshot(inProgress = inProgress)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttackInProgress(inProgress, longAttackCheckDue = false)
            ),
            actual
        )
    }

    @Test
    fun `Given still going confirmed 72 hours ago when executed then due again`() {
        val inProgress = attack(
            start = NOW.minus(Duration.ofHours(150)),
            longAttackCheck = LongAttackCheckDomainModel(
                confirmedAt = NOW.minus(Duration.ofHours(72)),
                disabled = false
            )
        )
        givenSnapshot(inProgress = inProgress)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttackInProgress(inProgress, longAttackCheckDue = true)
            ),
            actual
        )
    }

    @Test
    fun `Given the check is disabled on a 200-hour attack when executed then not due`() {
        val inProgress = attack(
            start = NOW.minus(Duration.ofHours(200)),
            longAttackCheck = LongAttackCheckDomainModel(confirmedAt = null, disabled = true)
        )
        givenSnapshot(inProgress = inProgress)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttackInProgress(inProgress, longAttackCheckDue = false)
            ),
            actual
        )
    }

    @Test
    fun `Given ended attacks with peaks 3 and 9 when executed then two attacks with highest 9`() {
        val mild = attack(id = "a", end = NOW, intensityPoints = listOf(point(3)))
        val severe = attack(id = "b", end = NOW, intensityPoints = listOf(point(9)))
        givenSnapshot(inProgress = null, ended = listOf(mild, severe))

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 2,
                    highestRecordedIntensity = 9,
                    anyIntensityNotRecorded = false,
                    attacks = listOf(summary(mild), summary(severe))
                )
            ),
            actual
        )
    }

    @Test
    fun `Given an ended attack without intensity when executed then not recorded`() {
        val unrecorded = attack(end = NOW)
        givenSnapshot(inProgress = null, ended = listOf(unrecorded))

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 1,
                    highestRecordedIntensity = null,
                    anyIntensityNotRecorded = true,
                    attacks = listOf(summary(unrecorded))
                )
            ),
            actual
        )
    }

    @Test
    fun `Given an overnight attack ended today then it counts for today`() {
        val overnight = attack(
            start = Instant.parse("2026-10-22T18:30:00Z"),
            end = Instant.parse("2026-10-22T21:00:00Z"),
            intensityPoints = listOf(point(6))
        )
        givenSnapshot(inProgress = null, ended = listOf(overnight))

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 1,
                    highestRecordedIntensity = 6,
                    anyIntensityNotRecorded = false,
                    attacks = listOf(summary(overnight))
                )
            ),
            actual
        )
    }

    @Test
    fun `Given the repository fails when executed then Failed`() {
        given(getTodayRepository.today(any(), any()))
            .willReturn(TodaySnapshotResultDomainModel.Failed)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Failed, actual)
    }

    private fun givenSnapshot(
        inProgress: AttackDomainModel?,
        ended: List<AttackDomainModel> = emptyList()
    ) {
        given(getTodayRepository.today(any(), any())).willReturn(
            TodaySnapshotResultDomainModel.Loaded(
                TodaySnapshotDomainModel(attackInProgress = inProgress, endedAttacks = ended)
            )
        )
    }

    private fun givenClock(instant: Instant, zone: ZoneId) {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(instant, zone))
    }

    private fun point(value: Int) = IntensityPointDomainModel(NOW.minus(Duration.ofHours(1)), value)

    @Test
    fun `Given a recent deletion when executed then passes it on`() {
        val deleted = DeletedAttackDomainModel("gone", NOW.minusSeconds(2))
        given(getTodayRepository.today(any(), any())).willReturn(
            TodaySnapshotResultDomainModel.Loaded(
                TodaySnapshotDomainModel(null, emptyList(), deleted)
            )
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Loaded(TodayDomainModel.Calm, deleted), actual)
    }

    private fun summary(attack: AttackDomainModel) = AttackSummaryDomainModel(
        id = attack.id,
        start = attack.start,
        startZone = attack.startZone,
        end = requireNotNull(attack.end),
        peakIntensity = attack.peakIntensity
    )
}
