package com.migrely.app.history.domain.model

sealed interface DayMarkWriteResultDomainModel {
    data object Written : DayMarkWriteResultDomainModel

    data object StorageFull : DayMarkWriteResultDomainModel

    data object Failed : DayMarkWriteResultDomainModel
}
