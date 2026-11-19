package com.migrely.app.designsystem.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics

/**
 * A control inside a page says [words] from its own node, a row's lines in order (TalkBack joins them with commas) (#255, BRD D193 (5), D181 (2)). The page's edge can cut the
 * control's Text out of TalkBack's tree while the control itself stays a stop, which then said only its role or state.
 * Give its Text [wordsShownOnly], so the words are heard once.
 */
fun Modifier.ownWords(vararg words: String?): Modifier =
    semantics { this[SemanticsProperties.ContentDescription] = words.filterNotNull() }

/**
 * A Text whose words its control already says ([ownWords]): shown, not read again. It stays in the semantics tree,
 * so tests and Voice Access still find the control by the words it shows.
 */
fun Modifier.wordsShownOnly(): Modifier = semantics { hideFromAccessibility() }
