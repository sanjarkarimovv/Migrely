package com.migrely.app.l10n

import com.migrely.app.locale.APP_LANGUAGES
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

private const val ENGLISH = "en"
private const val LONGEST_UNLISTED_WORD = 16
private val PLACEHOLDER = Regex("""%(\d+\$)?[-#+ 0,(]*\d*(\.\d+)?[a-zA-Z%]""")

// A hyphenated compound counts as one word: Android does not break a line at a written hyphen (ux R16, R18).
private val WORD = Regex("""\p{L}+(-\p{L}+)*""")
private val LANGUAGE_FOLDER = Regex("""values-([a-z]{2,3})(-r[A-Z]{2})?""")
private val SKIPPED_FOLDERS = setOf("build", ".git", ".gradle", "node_modules", "website")
private val MAIN_RES = "src${File.separator}main${File.separator}res"
private val INVISIBLE_BREAKS = Regex("[\u00AD\u200B\u200C\u200D\u2060\uFEFF]")
private val ESCAPE = Regex("""\\(u[0-9a-fA-F]{4}|.)""")
private val UNESCAPED_QUOTE = Regex("""(?<!\\)"""")
private const val UNICODE_ESCAPE_LENGTH = 5
private const val HEX = 16
private val NUMBER_SPACE_UNIT = Regex("""(\d|%(\d+\$)?d) (Min\.|Std\.|Tg\.|%%|mg\b|€)""")

/**
 * No untranslated text passes the gate (BRD D195 (9), EC-L10N-12, EC-L10N-13, design D6). Android Lint checks
 * strings inside a module that has a translation; this test also sees a module with none, placeholders of every kind,
 * copied English and long German words:
 * 1. every module's `values/strings.xml` has a `values-<lang>/strings.xml` for each language besides English;
 * 2. the names of strings, plurals and arrays match, after dropping `translatable="false"`;
 * 3. placeholders match as a multiset (a plural's `one` may drop the number), arrays have as many items;
 * 4. a value equal to English fails unless it is on the reviewed list `l10n/same-as-english.txt`;
 * 5. a word over 16 characters, a hyphenated compound counted whole, fails unless it is on
 *    `l10n/long-german-words.txt` with the screen where it was seen;
 * 6. `res/xml/locales_config.xml`, [APP_LANGUAGES] and the translated folders name the same languages;
 * 7. no soft hyphen or zero-width character, and a number keeps its unit on its line (a no-break space).
 *
 * Values are read as aapt2 compiles them: Android's escapes (`\uXXXX`, `\n`, `\'`, `\"`, `\@`, `\?`) are decoded and
 * unescaped double quotes dropped, so an escaped soft hyphen is seen like a typed one.
 */
class LocalizationCompletenessTest {
    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").isFile }

    private val englishFiles: List<File> = root.walkTopDown()
        .onEnter { it.name !in SKIPPED_FOLDERS }
        .filter { it.path.endsWith("$MAIN_RES${File.separator}values${File.separator}strings.xml") }
        .sortedBy { it.path }
        .toList()

    private val translatedLanguages = APP_LANGUAGES - ENGLISH

    private val sameAsEnglish: Set<String> = listFile("same-as-english.txt")
        .map { it.substringBefore('\t') }
        .toSet()

    private val longWords: Map<String, String> = listFile("long-german-words.txt")
        .associate { it.substringBefore('\t') to it.substringAfter('\t', "") }

    @Test
    fun `Every module with English strings has a file for each other language`() {
        assertTrue("No English strings found under $root", englishFiles.size >= 12)
        val missing = englishFiles.flatMap { english ->
            translatedLanguages.map { translation(english, it) }.filterNot(File::isFile)
        }
        assertNoProblems(missing.map { "missing ${it.relativeTo(root)}" })
    }

    @Test
    fun `Every translation has the same names, placeholders and items as English`() {
        assertNoProblems(
            translationPairs().flatMap { (english, translated) ->
                namesProblems(english, translated) + placeholderProblems(english, translated)
            }
        )
    }

    @Test
    fun `No translated value is a copy of English unless reviewed`() {
        assertNoProblems(
            translationPairs().flatMap { (english, translated) ->
                val sources = english.texts()
                translated.texts()
                    .filter { (key, value) -> isUnreviewedCopy(value, sources[key]) }
                    .map { (key, value) -> "${translated.label}: $key copies English \"$value\"" }
            }
        )
    }

    @Test
    fun `No German word over 16 letters unless seen in place`() {
        val unlisted = translationPairs().flatMap { (_, translated) ->
            translated.texts().flatMap { (key, value) ->
                WORD.findAll(value.replace(PLACEHOLDER, " ").replace(INVISIBLE_BREAKS, ""))
                    .map { it.value }
                    .filter { it.length > LONGEST_UNLISTED_WORD && it !in longWords }
                    .map { "${translated.label}: $key has \"$it\" (${it.length} letters)" }
                    .toList()
            }
        }
        val unseen = longWords.filterValues(String::isBlank).keys.map {
            "long-german-words.txt: \"$it\" names no screen where it was seen"
        }
        assertNoProblems(unlisted + unseen)
    }

    @Test
    fun `The system's app language list matches the translations`() {
        val config = parse(File(root, "app/src/main/res/xml/locales_config.xml"))
        val configured = config.getElementsByTagName("locale").let { nodes ->
            (0 until nodes.length).map { (nodes.item(it) as Element).getAttribute("android:name") }
        }.toSet()
        val folders = englishFiles.flatMap { english ->
            resFolder(english).listFiles().orEmpty()
                .filter { File(it, "strings.xml").isFile }
                .mapNotNull { LANGUAGE_FOLDER.matchEntire(it.name)?.groupValues?.get(1) }
        }.toSet() + ENGLISH

        assertEquals("locales_config.xml against APP_LANGUAGES", APP_LANGUAGES, configured)
        assertEquals("values-<lang> folders against APP_LANGUAGES", APP_LANGUAGES, folders)
    }

    @Test
    fun `No invisible break and no number apart from its unit`() {
        assertNoProblems(
            translationPairs().flatMap { (_, translated) ->
                translated.texts().flatMap { (key, value) ->
                    listOfNotNull(
                        "${translated.label}: $key has a soft hyphen or zero-width character"
                            .takeIf { INVISIBLE_BREAKS.containsMatchIn(value) },
                        "${translated.label}: $key needs a no-break space before its unit"
                            .takeIf { NUMBER_SPACE_UNIT.containsMatchIn(value) }
                    )
                }
            }
        )
    }

    private fun translationPairs(): List<Pair<Strings, Strings>> = englishFiles.flatMap { english ->
        translatedLanguages.map { translation(english, it) }.filter(File::isFile)
            .map { Strings.of(english, root) to Strings.of(it, root) }
    }

    private fun namesProblems(english: Strings, translated: Strings): List<String> =
        Kind.entries.flatMap { kind ->
            val expected = english.names(kind)
            val actual = translated.names(kind)
            (expected - actual).map { "${translated.label}: missing ${kind.tag} $it" } +
                (actual - expected).map { "${translated.label}: extra ${kind.tag} $it" }
        } + translated.untranslatable.map { "${translated.label}: $it is marked not translatable" }

    private fun placeholderProblems(english: Strings, translated: Strings): List<String> {
        val problems = mutableListOf<String>()
        english.strings.forEach { (key, source) ->
            val value = translated.strings[key] ?: return@forEach
            if (placeholders(source) != placeholders(value)) {
                problems += "${translated.label}: $key placeholders ${placeholders(value)}, " +
                    "English ${placeholders(source)}"
            }
        }
        english.plurals.forEach { (key, source) ->
            val items = translated.plurals[key] ?: return@forEach
            val other = source["other"].orEmpty()
            if (placeholders(items["other"].orEmpty()) != placeholders(other)) {
                problems +=
                    "${translated.label}: plural $key other placeholders differ from English"
            }
            items.filterKeys { it != "other" }.forEach { (quantity, value) ->
                if (!placeholders(other).toSet().containsAll(placeholders(value))) {
                    problems += "${translated.label}: plural $key $quantity has a placeholder " +
                        "English other lacks"
                }
            }
        }
        english.arrays.forEach { (key, source) ->
            val items = translated.arrays[key] ?: return@forEach
            if (items.size != source.size) {
                problems += "${translated.label}: array $key has ${items.size} items, " +
                    "English ${source.size}"
            }
            source.zip(items).forEachIndexed { index, (from, to) ->
                if (placeholders(from) != placeholders(to)) {
                    problems += "${translated.label}: array $key item $index placeholders differ"
                }
            }
        }
        return problems
    }

    private fun placeholders(value: String): List<String> =
        PLACEHOLDER.findAll(value).map { it.value }.sorted().toList()

    private fun isUnreviewedCopy(value: String, source: String?): Boolean =
        value.trim() == source?.trim() &&
            WORD.containsMatchIn(value.replace(PLACEHOLDER, " ")) &&
            value.trim() !in sameAsEnglish

    private fun translation(english: File, language: String) =
        File(resFolder(english), "values-$language/strings.xml")

    /** The `res` folder of an English `res/values/strings.xml`. */
    private fun resFolder(english: File): File = english.resolve("../..").normalize()

    private fun listFile(name: String): List<String> =
        File(root, "app/src/test/resources/l10n/$name").readLines()
            .map(String::trimEnd)
            .filter { it.isNotBlank() && !it.startsWith("#") }

    private fun assertNoProblems(problems: List<String>) {
        assertTrue(
            "${problems.size} localization problem(s):\n" + problems.joinToString("\n"),
            problems.isEmpty()
        )
    }

    private enum class Kind(val tag: String) {
        Single("string"),
        Quantity("plurals"),
        Items("string-array")
    }

    /** One `strings.xml`, with the entries marked `translatable="false"` set apart. */
    private class Strings(
        val label: String,
        val strings: Map<String, String>,
        val plurals: Map<String, Map<String, String>>,
        val arrays: Map<String, List<String>>,
        val untranslatable: List<String>
    ) {
        fun names(kind: Kind): Set<String> = when (kind) {
            Kind.Single -> strings.keys
            Kind.Quantity -> plurals.keys
            Kind.Items -> arrays.keys
        }

        /** Every text, keyed so that the English and translated entries line up. */
        fun texts(): Map<String, String> = strings +
            plurals.flatMap { (key, items) -> items.map { (q, v) -> "$key[$q]" to v } } +
            arrays.flatMap { (key, items) -> items.mapIndexed { i, v -> "$key[$i]" to v } }

        companion object {
            fun of(file: File, root: File): Strings {
                val resources = parse(file).documentElement
                val elements = childElements(resources)
                val (kept, untranslatable) = elements.partition {
                    it.getAttribute("translatable") != "false"
                }
                return Strings(
                    label = file.relativeTo(root).path,
                    strings = kept.filter { it.tagName == Kind.Single.tag }
                        .associate { it.getAttribute("name") to decoded(it.textContent) },
                    plurals = kept.filter { it.tagName == Kind.Quantity.tag }.associate { plural ->
                        plural.getAttribute("name") to childElements(plural)
                            .associate { it.getAttribute("quantity") to decoded(it.textContent) }
                    },
                    arrays = kept.filter { it.tagName == Kind.Items.tag }.associate { array ->
                        array.getAttribute("name") to
                            childElements(array).map { decoded(it.textContent) }
                    },
                    untranslatable = untranslatable.map { it.getAttribute("name") }
                )
            }
        }
    }
}

/** A value as aapt2 compiles it: unescaped double quotes dropped, escapes decoded. */
private fun decoded(raw: String): String =
    ESCAPE.replace(raw.replace(UNESCAPED_QUOTE, "")) { match ->
        val escaped = match.groupValues[1]
        when {
            escaped.length == UNICODE_ESCAPE_LENGTH ->
                escaped.drop(1).toInt(HEX).toChar().toString()
            escaped == "n" -> "\n"
            escaped == "t" -> "\t"
            else -> escaped
        }
    }

private fun parse(file: File) =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

private fun childElements(parent: Element): List<Element> =
    (0 until parent.childNodes.length).mapNotNull { parent.childNodes.item(it) as? Element }
