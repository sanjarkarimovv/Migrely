package com.migrely.app.attack.domain.repository

/**
 * Removes for good the attacks deleted before the undo window, with what hangs on them (BRD D23, D118). A purge that
 * fails is quiet: the attacks stay hidden and go with the next try.
 */
interface PurgeExpiredDeletionsRepository {
    fun purgeExpiredDeletions()
}
