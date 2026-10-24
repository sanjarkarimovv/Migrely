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
