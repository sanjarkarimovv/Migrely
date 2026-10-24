package com.migrely.app.backup.data.mapper

import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel

class BackupSummaryDomainMapper {
    fun toDomain(records: BackupRecordsDataModel) = BackupSummaryDomainModel(
        attacks = records.attacks.size,
        medications = records.medications.size,
        dayMarks = records.dayMarks.size
    )
}
