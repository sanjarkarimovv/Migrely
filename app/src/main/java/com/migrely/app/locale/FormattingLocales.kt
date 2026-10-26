package com.migrely.app.locale

import android.content.res.Configuration
import java.util.Locale

/**
 * The formatting locale (BRD D117) of a configuration, and where it is applied, so every feature follows it without
 * choosing a locale itself:
 * - [of] reads it from the phone's configuration as it is now: the formatters `:app/di` provides use it with the
 *   application context, so a new system language is followed at the next call;
 * - [activityOverride] is the change `MainActivity` makes to its own configuration, so Compose's `LocalConfiguration`,
 *   Material's date and time pickers, resources and their plural rules use it;
 * - [applyToProcess] makes it the default locale, for code that formats with the default. `MigrelyApplication` calls
 *   it at start and on every configuration change. Android sets the default back to the phone's own language on
 *   updates it does not report to the application, so the app's own formatters never rely on it.
 *
 * The application context keeps the phone's own configuration, which the CSV export reads for its separator (D72).
 */
class FormattingLocales(
    private val resolver: FormattingLocaleResolver = FormattingLocaleResolver()
) {
    fun of(phoneConfiguration: Configuration): Locale =
        resolver.resolve(phoneConfiguration.locales[0] ?: Locale.getDefault())

    fun activityOverride(phoneConfiguration: Configuration): Configuration =
        Configuration().apply { setLocale(of(phoneConfiguration)) }

    fun applyToProcess(phoneConfiguration: Configuration) {
        Locale.setDefault(of(phoneConfiguration))
    }
}
