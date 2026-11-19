package com.migrely.app.attack.ui.view

import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.semantics.SemanticsNode

/**
 * What lies between a typing field and the page that scrolls it (BRD D194): the page keeps the field in view by
 * scrolling its own [androidx.compose.foundation.ScrollState] in its layout pass, which moves the field only if the
 * page's scroll is the nearest one above it. Returns null where no other scroll or nested-scroll node sits between
 * them, or what is in the way. Modifier element names are Compose's own (1.9): a renamed element fails this loudly
 * rather than passing. The same file sits in each UI module's tests, which share no test module.
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
