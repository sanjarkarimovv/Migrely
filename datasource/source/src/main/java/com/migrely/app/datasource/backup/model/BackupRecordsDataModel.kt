package com.migrely.app.datasource.backup.model

/**
 * Every health record on the phone, as stored (BRD D71). Attacks being deleted are not part of it, and neither is
 * device state such as reminder settings or overuse notices.
 */
data class BackupRecordsDataModel(
    val attacks: List<BackupAttackDataModel>,
    val customTags: List<BackupCustomTagDataModel>,
    val medications: List<BackupMedicationDataModel>,
    val intakes: List<BackupIntakeDataModel>,
    val dayMarks: List<BackupDayMarkDataModel>
)
