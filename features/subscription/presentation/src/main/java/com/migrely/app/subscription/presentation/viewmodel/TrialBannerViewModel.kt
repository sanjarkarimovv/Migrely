package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.ManageSubscriptionResultDomainModel
import com.migrely.app.subscription.domain.model.TrialReminderDomainModel
import com.migrely.app.subscription.domain.usecase.DismissTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.GetTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.presentation.model.TrialBannerPresentationNotification
import com.migrely.app.subscription.presentation.model.TrialBannerViewState
import java.time.Instant

/**
 * Home's banner a day before the free trial ends (BRD §7.3, D111), in a slot home leaves to `:app`.
 * It enters again every time home loads.
 */
class TrialBannerViewModel(
    private val getTrialReminderUseCase: GetTrialReminderUseCase,
    private val dismissTrialReminderUseCase: DismissTrialReminderUseCase,
    private val manageSubscriptionUseCase: ManageSubscriptionUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<TrialBannerViewState, TrialBannerPresentationNotification>(useCaseExecutor) {
    fun onEnter() = showReminder(offlineToManage = false)

    /** Offline, the banner says so under "Manage" until home is left or the banner loads again. */
    fun onManageAction() {
        manageSubscriptionUseCase(
            onResult = { result ->
                showReminder(
                    offlineToManage = when (result) {
                        is ManageSubscriptionResultDomainModel.Opened -> false
                        is ManageSubscriptionResultDomainModel.Offline -> true
                    }
                )
            }
        )
    }

    /** Gone at once and for good: the dismissal is kept with the trial's end (design D6). */
    fun onDismissAction(trialEnd: Instant) {
        updateViewState(TrialBannerViewState.Hidden)
        dismissTrialReminderUseCase(value = trialEnd)
    }

    private fun showReminder(offlineToManage: Boolean) {
        getTrialReminderUseCase(
            onResult = { reminder ->
                updateViewState(
                    when (reminder) {
                        TrialReminderDomainModel.None -> TrialBannerViewState.Hidden
                        is TrialReminderDomainModel.EndsSoon -> TrialBannerViewState.Shown(
                            trialEnd = reminder.trialEnd,
                            endsToday = reminder.endsToday,
                            renews = reminder.renews,
                            offlineToManage = offlineToManage
                        )
                    }
                )
            }
        )
    }
}
