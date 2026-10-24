package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel

/** Turns the stored level (0 to 3) into an impact; a level this version does not know reads as none stored. */
class FunctionalImpactDomainMapper {
    fun toDomain(level: Int): FunctionalImpactDomainModel? =
        FunctionalImpactDomainModel.entries.firstOrNull { it.level == level }
}
