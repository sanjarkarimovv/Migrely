package com.migrely.app.attack.domain.model

sealed interface CustomTagCountResultDomainModel {
    data class Counted(val count: Int) : CustomTagCountResultDomainModel

    data object Failed : CustomTagCountResultDomainModel
}
