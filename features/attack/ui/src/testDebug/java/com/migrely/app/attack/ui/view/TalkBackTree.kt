package com.migrely.app.attack.ui.view

import android.accessibilityservice.AccessibilityServiceInfo
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat
import org.robolectric.Shadows.shadowOf

/** The extra Compose fills, for tests, with the node TalkBack goes to after this one (traversalBefore). */
private const val TRAVERSAL_BEFORE =
    "android.view.accessibility.extra.EXTRA_DATA_TEST_TRAVERSALBEFORE_VAL"

/**
 * Accessibility on as with TalkBack running, so Compose builds the tree TalkBack reads (#255). Call it on the main
 * thread before the screen is composed.
 */
internal fun ComponentActivity.turnOnTalkBack() {
    val manager = shadowOf(getSystemService(AccessibilityManager::class.java))
    manager.setEnabled(true)
    manager.setTouchExplorationEnabled(true)
    manager.setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
}

/** The accessibility tree TalkBack reads, as the window's Compose view gives it now. */
internal class TalkBackTree(private val activity: ComponentActivity) {
    private val provider: AccessibilityNodeProviderCompat
        get() {
            val view = composeViewIn(activity.window.decorView)!!
            return ViewCompat.getAccessibilityDelegate(view)!!.getAccessibilityNodeProvider(view)!!
        }

    /** The node with [id], or null when it is not in the tree. */
    fun node(id: Int): AccessibilityNodeInfoCompat? = provider.createAccessibilityNodeInfo(id)

    fun perform(id: Int, action: Int) = provider.performAction(id, action, null)

    /**
     * TalkBack's order among [ids] (the screen's semantics ids): from the one no other node points to, each node's
     * traversalBefore in turn; a node met twice ends it.
     */
    fun order(ids: Collection<Int>): List<Int> {
        val next = ids.associateWith { id -> node(id)?.extras?.getInt(TRAVERSAL_BEFORE, -1) ?: -1 }
            .filterValues { it != -1 }
        val first = next.keys.firstOrNull { it !in next.values } ?: return emptyList()
        val order = mutableListOf(first)
        while (true) {
            val following = next[order.last()] ?: return order
            order += following
            if (order.count { it == following } > 1) return order
        }
    }

    private fun composeViewIn(view: View): View? = when {
        view.javaClass.simpleName == "AndroidComposeView" -> view
        view is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull {
            composeViewIn(view.getChildAt(it))
        }
        else -> null
    }
}

/** Every semantics node of the screen by id, merged: a chip or button carries the name TalkBack reads for it. */
internal fun ComposeTestRule.mergedNodes(): Map<Int, SemanticsNode> =
    onAllNodes(SemanticsMatcher("any node") { true }).fetchSemanticsNodes().associateBy { it.id }

/** What TalkBack reads first for a node: its description, else its text. */
internal fun SemanticsNode.talkBackName(): String? =
    config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
        ?: config.getOrNull(SemanticsProperties.Text)?.joinToString(" ")?.takeIf { it.isNotBlank() }
        ?: config.getOrNull(SemanticsProperties.EditableText)?.text
