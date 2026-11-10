package com.migrely.app.history.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * History opened by the doctor visit reminder opens the report through the top bar's own premium gate, [onOpen], once:
 * a rotation, a process restore or coming back from the report does not open it again (BRD D168, design D6).
 */
@Composable
internal fun OpenReportOnce(openReport: Boolean, onOpen: () -> Unit) {
    var opened by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(openReport) {
        if (openReport && !opened) {
            opened = true
            onOpen()
        }
    }
}
