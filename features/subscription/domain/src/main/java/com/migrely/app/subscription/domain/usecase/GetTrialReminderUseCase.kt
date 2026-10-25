package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.TrialReminderDomainModel
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetTrialBannerDismissalRepository
import java.time.Duration

/** How long before the trial ends home reminds of it (BRD §7.3, D111). */
internal val TRIAL_REMINDER_WINDOW: Duration = Duration.ofDays(1)

/**
 * Home's reminder that the free trial ends tomorrow (BRD §7.3, EC-BIL-02, D111, design D6): from 24
 * hours before the trial's end until it ends, while the subscription is on, and until dismissed.
 * The trial's end is known only when the trial started on this phone.
 */
class GetTrialReminderUseCase(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val getTrialBannerDismissalRepository: GetTrialBannerDismissalRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, TrialReminderDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): TrialReminderDomainModel {
        val entitlement = getEntitlementRepository.entitlement()
        val trialEnd = entitlement.trialEnd
            ?.takeIf { entitlement.kind == EntitlementKindDomainModel.Subscription }
        val now = getCurrentTimeRepository.now()
        val due = trialEnd != null &&
            now < trialEnd &&
            !now.isBefore(trialEnd.minus(TRIAL_REMINDER_WINDOW)) &&
            getTrialBannerDismissalRepository.dismissedTrialEnd() != trialEnd
        return if (due && trialEnd != null) {
            TrialReminderDomainModel.EndsSoon(trialEnd)
        } else {
            TrialReminderDomainModel.None
        }
    }
}
