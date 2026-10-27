package com.migrely.app.attack.domain.model

sealed interface AddCustomTagResultDomainModel {
    /** The new tag, or the existing one with the same name, is in the list and selected on the attack. */
    data class Added(val tag: CustomTagDomainModel) : AddCustomTagResultDomainModel

    /** The trimmed name is empty or longer than 40 characters. */
    data object Invalid : AddCustomTagResultDomainModel

    /** The user already has 100 active tags of this kind (BRD EC-ATK-12). */
    data object LimitReached : AddCustomTagResultDomainModel

    data object StorageFull : AddCustomTagResultDomainModel

    data object Failed : AddCustomTagResultDomainModel
}
