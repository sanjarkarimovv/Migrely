package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertEquals
import org.junit.Test

/** By its place in the project, so a file of the same name elsewhere is checked like any other. */
private const val ENTRY_POINT_PATH =
    "/designsystem/src/main/java/com/migrely/app/designsystem/layout/PageScroll.kt"

/** The page scroll with a typed field's keyboard follow on the same state, which only the entry point calls (#259). */
private const val TYPING_SPACE_PATH =
    "/designsystem/src/main/java/com/migrely/app/designsystem/input/TypingSpace.kt"

private const val PICKER_DIR = "/designsystem/src/main/java/com/migrely/app/designsystem/picker/"
private const val REMINDERS =
    "/features/reminders/ui/src/main/java/com/migrely/app/reminders/ui/view/RemindersScreen.kt"

/**
 * A scroll that does not go through the entry point: Compose's own scrolls, and lazy lists, which would need the same
 * fix and have none today.
 */
private val RAW_SCROLL = Regex(
    """\b(verticalScroll|horizontalScroll)\(|\.scrollable\(|""" +
        """\b(LazyColumn|LazyRow|LazyVerticalGrid|LazyVerticalStaggeredGrid|LazyHorizontalGrid|""" +
        """LazyHorizontalStaggeredGrid|HorizontalPager|VerticalPager)\s*[({]"""
)

/** The only lines in production that scroll by Compose's own means, each with its count. */
private val RAW_SCROLL_SITES = mapOf(ENTRY_POINT_PATH to 1, TYPING_SPACE_PATH to 1)

/** A call of the typing space's page scroll, not its declaration. */
private val SCROLLING_PAGE_CALL = Regex("""(?<!fun )\bscrollingPage\(""")

/** `PageScroll(exactBounds = false)`, however it is spaced, named or not. */
private val STEP_ONLY = Regex("""\bPageScroll\(\s*(exactBounds\s*=\s*)?false\s*\)""")

/**
 * Where only the half step applies (design D5 T2, T3; BRD D193): Material's date and time pickers, whose controls under
 * 48 dp need Compose's widening, and Reminders' time dialog, which shows the same time picker.
 */
private val STEP_ONLY_SITES = mapOf(
    "${PICKER_DIR}MigrelyDatePicker.kt" to 1,
    "${PICKER_DIR}MigrelyDateTimePicker.kt" to 2,
    REMINDERS to 1
)

private val COMMENT = Regex("""^\s*(//|/\*|\*)""")

private fun String.isRawScroll() =
    !COMMENT.containsMatchIn(this) && RAW_SCROLL.containsMatchIn(this)

/**
 * A page that scrolls by Compose's own means would keep TalkBack's loop and skip (issue #255, BRD D193): every scroll
 * goes through `PageScroll`'s `pageScroll`, which brings exact bounds and the half step. Only the entry point names a
 * raw scroll, and the typing space's `scrollingPage`, which only the entry point calls (BRD D194). Failures name each
 * file and line.
 */
class ArchitectureScrollsTest {
    private val files = Konsist.scopeFromProduction().files

    @Test
    fun `only the entry point scrolls by Compose's own means`() {
        val raw = files
            .flatMap { file ->
                file.text.lines().withIndex()
                    .filter { (_, line) -> line.isRawScroll() }
                    .map { (index, _) -> file.projectPath to index + 1 }
            }
        val unexpected = raw
            .filter { (path, _) -> path !in RAW_SCROLL_SITES }
            .map { (path, line) -> "$path:$line" }
            .sorted()

        assertEquals("raw scrolls: ${unexpected.size}", emptyList<String>(), unexpected)
        assertEquals(RAW_SCROLL_SITES, raw.groupingBy { (path, _) -> path }.eachCount())
    }

    @Test
    fun `only the entry point calls the typing space's page scroll`() {
        val callers = files
            .flatMap { file ->
                file.text.lines().withIndex()
                    .filter { (_, line) ->
                        !COMMENT.containsMatchIn(line) && SCROLLING_PAGE_CALL.containsMatchIn(line)
                    }
                    .map { file.projectPath }
            }
            .groupingBy { it }
            .eachCount()

        assertEquals(mapOf(ENTRY_POINT_PATH to 1), callers)
    }

    @Test
    fun `only the pickers and the time dialog leave exact bounds out`() {
        val stepOnly = files
            .map { it.projectPath to STEP_ONLY.findAll(it.text).count() }
            .filter { (_, count) -> count > 0 }
            .toMap()

        assertEquals(STEP_ONLY_SITES, stepOnly)
    }
}
