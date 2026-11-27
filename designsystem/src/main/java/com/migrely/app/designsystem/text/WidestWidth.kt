package com.migrely.app.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density

/**
 * The width in px of the widest of [texts], each laid out on one line in [style], with its font features: for a
 * layout that must know whether a word or a label fits before it lays the Text out (BRD D181 (5), D198). Worked out
 * again only when the texts, the style or the density (font scale included) change.
 */
@Composable
fun rememberWidestWidth(texts: List<String>, style: TextStyle): Int {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(measurer, density, texts, style) { measurer.widestWidth(texts, style, density) }
}

/** The width in px of the widest of [texts], each on one line in [style]; 0 for none. */
internal fun TextMeasurer.widestWidth(
    texts: List<String>,
    style: TextStyle,
    density: Density
): Int = texts.maxOfOrNull { text ->
    measure(text, style, softWrap = false, maxLines = 1, density = density).size.width
} ?: 0
