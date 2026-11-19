package com.migrely.app.designsystem.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics

/**
 * A control inside a page says its [name] from its own node (#255, BRD D193 (5), D181 (2)). The page's edge can cut the
 * control's Text out of TalkBack's tree while the control itself stays a stop, which then said only its role or state.
 * Give the Text that shows the name [wordsShownOnly], so it is heard once. Other lines (a row's summary, a value, a
 * field's counter) stay ordinary children: TalkBack says them while they are in view. Android gets only one content
 * description from Compose, so a name is one string.
 */
fun Modifier.ownWords(name: String): Modifier = semantics { contentDescription = name }

/**
 * A Text whose words its control already says ([ownWords]): shown, not read again. It stays in the semantics tree,
 * so tests and Voice Access still find the control by the words it shows.
 */
fun Modifier.wordsShownOnly(): Modifier = semantics { hideFromAccessibility() }
