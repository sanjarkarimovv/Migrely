package com.migrely.app.locale

import java.util.Locale

/**
 * Recreates an activity whose language is no longer the app's (BRD D195, EC-L10N-06). After a process-death restore
 * the platform can keep the per-app language it held before the death, so a change back to that language reaches the
 * app with no configuration change, and no relaunch (T23 8.2 row 9d on API 36). On start the activity asks [current]
 * again and, when it differs from the locale it was built with, tells [languageChanged] and [recreate]s, which keeps
 * its saved state as a rotation does. Never while the activity is finishing or already changing its configuration.
 *
 * At most once per language, carried across instances in the saved state ([saved], [restore]): [current] and the
 * locale the activity was built with come from one source on Android 13 and newer, but with no LocaleManager each
 * reads a configuration, the activity's and the application's, which can stay apart, so every new instance would
 * otherwise recreate once, forever (cr-de on 9902f069). The mark lasts only while the mismatch does: an instance that
 * starts in the app's language clears it, so after a later process death a stale restored activity is recreated
 * again (T23 8.2 row 9e). [current] does not change the formatters' locale; the recreated activity refreshes it as it
 * is attached, so a recreate held back never leaves words in one language and dates in another. Returns whether it
 * recreated.
 */
class StaleLocaleRecreation(
    private val builtWith: Locale,
    private val current: () -> Locale,
    private val recreate: () -> Unit,
    private val languageChanged: () -> Unit
) {
    private var recreatedFor: Locale? = null

    /** The language an earlier instance was recreated for, from [saved]. */
    fun restore(savedRecreatedFor: String?) {
        recreatedFor = savedRecreatedFor?.let(Locale::forLanguageTag)
    }

    fun saved(): String? = recreatedFor?.toLanguageTag()

    fun onStart(finishing: Boolean, changingConfigurations: Boolean): Boolean {
        if (finishing || changingConfigurations) return false
        val now = current()
        if (now == builtWith) {
            // In the app's language: an earlier recreate has done its work, and must not hold back a later one.
            recreatedFor = null
            return false
        }
        if (now == recreatedFor) return false
        recreatedFor = now
        languageChanged()
        recreate()
        return true
    }
}
