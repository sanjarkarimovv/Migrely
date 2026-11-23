package com.migrely.app.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * The app's language and formatting locale (BRD D117, D195) of a context, and where they are applied, so every
 * feature follows them without choosing a locale itself:
 * - [of] gives them as [refresh] last resolved them, resolving once on the first call. Every formatted date and time
 *   asks, in composition on the main thread, and on Android 13 and newer the phone's language list is two binder calls
 *   to LocaleManager, so it is not read per string (T23 review of 2d99dabd);
 * - [refresh] resolves them again from [PhoneLanguages], the phone's language list with Android 13's per-app language
 *   first, from LocaleManager rather than a configuration that can predate it (EC-L10N-06). The app refreshes at
 *   start, on every configuration change, which a new system or per-app language is, and as `MainActivity` is
 *   created, so the formatters `:app/di` provides follow a new language from then on;
 * - [activityOverride] is the change `MainActivity` makes to its own configuration, so Compose's `LocalConfiguration`,
 *   Material's date and time pickers, resources and their plural rules use it;
 * - [localizedContext] is the same override for strings read outside an activity, such as notifications and their
 *   channel names (design D2): the application context keeps the phone's own list, from which Android's resource
 *   matching could pick German while the screens are English;
 * - [applyToProcess] makes it the default locale, for code that formats with the default. `MigrelyApplication` calls
 *   it at start and on every configuration change. Android sets the default back to the phone's own language on
 *   updates it does not report to the application, so the app's own formatters never rely on it.
 *
 * The CSV export reads its separator from the system's language instead, not from Migrely's (D72, D195).
 */
class FormattingLocales(
    private val resolver: FormattingLocaleResolver = FormattingLocaleResolver(),
    private val phoneLanguages: (Context) -> PhoneLanguages = PhoneLanguages::of
) {
    // Written on the main thread, read by the formatters on any thread.
    @Volatile
    private var current: Locale? = null

    fun of(context: Context): Locale = current ?: refresh(context)

    fun refresh(context: Context): Locale =
        resolver.resolve(phoneLanguages(context).of(context.resources.configuration))
            .also { current = it }

    fun activityOverride(base: Context): Configuration =
        Configuration().apply { setLocale(of(base)) }

    fun localizedContext(context: Context): Context =
        context.createConfigurationContext(activityOverride(context))

    fun applyToProcess(context: Context) {
        Locale.setDefault(of(context))
    }
}
