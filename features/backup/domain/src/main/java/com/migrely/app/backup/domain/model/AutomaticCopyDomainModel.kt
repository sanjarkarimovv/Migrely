package com.migrely.app.backup.domain.model

import java.time.Instant

/** The newest automatic copy, saved before the phone's records were last replaced (BRD D73, D98). */
sealed interface AutomaticCopyDomainModel {
    data class Available(val madeAt: Instant, val summary: BackupSummaryDomainModel) :
        AutomaticCopyDomainModel

    /** No copy, or none that can be read. */
    data object None : AutomaticCopyDomainModel
}
