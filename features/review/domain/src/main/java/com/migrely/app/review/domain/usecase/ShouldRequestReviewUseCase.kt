package com.migrely.app.review.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.repository.GetAttackInProgressRepository
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.GetLastPaywallShownRepository
import com.migrely.app.review.domain.repository.GetMigraineCountRepository
import com.migrely.app.review.domain.repository.GetPreviousExitRepository
import com.migrely.app.review.domain.repository.GetReviewMarksRepository
import java.time.Duration
import java.time.Instant

/** The fewest migraines logged before the app asks for a rating (BRD D130). */
internal const val MIN_MIGRAINES = 3

/** The least time between a paywall that showed itself and a request, so they never follow each other (BRD D130). */
internal val PAYWALL_GAP: Duration = Duration.ofDays(3)

/** The least time between two requests (BRD D130). */
internal val REQUEST_GAP: Duration = Duration.ofDays(120)

/** The most requests ever made on one install, restored ones included (BRD D130, EC-REV-10). */
internal const val MAX_REQUESTS = 3

/**
 * Whether the app asks for a rating now (BRD §14.4, D130, design D3). Only when all hold:
 * - at least [MIN_MIGRAINES] migraines are logged and not deleted;
 * - no paywall showed itself in the last [PAYWALL_GAP];
 * - the app's previous run did not end with a crash or an ANR;
 * - it never asked, or it asked fewer than [MAX_REQUESTS] times and last at least [REQUEST_GAP] ago;
 * - no attack is in progress. Asked last, so an attack started meanwhile is seen.
 *
 * Whatever cannot be read counts as no.
 */
@Suppress("LongParameterList")
class ShouldRequestReviewUseCase(
    private val getMigraineCountRepository: GetMigraineCountRepository,
    private val getLastPaywallShownRepository: GetLastPaywallShownRepository,
    private val getPreviousExitRepository: GetPreviousExitRepository,
    private val getReviewMarksRepository: GetReviewMarksRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Boolean {
        val now = getCurrentTimeRepository.now()
        return enoughMigraines() &&
            longSinceLastPaywall(now) &&
            !getPreviousExitRepository.previousExitCrashed() &&
            requestAllowed(now) &&
            getAttackInProgressRepository.attackInProgress() == false
    }

    private fun enoughMigraines(): Boolean =
        (getMigraineCountRepository.migraineCount() ?: 0) >= MIN_MIGRAINES

    private fun longSinceLastPaywall(now: Instant): Boolean {
        val shown = getLastPaywallShownRepository.lastPaywallShown() ?: return true
        return Duration.between(shown, now) >= PAYWALL_GAP
    }

    private fun requestAllowed(now: Instant): Boolean {
        val marks = getReviewMarksRepository.reviewMarks()
        val last = marks.lastRequested
        return marks.requests < MAX_REQUESTS &&
            (last == null || Duration.between(last, now) >= REQUEST_GAP)
    }
}
