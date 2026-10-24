package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel

interface GetPhoneRecordsRepository {
    fun phoneRecords(): PhoneRecordsDomainModel
}
