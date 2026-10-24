package com.migrely.app.history.domain.model

sealed interface SetDayMarkResultDomainModel {
    data object Done : SetDayMarkResultDomainModel

    /** A mark cannot be put on a day that has not come yet (BRD EC-DAY-03). */
    data object FutureDay : SetDayMarkResultDomainModel

    /** A day that an attack touches is never pain-free (BRD EC-DAY-01). */
    data object AttackLogged : SetDayMarkResultDomainModel

    data object StorageFull : SetDayMarkResultDomainModel

    data object Failed : SetDayMarkResultDomainModel
}
