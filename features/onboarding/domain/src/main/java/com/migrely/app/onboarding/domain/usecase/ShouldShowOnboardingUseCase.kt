package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.PhoneRecordsDomainModel
import com.migrely.app.onboarding.domain.repository.FinishOnboardingRepository
import com.migrely.app.onboarding.domain.repository.GetOnboardingFinishedRepository
import com.migrely.app.onboarding.domain.repository.GetPhoneRecordsRepository

/**
 * Whether the app starts with onboarding (BRD ONB-F2, D76). The request is whether the app was opened to log a
 * migraine, by the "start attack" shortcut or the daily reminder's "Log a migraine"; that is never held up. A phone that already holds records finishes onboarding without showing it, and a
 * read that fails shows home, so a broken store never keeps the user out.
 */
class ShouldShowOnboardingUseCase(
    private val getOnboardingFinishedRepository: GetOnboardingFinishedRepository,
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    private val finishOnboardingRepository: FinishOnboardingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Boolean): Boolean = when {
        request -> false
        getOnboardingFinishedRepository.onboardingFinished() -> false
        else -> when (getPhoneRecordsRepository.phoneRecords()) {
            PhoneRecordsDomainModel.None -> true
            PhoneRecordsDomainModel.Some -> {
                finishOnboardingRepository.finishOnboarding()
                false
            }
            PhoneRecordsDomainModel.Failed -> false
        }
    }
}
