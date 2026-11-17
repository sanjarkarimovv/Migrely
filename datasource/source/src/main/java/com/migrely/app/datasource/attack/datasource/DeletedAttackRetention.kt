package com.migrely.app.datasource.attack.datasource

/**
 * How long a deleted attack stays restorable: the longest Android accessibility timeout (BRD D23). Public so that the
 * Undo ceiling can be tied to it (BRD D182).
 */
const val DELETED_ATTACK_RETENTION_MILLIS = 120_000L
