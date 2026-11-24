package com.migrely.app.settings.ui.view

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.model.ExternalAppResultUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** "Share Migrely" and "Rate Migrely on Google Play" leave the app (BRD SET-F2, D185, design D1). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ExternalAppLauncherTest {
    private val resources = RuntimeEnvironment.getApplication().resources
    private val started = mutableListOf<Intent>()
    private val shareText get() = resources.getString(R.string.settings_share_text, SHARE_LINK)
    private val subject get() = resources.getString(R.string.settings_share_subject)
    private val chooserTitle get() = resources.getString(R.string.settings_share_chooser)

    private fun share(start: (Intent) -> Unit = { started += it }) =
        ExternalAppLauncher(start).share(shareText, subject, chooserTitle)

    @Suppress("DEPRECATION")
    private fun Intent.sent(): Intent = getParcelableExtra(Intent.EXTRA_INTENT)!!

    @Test
    fun `W3 - text only, the final sentence and the fixed link, no file or grant`() {
        assertEquals(ExternalAppResultUiModel.Opened, share())

        val chooser = started.single()
        val sent = chooser.sent()
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals(Intent.ACTION_SEND, sent.action)
        assertEquals("text/plain", sent.type)
        assertEquals(
            "Migrely is a private migraine diary for Android. " +
                "No internet permission, no account, no ads.\n" + SHARE_LINK,
            sent.getStringExtra(Intent.EXTRA_TEXT)
        )
        assertFalse(sent.hasExtra(Intent.EXTRA_STREAM))
        listOf(sent, chooser).forEach { intent ->
            val clip = intent.clipData
            if (clip != null) {
                (0 until clip.itemCount).forEach {
                    assertNull("a ClipData URI", clip.getItemAt(it).uri)
                }
            }
            assertEquals(0, intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
            assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    @Test
    fun `The subject and the preview title name Migrely, and the chooser says Share Migrely`() {
        share()

        val chooser = started.single()
        val subject = "Migrely, a migraine diary for Android"
        assertEquals(subject, chooser.sent().getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(subject, chooser.sent().getStringExtra(Intent.EXTRA_TITLE))
        assertEquals("Share Migrely", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE).toString())
    }

    @Test
    fun `Given no app takes the share then nothing could open, and nothing is thrown`() {
        assertEquals(
            ExternalAppResultUiModel.NothingCouldOpen,
            share { throw ActivityNotFoundException() }
        )
    }

    @Test
    fun `Rate opens Migrely's page in the Google Play app first`() {
        val result = ExternalAppLauncher { started += it }.openStorePage()

        assertEquals(ExternalAppResultUiModel.Opened, result)
        val intent = started.single()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("market://details?id=com.migrely.app", intent.dataString)
        assertEquals("com.android.vending", intent.`package`)
    }

    @Test
    fun `Given no Google Play app then the web page opens in a browser`() {
        val result = ExternalAppLauncher {
            if (it.`package` != null) throw ActivityNotFoundException()
            started += it
        }.openStorePage()

        assertEquals(ExternalAppResultUiModel.Opened, result)
        assertEquals(STORE_PAGE_ON_WEB, started.single().dataString)
        assertNull(started.single().`package`)
    }

    @Test
    fun `Given a work profile forbids both, or nothing can open them, then nothing could open`() {
        listOf<() -> Nothing>(
            { throw SecurityException("Blocked by the profile") },
            { throw ActivityNotFoundException() }
        ).forEach { failure ->
            assertEquals(
                ExternalAppResultUiModel.NothingCouldOpen,
                ExternalAppLauncher { failure() }.openStorePage()
            )
        }
    }

    @Test
    fun `The share text says nothing but the approved sentence before the link`() {
        // No outcome promise and nothing on what stays on or leaves the phone beyond "No internet permission".
        assertEquals(
            "Migrely is a private migraine diary for Android. " +
                "No internet permission, no account, no ads.",
            shareText.substringBefore("\n")
        )
    }
}
