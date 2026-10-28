package com.migrely.app.designsystem.snackbar

/** How long after a deletion the screen it returns to may still offer to undo it (BRD D23). */
const val DELETION_OFFER_MAX_AGE_MILLIS = 30_000L

/** How far ahead of the clock a deletion may read, as after an automatic correction, and still be offered. */
const val DELETION_OFFER_CLOCK_TOLERANCE_MILLIS = 2_000L

/**
 * Whether a deletion made at [deletedAtMillis] may still be offered for undo at [nowMillis]: no older than
 * [DELETION_OFFER_MAX_AGE_MILLIS], and no further ahead of the clock than [DELETION_OFFER_CLOCK_TOLERANCE_MILLIS]. After
 * the clock was set back by days, a deletion's age read negative, and it was offered at every start (#112).
 */
fun isFreshDeletion(deletedAtMillis: Long, nowMillis: Long): Boolean =
    nowMillis - deletedAtMillis in
        -DELETION_OFFER_CLOCK_TOLERANCE_MILLIS..DELETION_OFFER_MAX_AGE_MILLIS
