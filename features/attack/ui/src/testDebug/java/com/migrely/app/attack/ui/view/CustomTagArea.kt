package com.migrely.app.attack.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.TextFieldValue
import com.migrely.app.designsystem.dialog.ScrollingDialogContent

/**
 * The custom tag dialog's text area as `MigrelyScrollingAlertDialog` lays it out for [CustomTagDialog], without the
 * dialog window, which under Robolectric never settles here: the title and [CustomTagNameField] in one scrolling area,
 * with the area's height fed back to the field.
 */
@Composable
internal fun CustomTagArea(
    title: String,
    name: TextFieldValue,
    onTypingIntent: () -> Unit = {},
    onNameChange: (TextFieldValue) -> Unit
) {
    var visibleHeight by remember { mutableIntStateOf(0) }
    ScrollingDialogContent(title, Modifier.onSizeChanged { visibleHeight = it.height }) {
        CustomTagNameField(name, visibleHeight, onTypingIntent, onNameChange = onNameChange)
    }
}
