package com.migrely.app.backup.domain.model

/** BRD BAK-F3, EC-BAK-04. */
enum class ImportModeDomainModel {
    /** Everything on the phone gives way to the backup, after an automatic backup. */
    Replace,

    /** The backup's records the phone does not have yet are added. */
    Add
}
