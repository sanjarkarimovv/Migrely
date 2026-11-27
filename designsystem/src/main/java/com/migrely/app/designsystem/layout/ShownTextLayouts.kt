package com.migrely.app.designsystem.layout

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.text.TextLayoutResult

/**
 * The latest layouts of Texts that no screen reader hears on their own, because their control says its own words
 * and they sit under `clearAndSetSemantics`: a calendar's day numbers and weekday initials, a History row's date and
 * peak. Set it in that block with [shownTextLayouts] and let each Text [record] its layout; tests then read the lines
 * to prove that a number or a word is never broken or cut (BRD D198, D181 (5)). It is not important for
 * accessibility and never merges into the control, so what TalkBack says does not change.
 */
class ShownTextLayouts {
    private val layouts = sortedMapOf<Int, TextLayoutResult>()

    /** The latest layout of each Text, in the order of their indexes. */
    val all: List<TextLayoutResult>
        get() = layouts.values.toList()

    /** Keeps [layout] as the latest layout of the Text at [index]. */
    fun record(index: Int, layout: TextLayoutResult) {
        layouts[index] = layout
    }
}

/** The key of [shownTextLayouts]. A control that merges its children never takes it over from them. */
val ShownTextLayoutsKey = SemanticsPropertyKey<ShownTextLayouts>(
    name = "ShownTextLayouts",
    mergePolicy = { parentValue, _ -> parentValue }
)

/** See [ShownTextLayouts]. */
var SemanticsPropertyReceiver.shownTextLayouts by ShownTextLayoutsKey
