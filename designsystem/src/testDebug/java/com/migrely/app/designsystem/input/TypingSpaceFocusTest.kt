package com.migrely.app.designsystem.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STATUS_PX = 63

// Leaves the field's input line no room under the bar, so the bar steps aside.
private const val KEYBOARD_PX = 800

private const val BACK = "Back"
private const val ABOVE = "Above"
private const val FIELD = "Name"

/**
 * While the top bar is aside, keyboard focus never lands on it (BRD D192, EC-UX-76; ux R11; task 2.3): Shift+Tab from
 * the field reaches the content above it and goes round the page, never to Back; once the keyboard is gone, Back is a
 * stop again. Legacy graphics, where a Robolectric window can take focus; the field's line comes from its text style.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w800dp-h360dp-land-420dpi", fontScale = 2f)
class TypingSpaceFocusTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var text by mutableStateOf("")
    private var backFocused = false

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                val space = rememberTypingSpace()
                val pageScroll = rememberScrollState()
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Title") },
                        navigationIcon = {
                            // Seen by focus itself: a bar aside has no semantics to name the stop.
                            IconButton(
                                onClick = {},
                                modifier = Modifier.onFocusChanged {
                                    if (it.isFocused) {
                                        backFocused =
                                            true
                                    }
                                }
                            ) { Icon(MigrelyIcons.Back, contentDescription = BACK) }
                        },
                        windowInsets = MigrelyInsets.bars.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        ),
                        modifier = space.barModifier
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom)
                            )
                            .then(space.page(pageScroll))
                            .verticalScroll(pageScroll)
                    ) {
                        TextButton(onClick = {}) { Text(ABOVE) }
                        ProvideTypingSpace(space) {
                            SingleLineFieldInView(hasLabel = true) { inView, interactionSource ->
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    singleLine = true,
                                    label = { Text(FIELD) },
                                    interactionSource = interactionSource,
                                    modifier = inView.fillMaxWidth()
                                )
                            }
                        }
                        Spacer(Modifier.height(400.dp))
                    }
                }
            }
        }
        rule.waitForIdle()
        setKeyboard(0)
        rule.onNode(hasSetTextAction()).requestFocus()
        rule.waitForIdle()
    }

    private fun setKeyboard(px: Int) {
        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = px, statusPx = STATUS_PX) }
        rule.waitForIdle()
    }

    private fun focusedName(): String? {
        val node = rule.onAllNodes(isFocused()).fetchSemanticsNodes().firstOrNull() ?: return null
        return node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
            ?: node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
            ?: node.config.getOrNull(SemanticsProperties.EditableText)?.let { FIELD }
    }

    private fun shiftTabs(count: Int): List<String?> = (1..count).map {
        rule.onRoot().performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) } }
        rule.waitForIdle()
        focusedName()
    }

    @Test
    fun `While aside Shift+Tab goes round the page and never lands on Back`() {
        show()
        setKeyboard(KEYBOARD_PX)

        backFocused = false
        val stops = shiftTabs(STOPS)

        assertFalse("Shift+Tab reached Back: $stops", backFocused)
        assertEquals("Shift+Tab's stops", listOf(ABOVE, FIELD, ABOVE, FIELD), stops)
    }

    @Test
    fun `With the keyboard gone Back is a stop again`() {
        show()
        setKeyboard(KEYBOARD_PX)
        setKeyboard(0)

        val stops = shiftTabs(2)

        assertEquals("Shift+Tab's stops: $stops", listOf(ABOVE, BACK), stops)
    }

    private companion object {
        // Above the field, then round the page, twice.
        const val STOPS = 4
    }
}
