package com.migrely.app.review.data.repository

import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.review.datasource.ReviewDataSource
import com.migrely.app.review.data.mapper.ReviewFlowDomainMapper
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel
import com.migrely.app.review.domain.model.ReviewMarksDomainModel
import com.migrely.app.review.domain.repository.GetAttackInProgressRepository
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.GetLastPaywallShownRepository
import com.migrely.app.review.domain.repository.GetMigraineCountRepository
import com.migrely.app.review.domain.repository.GetPreviousExitRepository
import com.migrely.app.review.domain.repository.GetReviewMarksRepository
import com.migrely.app.review.domain.repository.LaunchReviewFlowRepository
import com.migrely.app.review.domain.repository.MarkReviewRequestedRepository
import java.time.Instant

/**
 * The review feature's repositories (change add-in-app-review design D3). A read that fails is null,
 * which the rules take as no; a mark that cannot be written is dropped, so at worst the app asks once
 * more.
 */
@Suppress("LongParameterList")
class ReviewRepository(
    private val attackDataSource: AttackDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val appInfoDataSource: AppInfoDataSource,
    private val clockDataSource: ClockDataSource,
    private val reviewDataSource: ReviewDataSource,
    private val reviewFlowDomainMapper: ReviewFlowDomainMapper
) : GetMigraineCountRepository,
    GetAttackInProgressRepository,
    GetLastPaywallShownRepository,
    GetPreviousExitRepository,
    GetReviewMarksRepository,
    GetCurrentTimeRepository,
    LaunchReviewFlowRepository,
    MarkReviewRequestedRepository {
    /**
     * Every attack not deleted that touches any time up to now, the one going on too, as the
     * subscription counts them. One in its Undo window is not read (BRD D23, EC-REV-11).
     */
    override fun migraineCount(): Int? = try {
        attackDataSource.overlapping(null, Instant.EPOCH, clockDataSource.now()).size
    } catch (_: DataException) {
        null
    }

    override fun attackInProgress(): Boolean? = try {
        attackDataSource.inProgress() != null
    } catch (_: DataException) {
        null
    }

    override fun lastPaywallShown(): Instant? =
        preferencesDataSource.lastPaywallShownMillis()?.let(Instant::ofEpochMilli)

    override fun previousExitCrashed(): Boolean = appInfoDataSource.previousExitCrashed()

    override fun reviewMarks(): ReviewMarksDomainModel {
        val requests = preferencesDataSource.reviewRequests()
        return ReviewMarksDomainModel(
            requests = requests.count,
            lastRequested = requests.lastMillis?.let(Instant::ofEpochMilli)
        )
    }

    override fun now(): Instant = clockDataSource.now()

    override fun launchReviewFlow(): ReviewFlowResultDomainModel =
        reviewFlowDomainMapper.toDomain(reviewDataSource.launchReviewFlow())

    override fun markReviewRequested(at: Instant) {
        try {
            preferencesDataSource.markReviewRequested(at.toEpochMilli())
        } catch (_: DataException) {
            // Dropped: at worst the app asks once more (BRD D131).
        }
    }
}
