package com.migrely.app.review.domain.model

import java.time.Instant

/** How many times the app has asked for a rating, and when it last did (BRD D131). */
data class ReviewMarksDomainModel(val requests: Int, val lastRequested: Instant?)

/** How Google Play's review flow went (BRD D131). */
enum class ReviewFlowResultDomainModel {
    /** The flow ran to its end. Google Play says neither whether a card showed nor whether a rating was given. */
    Completed,

    /** Google Play could not run it: no Play Store, no screen, an error or no answer in time. */
    Unavailable
}

/** When the first-10 card, "Still going?" or the medication overuse card last appeared (BRD D188, EC-REV-14). */
sealed interface LastSafetyCardDomainModel {
    data object Never : LastSafetyCardDomainModel

    data class ShownAt(val at: Instant) : LastSafetyCardDomainModel

    /** The mark could not be read; the rules take it as recent. */
    data object Unknown : LastSafetyCardDomainModel
}
