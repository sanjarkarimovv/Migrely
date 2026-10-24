package com.migrely.app.datasource.backup.model

/**
 * What "Add" did with a backup. [skippedAttacks] were already on the phone, either by their ID or because they overlap
 * an attack there. [addedOthers] counts the medications, custom tags, intakes and marked days added.
 */
data class AddedRecordsDataModel(
    val addedAttacks: Int,
    val skippedAttacks: Int,
    val addedOthers: Int
)
