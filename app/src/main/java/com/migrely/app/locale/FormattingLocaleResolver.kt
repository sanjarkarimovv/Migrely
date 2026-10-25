package com.migrely.app.locale

import java.time.temporal.WeekFields
import java.util.Locale

private const val FIRST_DAY_KEYWORD = "fw"
private const val DAY_KEYWORD_LENGTH = 3

/**
 * The locale every date and time is formatted in (BRD D117). While Migrely's words are English only (EC-L10N-01), the
 * names a date or time produces, months, weekdays and AM or PM, are English too, and only the region's conventions come
 * from the phone:
 * - a phone in English keeps its own locale;
 * - any other language becomes English with the phone's country, so ru-RU gives en-RU and uz-UZ gives en-UZ;
 * - with no country, plain English.
 *
 * The week starts where the phone's does: when English with that country would start it elsewhere, as plain English
 * does for a Russian phone with no country, or when the user chose another first day in Android's regional
 * preferences, the phone's first day is carried along as the Unicode `fw` keyword. The 12- or 24-hour setting is the
 * system's own and is read where a time is formatted.
 */
class FormattingLocaleResolver {
    fun resolve(phoneLocale: Locale): Locale {
        if (phoneLocale.language == Locale.ENGLISH.language) return phoneLocale
        val english = if (phoneLocale.country.isEmpty()) {
            Locale.ENGLISH
        } else {
            Locale(Locale.ENGLISH.language, phoneLocale.country)
        }
        val phoneFirstDay = WeekFields.of(phoneLocale).firstDayOfWeek
        return if (WeekFields.of(english).firstDayOfWeek == phoneFirstDay) {
            english
        } else {
            Locale.Builder()
                .setLocale(english)
                .setUnicodeLocaleKeyword(
                    FIRST_DAY_KEYWORD,
                    phoneFirstDay.name.take(DAY_KEYWORD_LENGTH).lowercase(Locale.ROOT)
                )
                .build()
        }
    }
}
