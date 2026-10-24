package com.migrely.app.ui.main.route

import kotlinx.serialization.Serializable

/** [startAttack] is true when the app was opened from the "Migraine started" launcher shortcut. */
@Serializable
data class Home(val startAttack: Boolean = false)

/**
 * The details of one attack (change add-attack-details). [announceMerge] is true when a merge from the add screen opened
 * it, so it confirms the merge (change add-past-attacks).
 */
@Serializable
data class AttackDetails(val attackId: String, val announceMerge: Boolean = false)

/**
 * Adding an attack that has already ended (change add-past-attacks). [epochDay] is the day chosen in History, or null
 * for the usual defaults (change add-attack-history, BRD D30).
 */
@Serializable
data class AddPastAttack(val epochDay: Long? = null)

/** History (change add-attack-history). [epochDay] is the day chosen on home's calendar, or null for this month. */
@Serializable
data class History(val epochDay: Long? = null)

/**
 * "Medication taken" (change add-medication-tracking, BRD D34): for [attackId] or on its own when null; [darkRoom] when
 * opened from attack mode.
 */
@Serializable
data class LogMedication(val attackId: String? = null, val darkRoom: Boolean = false)

/** My medications (change add-medication-tracking). */
@Serializable
data object Medications

/** The medication editor; [medicationId] is null when adding one. */
@Serializable
data class EditMedication(val medicationId: String? = null)

/** Reminders (change add-reminders). */
@Serializable
data object Reminders

/** Settings (change add-data-export-import, BRD D70). */
@Serializable
data object Settings

/** Your data: backups, exports and imports (change add-data-export-import). */
@Serializable
data object YourData

/** Onboarding, on a new user's first start (change add-onboarding, BRD D76). */
@Serializable
data object Onboarding

/** The doctor report (change add-doctor-report, BRD D81). */
@Serializable
data object Report
