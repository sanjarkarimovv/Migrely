package com.migrely.app.home.domain.model

sealed interface TodayResultDomainModel {
    data class Loaded(val today: TodayDomainModel) : TodayResultDomainModel

    data object Failed : TodayResultDomainModel
}
