package com.migrely.app.attack.domain.model

/** How much the attack stopped the user (BRD ATK-F5): from 0, no impact, to 3, bed rest. */
enum class FunctionalImpactDomainModel(val level: Int) {
    None(0),
    LessActive(1),
    HadToStop(2),
    BedRest(3)
}
