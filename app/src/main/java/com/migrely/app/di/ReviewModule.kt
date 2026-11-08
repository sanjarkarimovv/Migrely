package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.review.datasource.ReviewDataSource
import com.migrely.app.review.data.mapper.ReviewFlowDomainMapper
import com.migrely.app.review.data.repository.ReviewRepository
import com.migrely.app.review.domain.usecase.PrepareReviewUseCase
import com.migrely.app.review.domain.usecase.RequestReviewUseCase
import com.migrely.app.review.domain.usecase.ShouldRequestReviewUseCase
import com.migrely.app.review.presentation.viewmodel.ReviewPromptViewModel
import com.migrely.app.review.ui.di.ReviewPromptDependencies
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The request for a rating on home (change add-in-app-review, BRD D129–D132). Its datasource is
 * bound per build type, in `ReviewDataSourceModule`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object ReviewModule {
    @Provides
    fun providesReviewRepository(
        attackDataSource: AttackDataSource,
        preferencesDataSource: PreferencesDataSource,
        appInfoDataSource: AppInfoDataSource,
        clockDataSource: ClockDataSource,
        reviewDataSource: ReviewDataSource
    ) = ReviewRepository(
        attackDataSource,
        preferencesDataSource,
        appInfoDataSource,
        clockDataSource,
        reviewDataSource,
        ReviewFlowDomainMapper()
    )

    /**
     * One for the whole process, so a rotation or a new activity never launches the flow twice
     * (BRD D130, EC-REV-08, design D4). It holds no activity or context.
     */
    @Provides
    @Singleton
    fun providesReviewPromptViewModel(
        reviewRepository: ReviewRepository,
        coroutineContextProvider: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = ReviewPromptViewModel(
        shouldRequestReviewUseCase = ShouldRequestReviewUseCase(
            getMigraineCountRepository = reviewRepository,
            getLastPaywallShownRepository = reviewRepository,
            getPreviousExitRepository = reviewRepository,
            getReviewMarksRepository = reviewRepository,
            getAttackInProgressRepository = reviewRepository,
            getCurrentTimeRepository = reviewRepository,
            coroutineContextProvider = coroutineContextProvider
        ),
        prepareReviewUseCase = PrepareReviewUseCase(
            prepareReviewFlowRepository = reviewRepository,
            coroutineContextProvider = coroutineContextProvider
        ),
        requestReviewUseCase = RequestReviewUseCase(
            getAttackInProgressRepository = reviewRepository,
            getLastPaywallShownRepository = reviewRepository,
            launchReviewFlowRepository = reviewRepository,
            markReviewRequestedRepository = reviewRepository,
            getCurrentTimeRepository = reviewRepository,
            coroutineContextProvider = coroutineContextProvider
        ),
        useCaseExecutor = useCaseExecutor
    )

    @Provides
    fun providesReviewPromptDependencies(reviewPromptViewModel: ReviewPromptViewModel) =
        ReviewPromptDependencies(reviewPromptViewModel)
}
