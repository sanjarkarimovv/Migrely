package com.migrely.app.l10n

import org.junit.Assert.assertEquals
import org.junit.Test

private const val LIMIT = 16

/**
 * How the gate counts a word (design D6, cr-de round 2): a word Android cannot break counts whole, however it is
 * joined or encoded. Each fixture is over 16 characters only when counted that way.
 */
class WordsOverTest {
    @Test
    fun `Given a word over the limit then it is counted`() {
        assertEquals(listOf("Migrely-Sicherung"), wordsOver(LIMIT, "Keine Migrely-Sicherung"))
    }

    @Test
    fun `Given a word at the limit then it is not counted`() {
        assertEquals(emptyList<String>(), wordsOver(LIMIT, "Kopfschmerztagen"))
    }

    /** "Migränetagebücher" (17) with its umlauts as base letter plus combining diaeresis. */
    @Test
    fun `Given a decomposed word then it is counted as composed`() {
        assertEquals(
            listOf("Migränetagebücher"),
            wordsOver(LIMIT, "Migränetagebücher")
        )
    }

    @Test
    fun `Given a compound joined by non-breaking hyphens then it is one word`() {
        assertEquals(
            listOf("Ende‑zu‑Ende‑Schutz"),
            wordsOver(LIMIT, "Ende‑zu‑Ende‑Schutz")
        )
    }

    @Test
    fun `Given two words joined by a no-break space then they are one word`() {
        assertEquals(
            listOf("Kopfschmerz Tagebuch"),
            wordsOver(LIMIT, "Dein Kopfschmerz Tagebuch")
        )
    }

    @Test
    fun `Given a placeholder joined by a hyphen then it is part of the word`() {
        assertEquals(
            listOf("%1\$s-Sicherungsdatei"),
            wordsOver(LIMIT, "Die %1\$s-Sicherungsdatei fehlt")
        )
    }

    @Test
    fun `Given a no-break space between a number and its unit then nothing is counted`() {
        assertEquals(emptyList<String>(), wordsOver(LIMIT, "Vor 12 Min. notiert"))
    }
}
