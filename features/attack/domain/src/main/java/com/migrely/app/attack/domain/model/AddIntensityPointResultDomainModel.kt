package com.migrely.app.attack.domain.model

sealed interface AddIntensityPointResultDomainModel {
    /** [firstMaximum] is true when this is the first 10 the user has ever recorded (BRD SAF-F2). */
    data class Added(val firstMaximum: Boolean) : AddIntensityPointResultDomainModel

    data object OutsideAttack : AddIntensityPointResultDomainModel

    data object InFuture : AddIntensityPointResultDomainModel

    data object NotFound : AddIntensityPointResultDomainModel

    data object StorageFull : AddIntensityPointResultDomainModel

    data object Failed : AddIntensityPointResultDomainModel
}
