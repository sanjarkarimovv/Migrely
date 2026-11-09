package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Material's `DropdownMenu`, with a lift on its card counted for the windows beneath, as a lift on
 * a dialog's card is (issue #161, BRD D164). The first tap of a double tap on an item can close
 * the menu, and the second would then land on the screen beneath. Material puts the `modifier` on
 * the card's column, in the popup's own window, so it sees every press on the card, its padding
 * included, before an item does. There is no wait: the menu opens on a long press, with the finger
 * still down, so the person sees it before the next tap. It adds no semantics and draws nothing.
 */
@Composable
fun MigrelyDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.liftOnCard(),
        content = content
    )
}
