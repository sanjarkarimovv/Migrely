package com.migrely.app.locale

import java.util.Locale

/**
 * Recreates an activity whose language is no longer the app's (BRD D195, EC-L10N-06). After a process-death restore
 * the platform can keep the per-app language it held before the death, so a change back to that language reaches the
 * app with no configuration change, and no relaunch (T23 8.2 row 9d on API 36). On start the activity asks [current]
 * again and, when it differs from the locale it was built with, [recreate]s, which keeps its saved state as a rotation
 * does. At most once per activity: a platform that kept handing back a stale configuration cannot make it loop. Never
 * while the activity is finishing or already changing its configuration. Returns whether it recreated.
 */
class StaleLocaleRecreation(
    private val builtWith: Locale,
    private val current: () -> Locale,
    private val recreate: () -> Unit
) {
    private var recreated = false

    fun onStart(finishing: Boolean, changingConfigurations: Boolean): Boolean {
        if (recreated || finishing || changingConfigurations || current() == builtWith) return false
        recreated = true
        recreate()
        return true
    }
}
