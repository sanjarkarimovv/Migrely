package com.migrely.app.designsystem.snackbar

/** How long after a deletion the screen it returns to may still offer to undo it (BRD D23). */
const val DELETION_OFFER_MAX_AGE_MILLIS = 30_000L

/**
 * Whether a deletion made at [deletedAtMillis] may still be offered for undo at [nowMillis]: no older than
 * [DELETION_OFFER_MAX_AGE_MILLIS], and not stamped after now. After the clock was set back, a deletion's age read
 * negative, and it was offered at every start (#112).
 */
fun isFreshDeletion(deletedAtMillis: Long, nowMillis: Long): Boolean =
    nowMillis - deletedAtMillis in 0..DELETION_OFFER_MAX_AGE_MILLIS
