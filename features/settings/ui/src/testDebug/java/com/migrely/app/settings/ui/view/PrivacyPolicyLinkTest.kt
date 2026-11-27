package com.migrely.app.settings.ui.view

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val LINK_WORDS = "Read the full privacy policy"
private const val POLICY_URL = "https://migrely.health/privacy/"

/**
 * The Privacy page's policy link opens the browser in its own task, so a browser that crashes at
 * launch never takes Migrely's task down (#280, EC-SHR-14, design D10). Where nothing can open
 * it, nothing shows and nothing is thrown: the address is written under the link.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PrivacyPolicyLinkTest {
    @get:Rule
    val rule = createComposeRule()

    private val application get() = RuntimeEnvironment.getApplication()

    private fun showPrivacy() = rule.setContent { MigrelyTheme { PrivacyContents(onBack = {}) } }

    private fun showPrivacyStartingWith(start: (Intent) -> Unit) = rule.setContent {
        val base = LocalContext.current
        val context = object : ContextWrapper(base) {
            override fun startActivity(intent: Intent) = start(intent)
        }
        CompositionLocalProvider(LocalContext provides context as Context) {
            MigrelyTheme { PrivacyContents(onBack = {}) }
        }
    }

    private fun clickLink() {
        rule.onNode(
            hasContentDescription(LINK_WORDS) and
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        )
            .performScrollTo()
            .performClick()
        rule.waitForIdle()
    }

    @Test
    fun `The policy opens in the browser in its own task`() {
        showPrivacy()

        clickLink()

        val started = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(POLICY_URL, started.dataString)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, started.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `Given no browser then nothing opens and nothing is thrown`() {
        // Robolectric then throws ActivityNotFoundException for an intent nothing resolves.
        shadowOf(application).checkActivities(true)
        showPrivacy()

        clickLink()

        assertNull(shadowOf(application).nextStartedActivity)
        rule.onNode(hasContentDescription(LINK_WORDS)).assertExists()
    }

    @Test
    fun `Given a work profile forbids the browser then nothing is thrown`() {
        val tries = mutableListOf<Intent>()
        showPrivacyStartingWith {
            tries += it
            throw SecurityException("Blocked by the profile")
        }

        clickLink()

        assertEquals(POLICY_URL, tries.single().dataString)
        rule.onNode(hasContentDescription(LINK_WORDS)).assertExists()
    }
}
