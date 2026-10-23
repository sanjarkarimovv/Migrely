package com.migrely.app.home.domain.model

sealed interface TodaySnapshotResultDomainModel {
    data class Loaded(val snapshot: TodaySnapshotDomainModel) : TodaySnapshotResultDomainModel

    data object Failed : TodaySnapshotResultDomainModel
}
