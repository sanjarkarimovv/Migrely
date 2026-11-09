package com.migrely.app.datasource.notification.datasource

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.migrely.app.MainActivity
import com.migrely.app.R
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.designsystem.format.MigrelyDateFormat
import com.migrely.app.reminder.ReminderActionReceiver
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * Migrely's notifications (BRD D66, D67, D168), in three channels so the user can silence one kind in system settings. Nothing
 * is posted while notifications are off, so Android 13's permission is never needed at the moment of posting. Every
 * one is private, and the lock screen that hides sensitive content shows only "Reminder" (BRD D169).
 */
class NotificationDataSourceImpl(private val context: Context) : NotificationDataSource {
    private val manager = NotificationManagerCompat.from(context)

    override fun areEnabled(): Boolean = manager.areNotificationsEnabled()

    override fun showDailyReminder(date: LocalDate) = notify(
        DAILY_ID,
        builder(CHANNEL_DAILY, R.string.notification_channel_daily)
            .setContentTitle(context.getString(R.string.notification_daily_title))
            .setContentText(context.getString(R.string.notification_daily_text))
            .addAction(
                0,
                context.getString(R.string.notification_daily_pain_free),
                PendingIntent.getBroadcast(
                    context,
                    DAILY_ID,
                    Intent(context, ReminderActionReceiver::class.java)
                        .setAction(ReminderActionReceiver.ACTION_PAIN_FREE)
                        .putExtra(ReminderActionReceiver.EXTRA_DATE, date.toString()),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                0,
                context.getString(R.string.notification_daily_log),
                openApp(dismissDaily = true)
            )
    )

    override fun showAttackLoggedToday() = notify(
        DAILY_ID,
        builder(CHANNEL_DAILY, R.string.notification_channel_daily)
            .setContentTitle(context.getString(R.string.notification_attack_logged))
            .setOnlyAlertOnce(true)
    )

    override fun cancelDailyReminder() = manager.cancel(DAILY_ID)

    override fun showEffectQuestion(intakeId: String) = notify(
        effectId(intakeId),
        builder(CHANNEL_EFFECT, R.string.notification_channel_effect)
            .setContentTitle(context.getString(R.string.notification_effect_title))
            .setContentText(context.getString(R.string.notification_effect_text))
    )

    override fun cancelEffectQuestion(intakeId: String) = manager.cancel(effectId(intakeId))

    // One tap opens the report through History's gate, so no action button (design D5, D6). It names the date, never a
    // count of days, and goes by itself when the visit day ends, so a late one is still true (EC-REM-14, EC-REM-15).
    override fun showVisitReminder(visit: LocalDate, until: Instant) = notify(
        VISIT_ID,
        builder(CHANNEL_VISIT, R.string.notification_channel_visit)
            .setContentTitle(
                context.getString(
                    R.string.notification_visit_title,
                    MigrelyDateFormat.short(
                        visit,
                        LocalDate.now(),
                        context.resources.configuration.locales[0]
                    )
                )
            )
            .setContentText(context.getString(R.string.notification_visit_text))
            .setContentIntent(openDoctorReport())
            .setTimeoutAfter(Duration.between(Instant.now(), until).toMillis().coerceAtLeast(1L))
            .setOnlyAlertOnce(true)
    )

    override fun cancelVisitReminder() = manager.cancel(VISIT_ID)

    // The channel is made first, so Android's settings list it from the first time the Reminders screen asks.
    override fun isVisitChannelOff(): Boolean {
        createChannel(CHANNEL_VISIT, R.string.notification_channel_visit)
        return manager.getNotificationChannelCompat(CHANNEL_VISIT)?.importance ==
            NotificationManagerCompat.IMPORTANCE_NONE
    }

    override fun cancelAll() = manager.cancelAll()

    private fun createChannel(channelId: String, channelName: Int) {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                channelId,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                .setName(context.getString(channelName))
                .apply {
                    if (channelId == CHANNEL_VISIT) {
                        setDescription(
                            context.getString(R.string.notification_channel_visit_description)
                        )
                    }
                }
                .build()
        )
    }

    private fun builder(channelId: String, channelName: Int): NotificationCompat.Builder {
        createChannel(channelId, channelName)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openApp())
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion(channelId))
    }

    // One version for every channel: no text, no action, no channel name (BRD D169, EC-REM-21).
    private fun publicVersion(channelId: String) = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.notification_public_title))
        .build()

    // An action button does not clear its notification, so "Log a migraine" asks the app to (its own request code).
    private fun openApp(dismissDaily: Boolean = false): PendingIntent = PendingIntent.getActivity(
        context,
        if (dismissDaily) DAILY_ID else 0,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_DISMISS_DAILY, dismissDaily),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun openDoctorReport(): PendingIntent = PendingIntent.getActivity(
        context,
        VISIT_ID,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_OPEN_DOCTOR_REPORT, true),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Checked by areEnabled() first, which on Android 13 and later also means the permission is granted.
    @SuppressLint("MissingPermission")
    private fun notify(id: Int, builder: NotificationCompat.Builder) {
        if (areEnabled()) manager.notify(id, builder.build())
    }

    private fun effectId(intakeId: String) = EFFECT_ID_BASE + intakeId.hashCode()

    companion object {
        /** Set on the intent of "Log a migraine": the app clears the daily reminder when it opens. */
        const val EXTRA_DISMISS_DAILY = "com.migrely.app.DISMISS_DAILY_REMINDER"
        const val DAILY_ID = 1

        /** Set on the visit reminder's intent: the app opens the doctor report through History (design D6). */
        const val EXTRA_OPEN_DOCTOR_REPORT = "com.migrely.app.OPEN_DOCTOR_REPORT"
        private const val VISIT_ID = 2
        private const val CHANNEL_VISIT = "visit"
        private const val CHANNEL_DAILY = "daily"
        private const val CHANNEL_EFFECT = "effect"
        private const val EFFECT_ID_BASE = 1000
    }
}
