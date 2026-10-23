package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.GetTodayRepository
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val NOW: Instant = Instant.parse("2026-10-23T15:00:00Z")

class GetTodayUseCaseTest {
    private lateinit var classUnderTest: GetTodayUseCase
    private val getTodayRepository: GetTodayRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = GetTodayUseCase(getTodayRepository, CoroutineContextProvider.Default)
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
        givenSnapshot(
            inProgress = null,
            ended = listOf(
                attack(id = "a", end = NOW, intensityPoints = listOf(point(3))),
                attack(id = "b", end = NOW, intensityPoints = listOf(point(9)))
            )
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 2,
                    highestRecordedIntensity = 9,
                    anyIntensityNotRecorded = false
                )
            ),
            actual
        )
    }

    @Test
    fun `Given an ended attack without intensity when executed then not recorded`() {
        givenSnapshot(inProgress = null, ended = listOf(attack(end = NOW)))

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            TodayResultDomainModel.Loaded(
                TodayDomainModel.AttacksEnded(
                    count = 1,
                    highestRecordedIntensity = null,
                    anyIntensityNotRecorded = true
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
                    anyIntensityNotRecorded = false
                )
            ),
            actual
        )
    }

    @Test
    fun `Given the repository fails when executed then Failed`() {
        given(getTodayRepository.today()).willReturn(TodaySnapshotResultDomainModel.Failed)

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(TodayResultDomainModel.Failed, actual)
    }

    private fun givenSnapshot(
        inProgress: com.migrely.app.home.domain.model.AttackDomainModel?,
        ended: List<com.migrely.app.home.domain.model.AttackDomainModel> = emptyList()
    ) {
        given(getTodayRepository.today()).willReturn(
            TodaySnapshotResultDomainModel.Loaded(
                TodaySnapshotDomainModel(
                    attackInProgress = inProgress,
                    endedAttacksTouchingToday = ended,
                    now = NOW
                )
            )
        )
    }

    private fun point(value: Int) = IntensityPointDomainModel(NOW.minus(Duration.ofHours(1)), value)
}
