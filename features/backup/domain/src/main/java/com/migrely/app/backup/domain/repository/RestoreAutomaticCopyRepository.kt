package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.RestoreResultDomainModel

/**
 * Reads the newest automatic copy and replaces every health record with it, in one transaction, as a "Replace" import
 * does. With [saveCopyFirst], what the phone holds is saved as a new automatic copy first, once the copy has been read;
 * when that cannot be saved, nothing is replaced (BRD D98).
 */
interface RestoreAutomaticCopyRepository {
    fun restoreAutomaticCopy(saveCopyFirst: Boolean): RestoreResultDomainModel
}
