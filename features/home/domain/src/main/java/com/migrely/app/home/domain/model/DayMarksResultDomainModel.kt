package com.migrely.app.home.domain.model

sealed interface DayMarksResultDomainModel {
    data class Loaded(val marks: DayMarksDomainModel) : DayMarksResultDomainModel

    data object Failed : DayMarksResultDomainModel
}
