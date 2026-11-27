package com.migrely.app.attack.domain.model

sealed interface RestoreCustomTagResultDomainModel {
    data object Restored : RestoreCustomTagResultDomainModel

    /** The kind's list already holds the most active tags; the tag stays archived (BRD EC-ATK-12, EC-ATK-26). */
    data object LimitReached : RestoreCustomTagResultDomainModel

    data object StorageFull : RestoreCustomTagResultDomainModel

    data object Failed : RestoreCustomTagResultDomainModel
}
