package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel

interface GetMaximumRecordedRepository {
    /** Whether any attack the user still has holds a pain entry of 10. */
    fun maximumRecorded(): MaximumRecordedResultDomainModel
}
