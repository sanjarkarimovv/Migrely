package com.migrely.app.onboarding.domain.repository

import com.migrely.app.onboarding.domain.model.PhoneRecordsDomainModel

interface GetPhoneRecordsRepository {
    fun phoneRecords(): PhoneRecordsDomainModel
}
