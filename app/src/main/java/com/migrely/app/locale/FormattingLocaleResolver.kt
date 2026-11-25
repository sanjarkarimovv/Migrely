package com.migrely.app.locale

import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

private const val FIRST_DAY_KEYWORD = "fw"
private const val DAY_KEYWORD_LENGTH = 3
private const val GERMAN = "de"

/** The languages Migrely's words are written in (BRD D195); `res/xml/locales_config.xml` lists the same. */
val APP_LANGUAGES = setOf(Locale.ENGLISH.language, GERMAN)

/**
 * The app's language and the locale every date and time is formatted in (BRD D117, D195), from the phone's language
 * list, in which Android 13's per-app language comes first:
 * - the first language of the list that Migrely speaks ([APP_LANGUAGES]) decides;
 * - when it is German, the formatting locale is that German locale, so de-DE, de-AT and de-CH keep their own names;
 * - otherwise the D117 rule applies to the first language of the list, unchanged: a phone in English keeps its own
 *   locale, any other language becomes English with the phone's country (ru-RU gives en-RU), and with no country plain
 *   English. So a list with English ahead of German, such as ru-RU, en-US, de-DE, stays en-RU.
 *
 * The week starts where the phone's does: when the resolved locale would start it elsewhere, as plain English does for
 * a Russian phone with no country, or when the user chose another first day in Android's regional preferences, the
 * first language's first day is carried along as the Unicode `fw` keyword. The 12- or 24-hour setting is the system's
 * own and is read where a time is formatted.
 */
class FormattingLocaleResolver {
    fun resolve(phoneLocales: List<Locale>): Locale {
        val locales = phoneLocales.ifEmpty { listOf(Locale.getDefault()) }
        val phoneLocale = locales.first()
        val firstDay = WeekFields.of(phoneLocale).firstDayOfWeek
        val german = locales.firstOrNull { it.language in APP_LANGUAGES }
            ?.takeIf { it.language == GERMAN }
        return when {
            german != null -> withFirstDay(german, firstDay)
            phoneLocale.language == Locale.ENGLISH.language -> phoneLocale
            phoneLocale.country.isEmpty() -> withFirstDay(Locale.ENGLISH, firstDay)
            else -> withFirstDay(Locale(Locale.ENGLISH.language, phoneLocale.country), firstDay)
        }
    }

    private fun withFirstDay(locale: Locale, firstDay: DayOfWeek): Locale =
        if (WeekFields.of(locale).firstDayOfWeek == firstDay) {
            locale
        } else {
            Locale.Builder()
                .setLocale(locale)
                .setUnicodeLocaleKeyword(
                    FIRST_DAY_KEYWORD,
                    firstDay.name.take(DAY_KEYWORD_LENGTH).lowercase(Locale.ROOT)
                )
                .build()
        }
}
