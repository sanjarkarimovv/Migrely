package com.migrely.app.test.talkback

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
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat
import kotlin.math.roundToInt
import org.robolectric.Shadows.shadowOf

/** Where Compose puts a merging node's content description: a fake child with this offset on its id. */
private const val CONTENT_DESCRIPTION_FAKE_NODE = 2_000_000_000

/** The extra Compose fills, for tests, with the node TalkBack goes to after this one (traversalBefore). */
private const val TRAVERSAL_BEFORE =
    "android.view.accessibility.extra.EXTRA_DATA_TEST_TRAVERSALBEFORE_VAL"

/**
 * Accessibility on as with TalkBack running, so Compose builds the tree TalkBack reads (#255). Call it on the main
 * thread before the screen is composed.
 */
fun ComponentActivity.turnOnTalkBack() {
    val manager = shadowOf(getSystemService(AccessibilityManager::class.java))
    manager.setEnabled(true)
    manager.setTouchExplorationEnabled(true)
    manager.setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
}

/**
 * Gives the window a bottom navigation bar inset of [heightDp] and no keyboard, as an edge-to-edge window gets on a
 * phone with gesture navigation (#255): a page padded for it then ends above the window's bottom. The window must
 * draw edge to edge. Call it on the main thread.
 */
fun ComponentActivity.setNavigationBar(heightDp: Int) {
    val view = window.decorView
    val bottom = (heightDp * view.resources.displayMetrics.density).roundToInt()
    val insets = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottom))
        .build()
    ViewCompat.dispatchApplyWindowInsets(view, insets)
}

/** The accessibility tree TalkBack reads, as the window's Compose view gives it now. Use it on the main thread. */
class TalkBackTree(private val activity: ComponentActivity) {
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

    /**
     * The words TalkBack says for [node], read from what Android exposes, as TalkBack builds them (#255, design D7):
     * the node's content description (Compose gives only its first entry, on a fake first child when the node merges
     * children), text and state description (not for an edit box: Compose drops it there), then the words of each
     * child that is visible to the user and not a stop of its own. A child cut off by the visible edge is out of the
     * tree and one hidden from accessibility is not visible to the user, so their words are missing, as in TalkBack.
     * Pass a node fetched with `useUnmergedTree = true`.
     */
    fun spokenName(node: SemanticsNode): String = spokenParts(node).joinToString(" ")

    /** [spokenName] in the parts TalkBack joins: [ownWords], the state, then each child's words. */
    fun spokenParts(node: SemanticsNode): List<String> {
        val info = node(node.id) ?: return emptyList()
        val state = info.stateDescription?.toString()?.takeUnless { node.isEditable() }
        return (ownWords(node) + listOfNotNull(state) + childWords(node)).filter { it.isNotBlank() }
    }

    /** [spokenParts] without the state: the control's own words, which it must not say twice (BRD D181 (2)). */
    fun wordParts(node: SemanticsNode): List<String> {
        if (node(node.id) == null) return emptyList()
        return (ownWords(node) + childWords(node)).filter { it.isNotBlank() }
    }

    /** The words of each child that is visible to the user and not a stop of its own. */
    private fun childWords(node: SemanticsNode): List<String> = node.children
        .filter { child -> !child.isOwnStop() && node(child.id)?.isVisibleToUser == true }
        .map(::spokenName)

    /** The words Android exposes on [node] itself: its content description, as Compose hands it over, and its text. */
    fun ownWords(node: SemanticsNode): List<String> {
        val info = node(node.id) ?: return emptyList()
        val fake = node(node.id + CONTENT_DESCRIPTION_FAKE_NODE)?.takeIf {
            it.isVisibleToUser
        }?.contentDescription
        val description = (fake ?: info.contentDescription)?.toString()?.takeIf { it.isNotBlank() }
        val text = info.text?.toString()?.takeIf { it.isNotBlank() }
        // TalkBack says a node's description in place of its text, except on an edit box, where it says both.
        return if (description != null &&
            !node.isEditable()
        ) {
            listOf(description)
        } else {
            listOfNotNull(description, text)
        }
    }

    private fun SemanticsNode.isEditable() = config.contains(SemanticsProperties.EditableText)

    private fun SemanticsNode.isOwnStop() = config.isMergingSemanticsOfDescendants

    private fun composeViewIn(view: View): View? = when {
        view.javaClass.simpleName == "AndroidComposeView" -> view
        view is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull {
            composeViewIn(view.getChildAt(it))
        }
        else -> null
    }
}

/** Every semantics node of the screen by id, merged: a chip or button carries the words of all its children. */
fun ComposeTestRule.mergedNodes(): Map<Int, SemanticsNode> =
    onAllNodes(SemanticsMatcher("any node") { true }).fetchSemanticsNodes().associateBy { it.id }

/** Every semantics node of the screen by id, unmerged, as [TalkBackTree.spokenName] reads them. */
fun ComposeTestRule.unmergedNodes(): Map<Int, SemanticsNode> =
    onAllNodes(SemanticsMatcher("any node") { true }, useUnmergedTree = true).fetchSemanticsNodes()
        .associateBy { it.id }

/**
 * The words a merged node carries: its description, else its text. It finds the items TalkBack stops on and labels
 * them in a failure; it is never a check of what TalkBack says, because merged semantics keep a child's words even
 * when the child is cut off and out of the tree (use [TalkBackTree.spokenName] for that).
 */
fun SemanticsNode.mergedWords(): String? =
    config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
        ?: config.getOrNull(SemanticsProperties.Text)?.joinToString(" ")?.takeIf { it.isNotBlank() }
        ?: config.getOrNull(SemanticsProperties.EditableText)?.text
