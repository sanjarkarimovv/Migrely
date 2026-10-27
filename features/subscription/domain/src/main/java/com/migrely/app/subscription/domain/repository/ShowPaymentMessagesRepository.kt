package com.migrely.app.subscription.domain.repository

/**
 * Lets Google Play show its messages about failed payments; true when a subscription changed there.
 */
interface ShowPaymentMessagesRepository {
    fun showPaymentMessages(): Boolean
}
