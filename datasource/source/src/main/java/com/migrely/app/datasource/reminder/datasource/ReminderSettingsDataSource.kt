package com.migrely.app.datasource.reminder.datasource

import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import java.time.Instant
import java.time.LocalDate

/** The reminder settings (BRD D68). Every call throws a `DataException` when storage fails. */
interface ReminderSettingsDataSource {
    /** The saved settings, or the defaults before anything is saved: daily off at 20:00, the medication question on. */
    fun settings(): ReminderSettingsDataModel

    fun setDaily(enabled: Boolean, minuteOfDay: Int)

    fun setEffect(enabled: Boolean)

    fun setPermissionAsked(at: Instant)

    /** Records that the daily reminder showed on [date], so it never shows twice in one day. */
    fun setDailyShown(date: LocalDate)

    /** The visit on [visit] needs no notification any more (BRD D168). */
    fun setVisitReminded(visit: LocalDate)

    /** The home card of the visit on [visit] was tapped or closed. */
    fun setVisitCardClosed(visit: LocalDate)

    /** The visit reminder waits for 10:00 on [day] after an attack (BRD EC-REM-12). */
    fun setVisitPostponed(day: LocalDate)
}
