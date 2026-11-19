package com.migrely.app.settings.ui.view

import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.semantics.SemanticsNode

/**
 * What lies between a following field and the page that scrolls it, for the keyboard's in-step follow (BRD D194,
 * design D3): the field hands its distance to its nearest nested-scroll parent, which must be the page's scroll, so no
 * other nested-scroll node may sit between them. Returns null where that holds, or what is in the way. Modifier element
 * names are Compose's own (1.9): a renamed element fails this loudly rather than passing. The same file sits in each
 * UI module's tests, which share no test module.
 */
internal fun SemanticsNode.inTheWayOfThePage(page: SemanticsNode): String? {
    var node: LayoutInfo? = layoutInfo.parentInfo
    while (node != null) {
        val names = node.getModifierInfo().map { it.modifier.javaClass.simpleName }
        val scroll = names.indexOf(SCROLL)
        if (scroll >= 0) {
            val inside = names.drop(scroll + 1).filter { it == NESTED_SCROLL }
            return when {
                node != page.layoutInfo -> "a scroll other than the page's: $names"
                inside.isNotEmpty() -> "nested scroll inside the page's scroll: $names"
                else -> null
            }
        }
        if (NESTED_SCROLL in names) return "a nested-scroll node before the page: $names"
        node = node.parentInfo
    }
    return "no scroll above the field"
}

private const val SCROLL = "ScrollingContainerElement"
private const val NESTED_SCROLL = "NestedScrollElement"
