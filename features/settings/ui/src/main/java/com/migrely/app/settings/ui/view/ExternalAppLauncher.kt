package com.migrely.app.settings.ui.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.migrely.app.settings.ui.model.ExternalAppResultUiModel

/**
 * The "In-app Settings, Share Migrely" link of `docs/release/play-links.md` (BRD D184), byte for byte. It always names
 * `com.migrely.app`, never the running package, so a debug build shares the real listing (EC-SHR-09). A change to the
 * table comes to the app in a change of its own (design D6).
 */
internal const val SHARE_LINK =
    "https://play.google.com/store/apps/details?id=com.migrely.app" +
        "&utm_source=app&utm_medium=share&utm_campaign=settings_share" +
        "&referrer=utm_source%3Dapp%26utm_medium%3Dshare%26utm_campaign%3Dsettings_share"

/** Migrely's page in the Google Play app, then in a browser (BRD SET-F2, EC-SHR-01). */
internal const val STORE_PAGE_IN_PLAY = "market://details?id=com.migrely.app"
internal const val STORE_PAGE_ON_WEB =
    "https://play.google.com/store/apps/details?id=com.migrely.app"
internal const val PLAY_STORE_PACKAGE = "com.android.vending"

/**
 * Opens what lies outside Migrely from Settings: "Share and rate" (BRD SET-F2, D185, design D1) and the privacy
 * policy (D119, design D10), through [start], which is the activity's `startActivity`. It uses no `resolveActivity` and
 * adds no `<queries>`: an implicit intent needs no package visibility, and either would change the manifest. An app
 * that cannot take it, or a work profile that forbids it (`SecurityException`), counts as nothing opening, so the screen
 * can say so (EC-SHR-01, EC-SHR-02).
 *
 * The other app runs in its own task (`FLAG_ACTIVITY_NEW_TASK`), so on API 28 and later its crash at launch never takes
 * Migrely's task, and the person's place in it, down (EC-SHR-14); API 26 and 27 keep every full-screen task in one
 * stack, a platform limit (EC-SHR-17). Only `NEW_TASK`: the page may join Play's or the browser's open task, and Back
 * then shows that app's earlier page first (EC-SHR-15, design D6).
 */
internal class ExternalAppLauncher(private val start: (Intent) -> Unit) {
    /** Google Play's page in the Play app, else the web page in a browser. Never the in-app review flow. */
    fun openStorePage(): ExternalAppResultUiModel =
        result(tryStart(storePageInPlay()) || tryStart(storePageOnWeb()))

    /** [url] in a browser. */
    fun openWebPage(url: String): ExternalAppResultUiModel = result(tryStart(webPage(url)))

    /** The system share sheet with [text] only: no file, stream or URI grant (medical-reviewer W3). */
    fun share(text: String, subject: String, chooserTitle: String): ExternalAppResultUiModel =
        result(tryStart(shareChooser(text, subject, chooserTitle)))

    fun storePageInPlay(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(STORE_PAGE_IN_PLAY))
        .setPackage(PLAY_STORE_PACKAGE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun storePageOnWeb(): Intent = webPage(STORE_PAGE_ON_WEB)

    private fun webPage(url: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * The flag goes on the `ACTION_SEND` intent, which the chooser starts the chosen app with, and never on the chooser,
     * so the sheet stays over Settings in Migrely's task. AOSP's chooser already starts a send target in a task of its
     * own (it adds `NEW_DOCUMENT` and `MULTIPLE_TASK`), so the flag is only a guard against a chooser that does not
     * (design D5).
     */
    fun shareChooser(text: String, subject: String, chooserTitle: String): Intent {
        val send = Intent(Intent.ACTION_SEND)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
            // Email apps take the subject; Android 10 and later show the title above the preview.
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TITLE, subject)
        return Intent.createChooser(send, chooserTitle)
    }

    private fun tryStart(intent: Intent): Boolean = try {
        start(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

    private fun result(opened: Boolean) = if (opened) {
        ExternalAppResultUiModel.Opened
    } else {
        ExternalAppResultUiModel.NothingCouldOpen
    }
}
