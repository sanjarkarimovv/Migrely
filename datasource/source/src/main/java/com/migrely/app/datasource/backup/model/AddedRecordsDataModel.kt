package com.migrely.app.datasource.backup.model

/**
 * What "Add" did with a backup's attacks. [skippedAttacks] were already on the phone, either by their ID or because
 * they overlap an attack there.
 */
data class AddedRecordsDataModel(val addedAttacks: Int, val skippedAttacks: Int)
