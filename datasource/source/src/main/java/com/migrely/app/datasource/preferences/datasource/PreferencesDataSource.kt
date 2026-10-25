package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import kotlinx.coroutines.flow.Flow

/**
 * The app's own settings that are not health records (BRD D78): onboarding's answers, "Track periods" and the theme.
 * They stay on this phone and in Android's backup, and are not part of a JSON backup. Reads never fail; a write that is
 * not saved throws [com.migrely.app.datasource.exception.FileDataException]. Every function blocks.
 */
interface PreferencesDataSource {
    fun onboardingFinished(): Boolean

    fun finishOnboarding()

    /** Null until the user answers; a value this version does not know reads as null. */
    fun goal(): GoalDataModel?

    fun setGoal(goal: GoalDataModel)

    fun attackFrequency(): AttackFrequencyDataModel?

    fun setAttackFrequency(frequency: AttackFrequencyDataModel)

    /** On until the user turns it off (BRD D79). */
    fun periodTracking(): Boolean

    fun setPeriodTracking(on: Boolean)

    /** Dark until the user chooses (BRD D94); a value this version does not know reads as dark. */
    fun theme(): ThemeDataModel

    fun setTheme(theme: ThemeDataModel)

    /** The theme now, then again whenever it changes, until the collector stops. */
    fun themeChanges(): Flow<ThemeDataModel>

    /**
     * The premium entitlement as last decided (BRD D106), or null before the first check or when
     * what is stored cannot be read. The purchase itself stays in Google Play. Unlike the settings,
     * it is not in Android's backup: a new install asks Google Play again (D115).
     */
    fun entitlement(): EntitlementDataModel?

    fun setEntitlement(entitlement: EntitlementDataModel)

    /** Whether a paywall has shown itself for [trigger] (BRD D110). */
    fun paywallShown(trigger: PaywallTriggerDataModel): Boolean

    /** When a paywall last showed itself, in epoch milliseconds, or null. */
    fun lastPaywallShownMillis(): Long?

    fun markPaywallShown(trigger: PaywallTriggerDataModel, atMillis: Long)

    /** The trial end, in epoch milliseconds, whose home banner was dismissed, or null (D111). */
    fun trialBannerDismissedFor(): Long?

    fun dismissTrialBanner(trialEndMillis: Long)

    /**
     * Forgets every setting, so the app starts as on a new phone (BRD D96). The entitlement goes
     * too and is read from Google Play again at the next check (D112).
     */
    fun clear()
}
