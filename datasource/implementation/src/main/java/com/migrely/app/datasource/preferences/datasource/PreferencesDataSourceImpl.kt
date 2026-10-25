package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val ONBOARDING_FINISHED = "onboarding_finished"
private const val GOAL = "goal"
private const val ATTACK_FREQUENCY = "attack_frequency"
private const val PERIOD_TRACKING = "period_tracking"
private const val THEME = "theme"
private const val ENTITLEMENT_KIND = "entitlement_kind"
private const val ENTITLEMENT_PRODUCT = "entitlement_product"
private const val ENTITLEMENT_BASE_PLAN = "entitlement_base_plan"
private const val ENTITLEMENT_TOKEN = "entitlement_token"
private const val ENTITLEMENT_PURCHASE_TIME = "entitlement_purchase_time"
private const val ENTITLEMENT_AUTO_RENEWING = "entitlement_auto_renewing"
private const val ENTITLEMENT_TRIAL_END = "entitlement_trial_end"
private const val ENTITLEMENT_LAST_CHECK = "entitlement_last_check"
private const val ENTITLEMENT_SUBSCRIPTION_TOO = "entitlement_subscription_too"
private const val PAYWALL_SHOWN_PREFIX = "paywall_shown_"
private const val PAYWALL_LAST_SHOWN = "paywall_last_shown"
private const val TRIAL_BANNER_DISMISSED = "trial_banner_dismissed"

/**
 * Over the app's `migrely_preferences` file ([FILE]). Writes use `commit()`, which is safe here
 * because every call already runs off the main thread, so a write that did not reach the disk is
 * reported instead of lost.
 *
 * The premium entitlement is kept apart, in [entitlementPreferences] over [ENTITLEMENT_FILE],
 * which Android's backup and device transfer leave out (BRD D115): a purchase belongs to a Google
 * account, so on a new install or phone Premium comes only from what Google Play reports for the
 * account signed in there.
 */
class PreferencesDataSourceImpl(
    private val preferences: SharedPreferences,
    private val entitlementPreferences: SharedPreferences
) : PreferencesDataSource {
    override fun onboardingFinished(): Boolean = preferences.getBoolean(ONBOARDING_FINISHED, false)

    override fun finishOnboarding() = write { putBoolean(ONBOARDING_FINISHED, true) }

    override fun goal(): GoalDataModel? = preferences.getString(GOAL, null)
        ?.let { stored -> GoalDataModel.entries.firstOrNull { it.key == stored } }

    override fun setGoal(goal: GoalDataModel) = write { putString(GOAL, goal.key) }

    override fun attackFrequency(): AttackFrequencyDataModel? =
        preferences.getString(ATTACK_FREQUENCY, null)
            ?.let { stored -> AttackFrequencyDataModel.entries.firstOrNull { it.key == stored } }

    override fun setAttackFrequency(frequency: AttackFrequencyDataModel) =
        write { putString(ATTACK_FREQUENCY, frequency.key) }

    override fun periodTracking(): Boolean = preferences.getBoolean(PERIOD_TRACKING, true)

    override fun setPeriodTracking(on: Boolean) = write { putBoolean(PERIOD_TRACKING, on) }

    override fun theme(): ThemeDataModel = preferences.getString(THEME, null)
        ?.let { stored -> ThemeDataModel.entries.firstOrNull { it.key == stored } }
        ?: ThemeDataModel.Dark

    override fun setTheme(theme: ThemeDataModel) = write { putString(THEME, theme.key) }

    override fun themeChanges(): Flow<ThemeDataModel> = callbackFlow {
        // Android holds listeners weakly; this one is held by the flow until its collector stops.
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            // A null key means the file was cleared (Android 11 and later).
            if (key == THEME || key == null) trySend(theme())
        }
        // Listening before reading, so a change between the two is never missed.
        preferences.registerOnSharedPreferenceChangeListener(listener)
        send(theme())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun entitlement(): EntitlementDataModel? {
        val stored = entitlementPreferences
        val kind = stored.getString(ENTITLEMENT_KIND, null)
            ?.let { key -> EntitlementKindDataModel.entries.firstOrNull { it.key == key } }
            ?: return null
        return EntitlementDataModel(
            kind = kind,
            productId = stored.getString(ENTITLEMENT_PRODUCT, null),
            basePlanId = stored.getString(ENTITLEMENT_BASE_PLAN, null),
            purchaseToken = stored.getString(ENTITLEMENT_TOKEN, null),
            purchaseTimeMillis = stored.longOrNull(ENTITLEMENT_PURCHASE_TIME),
            autoRenewing = stored.getBoolean(ENTITLEMENT_AUTO_RENEWING, false),
            trialEndMillis = stored.longOrNull(ENTITLEMENT_TRIAL_END),
            lastCheckMillis = stored.longOrNull(ENTITLEMENT_LAST_CHECK),
            subscriptionRenewsToo = stored.getBoolean(ENTITLEMENT_SUBSCRIPTION_TOO, false)
        )
    }

    // One commit, so a check is never stored half written.
    override fun setEntitlement(entitlement: EntitlementDataModel) = entitlementPreferences.write {
        putString(ENTITLEMENT_KIND, entitlement.kind.key)
        putString(ENTITLEMENT_PRODUCT, entitlement.productId)
        putString(ENTITLEMENT_BASE_PLAN, entitlement.basePlanId)
        putString(ENTITLEMENT_TOKEN, entitlement.purchaseToken)
        putLongOrRemove(ENTITLEMENT_PURCHASE_TIME, entitlement.purchaseTimeMillis)
        putBoolean(ENTITLEMENT_AUTO_RENEWING, entitlement.autoRenewing)
        putLongOrRemove(ENTITLEMENT_TRIAL_END, entitlement.trialEndMillis)
        putLongOrRemove(ENTITLEMENT_LAST_CHECK, entitlement.lastCheckMillis)
        putBoolean(ENTITLEMENT_SUBSCRIPTION_TOO, entitlement.subscriptionRenewsToo)
    }

    override fun paywallShown(trigger: PaywallTriggerDataModel): Boolean =
        preferences.getBoolean(PAYWALL_SHOWN_PREFIX + trigger.key, false)

    override fun lastPaywallShownMillis(): Long? = preferences.longOrNull(PAYWALL_LAST_SHOWN)

    override fun markPaywallShown(trigger: PaywallTriggerDataModel, atMillis: Long) = write {
        putBoolean(PAYWALL_SHOWN_PREFIX + trigger.key, true)
        putLong(PAYWALL_LAST_SHOWN, atMillis)
    }

    override fun trialBannerDismissedFor(): Long? = preferences.longOrNull(TRIAL_BANNER_DISMISSED)

    override fun dismissTrialBanner(trialEndMillis: Long) =
        write { putLong(TRIAL_BANNER_DISMISSED, trialEndMillis) }

    /** The entitlement first, so whatever stops this leaves no Premium behind (BRD D112). */
    override fun clear() {
        entitlementPreferences.write { clear() }
        write { this.clear() }
    }

    private fun SharedPreferences.longOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private fun SharedPreferences.Editor.putLongOrRemove(key: String, value: Long?) {
        if (value == null) remove(key) else putLong(key, value)
    }

    private inline fun write(change: SharedPreferences.Editor.() -> Unit) =
        preferences.write(change)

    private inline fun SharedPreferences.write(change: SharedPreferences.Editor.() -> Unit) {
        if (!edit().apply(change).commit()) throw FileDataException("Preferences not saved")
    }

    companion object {
        /** The settings, paywall marks and trial banner: backed up with the diary (BRD D74). */
        const val FILE = "migrely_preferences"

        /** The premium entitlement, never backed up (BRD D115; res/xml/backup_rules.xml). */
        const val ENTITLEMENT_FILE = "migrely_entitlement"
    }
}
