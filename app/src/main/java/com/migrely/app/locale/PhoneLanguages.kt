package com.migrely.app.locale

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * The phone's language list that [FormattingLocales] decides from (BRD D195, EC-L10N-06): on Android 13 and newer,
 * Migrely's per-app language as LocaleManager holds it now, followed by the system's own list; with no per-app language
 * ([]) the system's list alone. Below Android 13 there is no per-app language and the configuration is the list.
 *
 * Not the configuration Android hands an activity: an activity restored after its process died is given the
 * configuration it had then, so a per-app language changed while the process was dead was not applied (T23 8.2, 5570).
 * [appLanguages] gives null below Android 13.
 */
class PhoneLanguages(
    private val appLanguages: () -> LocaleList?,
    private val systemLanguages: () -> LocaleList
) {
    fun of(configuration: Configuration): List<Locale> {
        val app = appLanguages() ?: return configuration.locales.toList()
        return app.toList() + systemLanguages().toList()
    }

    companion object {
        /** LocaleManager's lists on Android 13 and newer; the configuration's list below. */
        fun of(context: Context): PhoneLanguages {
            val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)
            } else {
                null
            }
            return PhoneLanguages(
                appLanguages = { manager?.applicationLocales },
                systemLanguages = { manager?.systemLocales ?: LocaleList.getEmptyLocaleList() }
            )
        }
    }
}

private fun LocaleList.toList(): List<Locale> = List(size()) { get(it) }
