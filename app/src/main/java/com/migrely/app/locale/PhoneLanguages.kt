package com.migrely.app.locale

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import java.util.Locale

/**
 * The phone's language list that [FormattingLocales] decides from (BRD D195, EC-L10N-06): on Android 13 and newer,
 * Migrely's per-app language as LocaleManager holds it now, followed by the system's own list, or the system's list
 * alone when Migrely follows the system ([]); below Android 13, with no per-app language, the configuration's list.
 *
 * Not the configuration alone: an activity, and the process, restored after the process died are given the
 * configuration they had then, so a per-app language changed while the process was dead was not applied (T23 8.2,
 * 5570). [appLanguages] gives null below Android 13.
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
        /**
         * LocaleManager's lists on Android 13 and newer; the configuration's list below, and with no LocaleManager, as
         * the CSV's `SystemLocale` does: the service is platform-typed, and a null must not stop the app starting.
         */
        fun of(context: Context): PhoneLanguages =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                fromLocaleManager(context.getSystemService(LocaleManager::class.java))
            } else {
                configurationOnly()
            }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun fromLocaleManager(manager: LocaleManager?) = manager?.let {
            PhoneLanguages(
                appLanguages = { it.applicationLocales },
                systemLanguages = { it.systemLocales }
            )
        } ?: configurationOnly()

        private fun configurationOnly() = PhoneLanguages(
            appLanguages = { null },
            systemLanguages = { LocaleList.getEmptyLocaleList() }
        )
    }
}

private fun LocaleList.toList(): List<Locale> = List(size()) { get(it) }
