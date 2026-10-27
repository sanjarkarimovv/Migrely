package com.migrely.app.history.domain.model

/** The three pain levels the skies and the month's numbers use, with their order (BRD EC-UX-09, D56). */
enum class PainLevelDomainModel(val rank: Int) {
    Mild(1),
    Moderate(2),
    Severe(3);

    companion object {
        private val MILD = 1..3
        private val MODERATE = 4..6

        /** The level of a 1–10 pain value. */
        fun of(value: Int): PainLevelDomainModel = when (value) {
            in MILD -> Mild
            in MODERATE -> Moderate
            else -> Severe
        }
    }
}
