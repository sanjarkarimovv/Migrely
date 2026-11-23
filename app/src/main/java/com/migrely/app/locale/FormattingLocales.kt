package com.migrely.app.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * The app's language and formatting locale (BRD D117, D195) of a configuration, and where they are applied, so every
 * feature follows them without choosing a locale itself:
 * - [of] reads them from the configuration as it is now, the phone's language list with Android 13's per-app language
 *   first: the formatters `:app/di` provides use it with the application context, so a new language is followed at
 *   the next call;
 * - [activityOverride] is the change `MainActivity` makes to its own configuration, so Compose's `LocalConfiguration`,
 *   Material's date and time pickers, resources and their plural rules use it. For the activity it reads the language
 *   list from [PhoneLanguages], as a restored activity's configuration can predate a per-app language change;
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
    private val resolver: FormattingLocaleResolver = FormattingLocaleResolver()
) {
    fun of(phoneConfiguration: Configuration): Locale = resolver.resolve(
        List(phoneConfiguration.locales.size()) { phoneConfiguration.locales[it] }
    )

    fun activityOverride(phoneConfiguration: Configuration): Configuration =
        withLocale(of(phoneConfiguration))

    /**
     * [activityOverride] for an activity's [base] context, from [PhoneLanguages]: the per-app language as LocaleManager
     * holds it now, not as the configuration Android restored the activity with says (EC-L10N-06).
     */
    fun activityOverride(base: Context): Configuration =
        withLocale(resolver.resolve(PhoneLanguages.of(base).of(base.resources.configuration)))

    private fun withLocale(locale: Locale) = Configuration().apply { setLocale(locale) }

    fun localizedContext(context: Context): Context =
        context.createConfigurationContext(activityOverride(context.resources.configuration))

    fun applyToProcess(phoneConfiguration: Configuration) {
        Locale.setDefault(of(phoneConfiguration))
    }
}
