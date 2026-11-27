package com.migrely.app.designsystem.sky

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.text.widestWidth
import kotlin.math.ceil
import kotlin.math.floor

/**
 * The one style that all labels of a calendar grid share (BRD D198, the only exception to "type never shrinks"):
 * [style] itself when the widest of [samples], measured in [measuredWeight] if given, fits [widthPx] on one line;
 * otherwise the largest whole-pixel size that fits; never below [style]'s size at 100%. Measured, not multiplied by
 * the font scale, because Android 14 scales sizes non-linearly. It is worked out again only when the width, the
 * style, the samples or the density (font scale included) change, never per month, so paging never resizes them.
 */
@Composable
internal fun rememberFittedStyle(
    style: TextStyle,
    samples: List<String>,
    widthPx: Int,
    measuredWeight: FontWeight? = null
): TextStyle {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(measurer, density, style, samples, widthPx, measuredWeight) {
        fittedStyle(measurer, density, style, samples, widthPx, measuredWeight)
    }
}

private fun fittedStyle(
    measurer: TextMeasurer,
    density: Density,
    style: TextStyle,
    samples: List<String>,
    widthPx: Int,
    measuredWeight: FontWeight?
): TextStyle {
    val measured = style.copy(fontWeight = measuredWeight ?: style.fontWeight)
    fun widest(sized: TextStyle) = measurer.widestWidth(samples, sized, density)
    if (samples.isEmpty() || widest(measured) <= widthPx) return style
    with(density) {
        val hundredPercentPx = style.fontSize.value.dp.toPx()
        var fittedPx = hundredPercentPx
        var low = ceil(hundredPercentPx).toInt()
        var high = floor(style.fontSize.toPx()).toInt()
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (widest(measured.copy(fontSize = middle.toFloat().toSp())) <= widthPx) {
                fittedPx = middle.toFloat()
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return style.copy(fontSize = fittedPx.toSp())
    }
}
