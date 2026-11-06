package com.migrely.app.designsystem.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** What a limited text field shows, and what to do with an edit of it. */
class LimitedField(val value: TextFieldValue, val onValueChange: (TextFieldValue) -> Unit)

/**
 * A text field holding at most [maxLength] units of a [text] stored by the caller. What is shown is what is stored: the
 * field only adds the cursor, and any other change of [text] (a load, a merge, another write in the same frame) puts
 * the cursor at the end. Edits go through [limitedEdit]. [onTextChange] hears a change of the text, never a cursor move
 * or a refused edit. Changing [keys] forgets the cursor.
 */
@Composable
fun rememberLimitedField(
    text: String,
    maxLength: Int,
    vararg keys: Any?,
    onTextChange: (String) -> Unit
): LimitedField {
    var field by rememberSaveable(*keys, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(atTheEnd(text))
    }
    val shown = if (field.text == text) field else atTheEnd(text)
    return LimitedField(shown) { edited ->
        val limited = limitedEdit(shown, edited, maxLength)
        field = limited
        if (limited.text != shown.text) onTextChange(limited.text)
    }
}

private fun atTheEnd(text: String) = TextFieldValue(text, TextRange(text.length))
