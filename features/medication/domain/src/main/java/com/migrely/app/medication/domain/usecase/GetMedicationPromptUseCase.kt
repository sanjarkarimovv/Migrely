package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticeDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticesResultDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetOveruseNoticesRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import java.time.Duration

private const val SOFT_DAYS_BEFORE_THRESHOLD = 2
private val NOTICE_QUIET_PERIOD: Duration = Duration.ofDays(30)
private val QUESTION_FROM: Duration = Duration.ofHours(2)
private val QUESTION_UNTIL: Duration = Duration.ofHours(24)

// Ties go to the category declared first.
private val MOST_DAYS_FIRST: Comparator<GetMedicationPromptUseCase.Candidate> =
    compareBy<GetMedicationPromptUseCase.Candidate> { it.days }
        .thenByDescending { it.category.ordinal }

/**
 * The one medication card for home (BRD D46): an overuse warning, the highest level first, then "Did it help?". A read
 * that fails gives no card; home reports its own failures.
 *
 * Overuse (BRD MED-F5, D45, ICHD-3 8.2): the days of the last 30, today included, with an acute intake, each day taken
 * in the zone of its intake (EC-TIM-02), counted per category (EC-MED-02 to EC-MED-04). All acute days together count
 * when two or more categories were used (8.2.6), shown only above every single category's level. A "Got it" hides that
 * category at that level or lower for 30 days (EC-MED-06); of the warnings left, the highest level shows.
 *
 * "Did it help?" (BRD MED-F4, EC-MED-05, EC-MED-11): the oldest open acute intake taken more than 2 and at most 24
 * hours ago.
 */
class GetMedicationPromptUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAcuteIntakesRepository: GetAcuteIntakesRepository,
    private val getOveruseNoticesRepository: GetOveruseNoticesRepository,
    private val getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, MedicationPromptDomainModel>(coroutineContextProvider) {
    private val overuseDayCounter = OveruseDayCounter(getAcuteIntakesRepository)

    override fun executeInBackground(request: Unit): MedicationPromptDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        return overuseWarning(now) ?: effectQuestion(now)
    }

    private fun overuseWarning(now: CurrentTimeDomainModel): MedicationPromptDomainModel.Overuse? {
        val days = overuseDayCounter.days(now) ?: return null
        val notices = when (val result = getOveruseNoticesRepository.overuseNotices()) {
            is OveruseNoticesResultDomainModel.Loaded -> result.notices
            OveruseNoticesResultDomainModel.Failed -> return null
        }
        // Quiet categories go first, so a "Got it" on one category never hides another's warning (EC-MED-06).
        val shown = candidates(days)
            .filterNot { it.isQuiet(notices, now) }
        val topLevel = shown.maxOfOrNull { it.level } ?: return null
        return shown
            .filter { it.level == topLevel }
            .maxWithOrNull(MOST_DAYS_FIRST)
            ?.let { MedicationPromptDomainModel.Overuse(it.category, it.level, it.days) }
    }

    private fun candidates(days: Map<OveruseCategoryDomainModel, Int>): List<Candidate> {
        val singles = SINGLE_CATEGORIES.mapNotNull { category ->
            candidate(category, days[category] ?: 0)
        }
        val topSingleLevel = singles.maxOfOrNull { it.level }
        val multiple = days[OveruseCategoryDomainModel.Multiple]
            ?.let { candidate(OveruseCategoryDomainModel.Multiple, it) }
            ?.takeIf { topSingleLevel == null || it.level > topSingleLevel }
        return singles + listOfNotNull(multiple)
    }

    private fun candidate(category: OveruseCategoryDomainModel, days: Int): Candidate? {
        val softDays = category.thresholdDays - SOFT_DAYS_BEFORE_THRESHOLD
        val level = when {
            days >= category.thresholdDays -> OveruseLevelDomainModel.Doctor
            days >= softDays -> OveruseLevelDomainModel.Soft
            else -> return null
        }
        return Candidate(category, level, days)
    }

    private fun effectQuestion(now: CurrentTimeDomainModel): MedicationPromptDomainModel = when (
        val result = getPendingEffectQuestionRepository.pendingEffectQuestion(
            takenFrom = now.instant.minus(QUESTION_UNTIL),
            takenTo = now.instant.minus(QUESTION_FROM)
        )
    ) {
        is PendingEffectQuestionResultDomainModel.Found ->
            MedicationPromptDomainModel.EffectQuestion(result.question)

        PendingEffectQuestionResultDomainModel.None,
        PendingEffectQuestionResultDomainModel.Failed -> MedicationPromptDomainModel.None
    }

    internal data class Candidate(
        val category: OveruseCategoryDomainModel,
        val level: OveruseLevelDomainModel,
        val days: Int
    ) {
        fun isQuiet(notices: List<OveruseNoticeDomainModel>, now: CurrentTimeDomainModel) =
            notices.any { notice ->
                notice.category == category &&
                    notice.level >= level &&
                    Duration.between(notice.acknowledgedAt, now.instant) < NOTICE_QUIET_PERIOD
            }
    }
}
