package com.migrely.app.ui.main.route

import kotlinx.serialization.Serializable

/** [startAttack] is true when the app was opened from the "Migraine started" launcher shortcut. */
@Serializable
data class Home(val startAttack: Boolean = false)

/** The details of one attack (change add-attack-details). */
@Serializable
data class AttackDetails(val attackId: String)
