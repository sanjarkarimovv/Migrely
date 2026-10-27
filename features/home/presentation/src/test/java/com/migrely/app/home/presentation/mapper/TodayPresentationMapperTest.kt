package com.migrely.app.home.presentation.mapper

import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.presentation.model.AttackInProgressPresentationModel
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.model.PeakPresentationModel
import com.migrely.app.home.presentation.model.SkyPresentationModel
import com.migrely.app.home.presentation.model.TodayPresentationModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

@RunWith(Parameterized::class)
class TodayPresentationMapperTest(
    private val given: TodayDomainModel,
    private val expected: HomeViewState
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            case(TodayDomainModel.Calm, today(SkyPresentationModel.Calm, 0, null)),
            case(TodayDomainModel.PainFree, today(SkyPresentationModel.Clear, 0, null)),
            case(
                ended(highest = 3),
                today(SkyPresentationModel.Hazy, 1, PeakPresentationModel.Mild)
            ),
            case(
                ended(highest = 6),
                today(SkyPresentationModel.Cloudy, 1, PeakPresentationModel.Moderate)
            ),
            case(
                ended(highest = 7),
                today(SkyPresentationModel.Storm, 1, PeakPresentationModel.Severe)
            ),
            case(
                ended(highest = null, unknown = true),
                today(SkyPresentationModel.Cloudy, 1, PeakPresentationModel.NotRecorded)
            ),
            case(
                ended(highest = 3, unknown = true, count = 2),
                today(SkyPresentationModel.Cloudy, 2, PeakPresentationModel.Mild)
            ),
            case(
                ended(highest = 9, unknown = true, count = 2),
                today(SkyPresentationModel.Storm, 2, PeakPresentationModel.Severe)
            ),
            case(
                TodayDomainModel.AttackInProgress(
                    attack(points = listOf(point(9, 0), point(4, 60))),
                    longAttackCheckDue = true
                ),
                HomeViewState.AttackInProgress(
                    AttackInProgressPresentationModel(
                        "a",
                        START,
                        ZONE,
                        IntensityLevelPresentationModel.Moderate,
                        true
                    )
                )
            ),
            case(
                TodayDomainModel.AttackInProgress(
                    attack(points = emptyList()),
                    longAttackCheckDue = false
                ),
                HomeViewState.AttackInProgress(
                    AttackInProgressPresentationModel("a", START, ZONE, null, false)
                )
            )
        )

        private fun case(given: TodayDomainModel, expected: HomeViewState) =
            arrayOf<Any>(given, expected)

        private fun today(sky: SkyPresentationModel, count: Int, peak: PeakPresentationModel?) =
            HomeViewState.Today(TodayPresentationModel(sky, count, peak))

        private fun ended(highest: Int?, unknown: Boolean = false, count: Int = 1) =
            TodayDomainModel.AttacksEnded(count, highest, unknown)

        private fun point(value: Int, minutesAfterStart: Long) =
            IntensityPointDomainModel(START.plusSeconds(minutesAfterStart * 60), value)

        private fun attack(points: List<IntensityPointDomainModel>) = AttackDomainModel(
            "a",
            START,
            ZONE,
            null,
            points,
            LongAttackCheckDomainModel(null, false)
        )
    }

    private val classUnderTest = TodayPresentationMapper(IntensityLevelPresentationMapper())

    @Test
    fun `When toPresentation then returns the expected view state`() {
        assertEquals(expected, classUnderTest.toPresentation(given))
    }
}

class TodayPresentationMapperPeriodTest {
    private val classUnderTest = TodayPresentationMapper(IntensityLevelPresentationMapper())

    @Test
    fun `Given a period mark when mapping today then it is on today's model`() {
        val actual = classUnderTest.toPresentation(TodayDomainModel.PainFree, periodToday = true)

        assertEquals(
            HomeViewState.Today(
                TodayPresentationModel(
                    sky = SkyPresentationModel.Clear,
                    attackCount = 0,
                    peak = null,
                    periodToday = true
                )
            ),
            actual
        )
    }

    @Test
    fun `Given Period is not offered when mapping today then the model says so`() {
        val actual = classUnderTest.toPresentation(TodayDomainModel.Calm, periodOffered = false)

        assertEquals(false, (actual as HomeViewState.Today).today.periodOffered)
    }
}
