package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel

/**
 * The app's own settings that are not health records (BRD D78): onboarding's answers and "Track periods". They stay on
 * this phone and in Android's backup, and are not part of a JSON backup. Reads never fail; a write that is not saved
 * throws [com.migrely.app.datasource.exception.FileDataException]. Every function blocks.
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
}
