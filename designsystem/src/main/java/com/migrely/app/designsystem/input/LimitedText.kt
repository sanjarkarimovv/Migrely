package com.migrely.app.designsystem.input

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.BreakIterator

/**
 * What a text field holding at most [maxLength] UTF-16 units shows after the user edits [old] into [new]: the edit as
 * typed when it fits or shortens the text; otherwise only as much of the typed or pasted text as fits, cut after a
 * whole character, so an emoji is never split and nothing elsewhere in the text is lost (BRD EC-ATK-13, QA #80, #81).
 * When nothing fits, the text stays [old].
 */
fun limitedEdit(old: String, new: String, maxLength: Int): String {
    if (new.length <= maxLength || new.length <= old.length) return new
    val prefix = old.commonPrefixWith(new).length
    val suffix = old.substring(prefix).commonSuffixWith(new.substring(prefix)).length
    val inserted = new.substring(prefix, new.length - suffix)
    val fitting = inserted.wholeCharactersWithin(maxLength - prefix - suffix)
    if (fitting.isEmpty()) return old
    return old.substring(0, prefix) + fitting + old.substring(old.length - suffix)
}

/**
 * [limitedEdit] for a field that also keeps its cursor: a refused edit keeps [old] as it was, cursor included, and a
 * shortened insertion leaves the cursor right after what was inserted.
 */
fun limitedEdit(old: TextFieldValue, new: TextFieldValue, maxLength: Int): TextFieldValue =
    when (val text = limitedEdit(old.text, new.text, maxLength)) {
        new.text -> new
        old.text -> old
        else -> {
            val cut = new.text.length - text.length
            val cursor = (new.selection.end - cut).coerceIn(0, text.length)
            TextFieldValue(text, TextRange(cursor))
        }
    }

/** The longest start of this text, at most [length] units long, that ends after a whole character. */
private fun String.wholeCharactersWithin(length: Int): String {
    if (length <= 0) return ""
    val characters = BreakIterator.getCharacterInstance().also { it.setText(this) }
    var end = 0
    var next = characters.next()
    while (next != BreakIterator.DONE && next <= length) {
        end = next
        next = characters.next()
    }
    return substring(0, end)
}
