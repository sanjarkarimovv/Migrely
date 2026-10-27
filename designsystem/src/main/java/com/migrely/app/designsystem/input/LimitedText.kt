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
    val edit = limited(
        old = old,
        new = new,
        maxLength = maxLength,
        keptStartAtMost = old.length,
        keptEndAtMost = old.length
    )
    return edit?.text ?: old
}

/**
 * [limitedEdit] for a field that also keeps its cursor. The edit is taken to replace what lay between [old]'s
 * selection bounds, so a paste that happens to start like the text after the cursor is still cut at its own end. A
 * refused edit keeps [old] as it was, cursor included; a shortened one leaves the cursor right after what it inserted.
 */
fun limitedEdit(old: TextFieldValue, new: TextFieldValue, maxLength: Int): TextFieldValue {
    if (new.text.length <= maxLength || new.text.length <= old.text.length) return new
    val edit = limited(
        old = old.text,
        new = new.text,
        maxLength = maxLength,
        keptStartAtMost = old.selection.min,
        keptEndAtMost = old.text.length - old.selection.max
    ) ?: return old
    return TextFieldValue(edit.text, TextRange(edit.cursor))
}

private class LimitedEdit(val text: String, val cursor: Int)

/**
 * [old] with the text [new] inserts kept only as far as it fits, or null when nothing fits. The unchanged start and
 * end are the parts [old] and [new] share, at most [keptStartAtMost] and [keptEndAtMost] units long.
 */
private fun limited(
    old: String,
    new: String,
    maxLength: Int,
    keptStartAtMost: Int,
    keptEndAtMost: Int
): LimitedEdit? {
    val start = old.commonPrefixWith(new).length.coerceAtMost(keptStartAtMost)
    val shared = old.substring(start).commonSuffixWith(new.substring(start))
    val end = shared.length.coerceAtMost(keptEndAtMost)
    val inserted = new.substring(start, new.length - end)
    val fitting = inserted.wholeCharactersWithin(maxLength - start - end)
    if (fitting.isEmpty()) return null
    return LimitedEdit(
        text = old.substring(0, start) + fitting + old.substring(old.length - end),
        cursor = start + fitting.length
    )
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
