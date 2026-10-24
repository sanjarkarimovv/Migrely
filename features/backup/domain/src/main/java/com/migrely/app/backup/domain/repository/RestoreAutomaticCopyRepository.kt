package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import java.time.Instant

/**
 * Reads the automatic copy made at [madeAt], the one the user was shown, and replaces every health record with it, in
 * one transaction, as a "Replace" import does. With [saveCopyFirst], what the phone holds is saved as a new automatic
 * copy first, once the copy has been read; when that cannot be saved, nothing is replaced. A copy that is gone fails
 * like one that cannot be read (BRD D98).
 */
interface RestoreAutomaticCopyRepository {
    fun restoreAutomaticCopy(madeAt: Instant, saveCopyFirst: Boolean): RestoreResultDomainModel
}
