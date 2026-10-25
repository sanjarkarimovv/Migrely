package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
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

/**
 * Over the app's `migrely_preferences` file. Writes use `commit()`, which is safe here because every call already runs
 * off the main thread, so a write that did not reach the disk is reported instead of lost.
 */
class PreferencesDataSourceImpl(private val preferences: SharedPreferences) :
    PreferencesDataSource {
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

    override fun clear() = write { this.clear() }

    private inline fun write(change: SharedPreferences.Editor.() -> Unit) {
        if (!preferences.edit().apply(
                change
            ).commit()
        ) {
            throw FileDataException("Preferences not saved")
        }
    }
}
