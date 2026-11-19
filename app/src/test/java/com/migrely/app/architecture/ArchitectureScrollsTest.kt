package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertEquals
import org.junit.Test

/** By its place in the project, so a file of the same name elsewhere is checked like any other. */
private const val ENTRY_POINT_PATH =
    "/designsystem/src/main/java/com/migrely/app/designsystem/layout/PageScroll.kt"

private val RAW_SCROLL = Regex("""\bverticalScroll\(""")

private val COMMENT = Regex("""^\s*(//|/\*|\*)""")

/**
 * A page that calls Compose's `verticalScroll` itself would keep TalkBack's loop and skip (issue #255, BRD D193):
 * every vertical scroll goes through `PageScroll`'s `pageScroll`, which brings exact bounds and the half step.
 * Only the entry point's file names the raw call. Failures name each file and line.
 */
class ArchitectureScrollsTest {
    @Test
    fun `only the entry point calls verticalScroll`() {
        val raw = Konsist.scopeFromProduction()
            .files
            .filter { it.projectPath != ENTRY_POINT_PATH }
            .flatMap { file ->
                file.text.lines().withIndex()
                    .filter { (_, line) -> line.isRawScroll() }
                    .map { (index, _) -> "${file.projectPath}:${index + 1}" }
            }
            .sorted()

        assertEquals("raw verticalScroll calls: ${raw.size}", emptyList<String>(), raw)
    }
}

private fun String.isRawScroll() =
    !COMMENT.containsMatchIn(this) && RAW_SCROLL.containsMatchIn(this)
