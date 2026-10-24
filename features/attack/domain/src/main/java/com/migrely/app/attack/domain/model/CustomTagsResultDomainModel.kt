package com.migrely.app.attack.domain.model

sealed interface CustomTagsResultDomainModel {
    data class Loaded(val tags: List<CustomTagDomainModel>) : CustomTagsResultDomainModel

    data object Failed : CustomTagsResultDomainModel
}
