package com.migrely.app.medication.ui.view

import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import org.robolectric.Shadows.shadowOf

private const val WAIT_MILLIS = 5_000L

internal const val LOGGED_IBUPROFEN = "Ibuprofen 400 mg logged at"
internal const val STORAGE_FULL_TEXT = "Couldn't save. Your phone's storage is full."
internal const val GENERIC_TEXT = "Something went wrong. Please try again."
internal const val IN_FUTURE_TEXT = "Pick a time up to now."
internal const val INTAKE_UNDONE_TEXT = "Removed. Nothing was logged."
internal const val KEPT_STORAGE_FULL_TEXT = "Still logged. Your phone's storage is full."
internal const val KEPT_GENERIC_TEXT = "Still logged. Something went wrong. Please try again."

/**
 * Log medication as the app shows it, over a [FakeDiary]: the real ViewModel, use cases and mappers, so a tap goes
 * through the same path as on a phone. [post] hands the screen an event the way its notification mapper does.
 */
internal class LogMedicationHarness(
    private val rule: ComposeContentTestRule,
    val diary: FakeDiary
) {
    val dependencies = logMedicationDependencies(diary)
    val ibuprofen: SemanticsMatcher = hasContentDescription("Log $IBUPROFEN_NAME", substring = true)
    val loggedLine: SemanticsMatcher = hasText(LOGGED_IBUPROFEN, substring = true)

    @Composable
    private fun Screen(darkRoom: Boolean, fontScale: Float?) {
        val density = LocalDensity.current
        val scaled = fontScale?.let { Density(density.density, it) } ?: density
        CompositionLocalProvider(LocalDensity provides scaled) {
            MigrelyTheme {
                with(dependencies) {
                    LogMedication(rememberNavController(), attackId = null, darkRoom = darkRoom)
                }
            }
        }
    }

    /** Shows the screen and waits for its first content; [restoration] lets a test recreate it from saved state. */
    fun show(
        darkRoom: Boolean = false,
        restoration: StateRestorationTester? = null,
        firstContent: SemanticsMatcher = ibuprofen,
        fontScale: Float? = null
    ) {
        if (restoration != null) {
            restoration.setContent { Screen(darkRoom, fontScale) }
        } else {
            rule.setContent { Screen(darkRoom, fontScale) }
        }
        awaitNode(firstContent)
    }

    /** `waitUntil` runs the main thread's tasks only when its condition reads them. */
    fun waitFor(condition: () -> Boolean) {
        rule.waitUntil(WAIT_MILLIS) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    fun exists(matcher: SemanticsMatcher) =
        rule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    fun awaitNode(matcher: SemanticsMatcher) = waitFor { exists(matcher) }

    fun awaitText(text: String) = awaitNode(hasText(text))

    fun tapIbuprofen() {
        rule.onNode(ibuprofen).performClick()
    }

    fun logIbuprofen() {
        tapIbuprofen()
        awaitNode(loggedLine)
    }

    /** A tap on Ibuprofen with the disk full: the storage error shows over the list. */
    fun failOnFullStorage() {
        diary.addFailure = AddIntakeResultDomainModel.StorageFull
        tapIbuprofen()
        awaitText(STORAGE_FULL_TEXT)
        diary.addFailure = null
    }

    fun post(event: MedicationEventUiModel) {
        rule.runOnUiThread { dependencies.medicationEventPresenter.show(event) }
    }

    /** The ids of every semantics node with an area on screen, nodes leaving the screen included. */
    fun nodesWithRoom(): Set<Int> {
        val found = mutableSetOf<Int>()
        fun visit(node: SemanticsNode) {
            if (node.boundsInRoot.width > 0f && node.boundsInRoot.height > 0f) found += node.id
            node.children.forEach(::visit)
        }
        visit(rule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        return found
    }
}
