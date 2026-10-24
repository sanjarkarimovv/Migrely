package com.migrely.app.medication.domain.model

sealed interface ActiveCountResultDomainModel {
    data class Counted(val count: Int) : ActiveCountResultDomainModel

    data object Failed : ActiveCountResultDomainModel
}
