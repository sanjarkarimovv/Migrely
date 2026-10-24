package com.migrely.app.datasource.preferences.model

/** How often migraines come, as the user said (BRD ONB-F1). [key] is what is stored and must never change. */
enum class AttackFrequencyDataModel(val key: String) {
    FewerThanFour("fewer_than_4"),
    FourToFourteen("4_to_14"),
    FifteenOrMore("15_or_more")
}
