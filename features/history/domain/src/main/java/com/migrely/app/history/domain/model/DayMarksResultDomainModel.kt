package com.migrely.app.history.domain.model

sealed interface DayMarksResultDomainModel {
    data class Loaded(val marks: List<DayMarkDomainModel>) : DayMarksResultDomainModel

    data object Failed : DayMarksResultDomainModel
}
