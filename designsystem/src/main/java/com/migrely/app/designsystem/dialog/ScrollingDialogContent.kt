package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.PageScroll

/**
 * An alert dialog's title and content in one area that scrolls, for the dialog's `text` slot with no `title` (T23 8.1
 * rows R03, R04). Material's title slot does not scroll: at large fonts in a short window a long title, or stacked
 * buttons, left the content no height at all. Here the title scrolls away before the content is cut. It keeps the
 * dialog title's style, is a heading, and names the pane for TalkBack. [modifier] applies to the visible area, before
 * the scroll.
 */
@Composable
fun ScrollingDialogContent(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    PageScroll {
        // The top padding leaves room for the title's tall letters, drawn above its line at large fonts.
        Column(
            modifier
                .pageScroll(rememberScrollState())
                .padding(top = 4.dp)
                .semantics { paneTitle = title }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = AlertDialogDefaults.titleContentColor,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .semantics { heading() }
            )
            content()
        }
    }
}
