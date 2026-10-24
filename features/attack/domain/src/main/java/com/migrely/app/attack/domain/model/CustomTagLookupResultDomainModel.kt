package com.migrely.app.attack.domain.model

sealed interface CustomTagLookupResultDomainModel {
    data class Found(val tag: CustomTagDomainModel) : CustomTagLookupResultDomainModel

    data object None : CustomTagLookupResultDomainModel

    data object Failed : CustomTagLookupResultDomainModel
}
