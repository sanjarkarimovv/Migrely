package com.migrely.app.datasource.review.model

/** How Google Play's review flow went (BRD D131). */
enum class ReviewFlowDataModel {
    /** The flow ran to its end. Google Play says neither whether a card showed nor whether a rating was given. */
    Completed,

    /** Google Play could not run it: no Play Store, no screen, an error or no answer in time. */
    Unavailable
}
