package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.navigation.mapper.OnboardingNavigationEventDestinationMapper
import com.migrely.app.onboarding.data.mapper.AttackFrequencyDataMapper
import com.migrely.app.onboarding.data.mapper.OnboardingGoalDataMapper
import com.migrely.app.onboarding.data.repository.OnboardingRepository
import com.migrely.app.onboarding.domain.usecase.FinishOnboardingUseCase
import com.migrely.app.onboarding.domain.usecase.SaveAttackFrequencyUseCase
import com.migrely.app.onboarding.domain.usecase.SaveGoalUseCase
import com.migrely.app.onboarding.domain.usecase.SavePeriodTrackingUseCase
import com.migrely.app.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import com.migrely.app.onboarding.presentation.mapper.AttackFrequencyDomainMapper
import com.migrely.app.onboarding.presentation.mapper.OnboardingGoalDomainMapper
import com.migrely.app.onboarding.presentation.viewmodel.OnboardingViewModel
import com.migrely.app.onboarding.ui.di.OnboardingDependencies
import com.migrely.app.onboarding.ui.mapper.OnboardingEventPresenter
import com.migrely.app.onboarding.ui.mapper.OnboardingNotificationUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Onboarding (change add-onboarding, BRD D76–D80). One repository serves every repository interface. */
@Module
@InstallIn(SingletonComponent::class)
internal object OnboardingModule {
    @Provides
    fun providesOnboardingRepository(
        preferencesDataSource: PreferencesDataSource,
        backupDataSource: BackupDataSource
    ) = OnboardingRepository(
        preferencesDataSource,
        backupDataSource,
        OnboardingGoalDataMapper(),
        AttackFrequencyDataMapper()
    )

    @Provides
    fun providesShouldShowOnboardingUseCase(
        repository: OnboardingRepository,
        coroutines: CoroutineContextProvider
    ) = ShouldShowOnboardingUseCase(repository, repository, repository, coroutines)

    @Provides
    fun providesOnboardingDependencies(
        repository: OnboardingRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ): OnboardingDependencies {
        val presenter = OnboardingEventPresenter()
        return OnboardingDependencies(
            onboardingViewModel = OnboardingViewModel(
                SaveGoalUseCase(repository, coroutines),
                SaveAttackFrequencyUseCase(repository, coroutines),
                SavePeriodTrackingUseCase(repository, coroutines),
                FinishOnboardingUseCase(repository, coroutines),
                OnboardingGoalDomainMapper(),
                AttackFrequencyDomainMapper(),
                useCaseExecutor
            ),
            onboardingEventPresenter = presenter,
            navigationMapper = OnboardingNavigationEventDestinationMapper(),
            notificationMapper = OnboardingNotificationUiMapper(presenter)
        )
    }
}
