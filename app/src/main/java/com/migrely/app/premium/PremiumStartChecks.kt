package com.migrely.app.premium

/**
 * What `MainActivity` asks of Google Play as it starts and comes back (change add-subscription):
 * a check of Premium on every resume, throttled by the check itself (BRD D106), and Play's own
 * messages about a failed payment once per start (EC-BIL-03). Those never come when the app opened
 * to start an attack, by the shortcut or the "Migraine started" intent; while an attack is in
 * progress the use case keeps them back (review M7).
 */
class PremiumStartChecks(
    private val refresh: () -> Unit,
    private val showPaymentMessages: () -> Unit
) {
    /** Kept across a recreation, so a rotation does not ask again. */
    var paymentMessagesAsked = false

    fun onResume() = refresh()

    /** The first screen is up, never the splash: the messages, once, unless an attack starts. */
    fun onFirstScreen(startAttack: Boolean) {
        if (paymentMessagesAsked) return
        paymentMessagesAsked = true
        if (!startAttack) showPaymentMessages()
    }
}
