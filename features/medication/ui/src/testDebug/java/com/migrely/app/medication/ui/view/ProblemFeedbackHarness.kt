package com.migrely.app.medication.ui.view

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.robolectric.Shadows.shadowOf

private const val WAIT_MILLIS = 5_000L

internal const val ENTER_A_NAME = "Enter a name."
internal const val NAME_LABEL = "Name"
internal const val DOSE_LABEL = "Usual dose (optional)"

/** Longer than the time the announcement stays in the tree (BRD D191). */
internal const val PAST_ANNOUNCEMENT_MILLIS = 6_000L

/** The polite announcement of a field problem: its own node, outside the field (BRD D191, ux F5). */
internal fun announcement(text: String): SemanticsMatcher =
    SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite) and
        SemanticsMatcher("says \"$text\"") { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(text) == true ||
                node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == text } == true
        }

internal val nameField: SemanticsMatcher = hasSetTextAction() and hasText(NAME_LABEL)
internal val doseField: SemanticsMatcher = hasSetTextAction() and hasText(DOSE_LABEL)

private typealias ActivityComposeRule =
    AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

/**
 * The medication forms as the app shows them, edge to edge so the keyboard's inset reaches them, over the real
 * ViewModels and use cases (BRD D191).
 */
internal class ProblemFeedbackHarness(private val rule: ActivityComposeRule, val diary: FakeDiary) {
    val edit = editMedicationDependencies(diary)
    val log = logMedicationDependencies(diary)

    /** Touch unless a test uses a hardware keyboard: Robolectric's view never stays in touch mode by itself. */
    var touch = true

    @Composable
    private fun Frame(fontScale: Float?, screen: @Composable () -> Unit) {
        val density = LocalDensity.current
        val scaled = fontScale?.let { Density(density.density, it) } ?: density
        val inputMode = if (touch) TouchOnlyInputMode else LocalInputModeManager.current
        CompositionLocalProvider(
            LocalDensity provides scaled,
            LocalInputModeManager provides inputMode
        ) { MigrelyTheme { screen() } }
    }

    private fun content(
        fontScale: Float?,
        restoration: StateRestorationTester?,
        screen: @Composable () -> Unit
    ) {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        if (restoration != null) {
            restoration.setContent { Frame(fontScale, screen) }
        } else {
            rule.setContent { Frame(fontScale, screen) }
        }
    }

    /** Edit medication for [medicationId], or Add with null. */
    fun showEdit(
        medicationId: String? = null,
        fontScale: Float? = null,
        restoration: StateRestorationTester? = null
    ) {
        content(fontScale, restoration) {
            with(edit) { EditMedication(rememberNavController(), medicationId) }
        }
        awaitNode(hasText("Save"))
    }

    /** Log medication's first-use form (the diary has no medication). */
    fun showFirstUse(
        darkRoom: Boolean = false,
        fontScale: Float? = null,
        restoration: StateRestorationTester? = null
    ) {
        content(fontScale, restoration) {
            with(log) {
                LogMedication(rememberNavController(), attackId = null, darkRoom = darkRoom)
            }
        }
        awaitNode(hasText("Add and log"))
    }

    fun keyboard(heightDp: Int) {
        rule.runOnUiThread { rule.activity.setKeyboard(heightDp) }
        rule.waitForIdle()
    }

    /** `waitUntil` runs the main thread's tasks only when its condition reads them. */
    fun waitFor(condition: () -> Boolean) {
        rule.waitUntil(WAIT_MILLIS) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    fun exists(matcher: SemanticsMatcher, unmerged: Boolean = false) =
        rule.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty()

    fun awaitNode(matcher: SemanticsMatcher) = waitFor { exists(matcher) }

    /** Waits for the field problem [text] to show under its field. */
    fun awaitProblem(text: String) = awaitNode(hasText(text))

    fun focusedNodes(): List<SemanticsNode> = rule.onAllNodes(isFocused()).fetchSemanticsNodes()

    /** What the focused nodes say first, to name them in a failure. */
    fun focusedLabels(): List<String> = focusedNodes().map { node ->
        node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
            ?: node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
            ?: "#${node.id}"
    }

    /** Every node, the leaving ones and the merged-away ones included. */
    fun allNodes(): List<SemanticsNode> {
        val found = mutableListOf<SemanticsNode>()
        fun visit(node: SemanticsNode) {
            found += node
            node.children.forEach(::visit)
        }
        visit(rule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        return found
    }

    /** Whether [field]'s whole region lies inside the visible part of the page's scrolling area. */
    fun isInView(field: SemanticsNodeInteraction): Boolean {
        val visible = rule.onNode(hasScrollAction()).getBoundsInRoot()
        val region = field.getUnclippedBoundsInRoot()
        return region.inside(visible)
    }
}

/** An input mode that stays Touch, as on a phone used by touch or TalkBack. */
internal object TouchOnlyInputMode : InputModeManager {
    override val inputMode = InputMode.Touch

    @ExperimentalComposeUiApi
    override fun requestInputMode(inputMode: InputMode) = false
}

internal fun DpRect.inside(outer: DpRect) =
    left >= outer.left && top >= outer.top && right <= outer.right && bottom <= outer.bottom
