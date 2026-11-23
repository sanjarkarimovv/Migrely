package com.migrely.app.designsystem.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * The range picker's headline, in place of Material's, whose one row squeezed the end until German "Enddatum" broke
 * letter by letter at 200% (T23 8.1 row R02, ux A22). Each side, a date or its placeholder, stays whole; the line
 * wraps only between the sides, and the dash stays with the start, so a wrapped first line visibly goes on. Style
 * and colour are Material's: the picker provides the text style, and both sides take its headline colour.
 *
 * TalkBack hears one node, [DateRangePickerLabels.headlineDescription], announced politely when a date changes, as
 * Material's was.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun RangeHeadline(
    startMillis: Long?,
    endMillis: Long?,
    dateFormatter: DatePickerFormatter,
    labels: DateRangePickerLabels,
    modifier: Modifier = Modifier,
    color: Color = DatePickerDefaults.colors().headlineContentColor
) {
    val locale = LocalConfiguration.current.locales[0]
    fun shown(millis: Long?, placeholder: String) =
        unbroken(dateFormatter.formatDate(millis, locale) ?: placeholder)
    fun spoken(millis: Long?) =
        dateFormatter.formatDate(millis, locale, forContentDescription = true) ?: labels.notChosen
    val description = String.format(
        locale,
        labels.headlineDescription,
        spoken(startMillis),
        spoken(endMillis)
    )
    FlowRow(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
            liveRegion = LiveRegionMode.Polite
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = shown(startMillis, labels.startDate) + NO_BREAK_SPACE + "–", color = color)
        Text(text = shown(endMillis, labels.endDate), color = color)
    }
}

private const val NO_BREAK_SPACE = ' '

/** A side never breaks inside: "1. Okt. 2026" stays on one line. */
private fun unbroken(text: String) = text.replace(' ', NO_BREAK_SPACE)
