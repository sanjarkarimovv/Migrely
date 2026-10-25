package com.migrely.app.locale

import android.content.res.Configuration
import java.util.Locale

/**
 * Where the formatting locale (BRD D117) is applied, so every feature follows it without choosing a locale itself:
 * - [applyToProcess] makes it the default locale, which every formatter that is not handed one reads, the app's
 *   formatters provided in `:app/di`, notifications and work done with the application context included.
 *   `MigrelyApplication` calls it at start and on every configuration change, since the system language can change
 *   while the app runs;
 * - [activityOverride] is the change `MainActivity` makes to its own configuration, so Compose's
 *   `LocalConfiguration`, Material's date and time pickers, resources and their plural rules use it too.
 *
 * The application context keeps the phone's own configuration, which the CSV export reads for its separator (D72).
 */
class FormattingLocaleApplier(
    private val resolver: FormattingLocaleResolver = FormattingLocaleResolver()
) {
    fun applyToProcess(phoneConfiguration: Configuration) {
        Locale.setDefault(formattingLocale(phoneConfiguration))
    }

    fun activityOverride(phoneConfiguration: Configuration): Configuration =
        Configuration().apply { setLocale(formattingLocale(phoneConfiguration)) }

    private fun formattingLocale(phoneConfiguration: Configuration): Locale =
        resolver.resolve(phoneConfiguration.locales[0] ?: Locale.getDefault())
}
