package com.migrely.app.attack.domain.model

sealed interface CreateCustomTagResultDomainModel {
    data class Created(val tag: CustomTagDomainModel) : CreateCustomTagResultDomainModel

    data object StorageFull : CreateCustomTagResultDomainModel

    data object Failed : CreateCustomTagResultDomainModel
}
