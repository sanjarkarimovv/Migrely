package com.migrely.app.attack.domain.model

/**
 * How adding a pain entry ended. [firstMaximum] is true when the entry is a 10 and no saved attack
 * has a 10, or the saved 10s cannot be read, whether or not this entry was saved (BRD SAF-F2,
 * D163). An attack that is gone shows no card.
 */
sealed interface AddIntensityPointResultDomainModel {
    val firstMaximum: Boolean

    data class Added(override val firstMaximum: Boolean) : AddIntensityPointResultDomainModel

    data class OutsideAttack(override val firstMaximum: Boolean) :
        AddIntensityPointResultDomainModel

    data class InFuture(override val firstMaximum: Boolean) : AddIntensityPointResultDomainModel

    data object NotFound : AddIntensityPointResultDomainModel {
        override val firstMaximum = false
    }

    data class StorageFull(override val firstMaximum: Boolean) : AddIntensityPointResultDomainModel

    data class Failed(override val firstMaximum: Boolean) : AddIntensityPointResultDomainModel
}
