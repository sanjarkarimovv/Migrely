package com.migrely.app.start

import com.migrely.app.attack.domain.usecase.PurgeExpiredDeletionsUseCase
import com.migrely.app.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import com.migrely.app.report.domain.usecase.CleanUpReportsUseCase
import com.migrely.app.settings.domain.usecase.GetAppearanceUseCase
import com.migrely.app.subscription.domain.usecase.RefreshPremiumUseCase
import com.migrely.app.subscription.domain.usecase.ShowPaymentMessagesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * What the app needs as it starts: where `MainActivity` begins (BRD D76), how it is drawn and each change to it: the
 * theme, and dark while an attack is in progress (D94), clearing what a cut-off report left (EC-REP-03), and purging
 * deleted attacks whose undo has run out (D23, D118).
 * Premium is checked with Google Play at start and on every return, and Play may show its own
 * messages about a failed payment (change add-subscription, D106, EC-BIL-03).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StartEntryPoint {
    fun shouldShowOnboardingUseCase(): ShouldShowOnboardingUseCase

    fun cleanUpReportsUseCase(): CleanUpReportsUseCase

    fun purgeExpiredDeletionsUseCase(): PurgeExpiredDeletionsUseCase

    fun getAppearanceUseCase(): GetAppearanceUseCase

    fun refreshPremiumUseCase(): RefreshPremiumUseCase

    fun showPaymentMessagesUseCase(): ShowPaymentMessagesUseCase
}
