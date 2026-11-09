package com.migrely.app.backup.domain.repository

interface PlanRemindersAgainRepository {
    /** Every reminder is planned again soon, by the records an import just changed (BRD D168, design D7). */
    fun planRemindersAgain()
}
