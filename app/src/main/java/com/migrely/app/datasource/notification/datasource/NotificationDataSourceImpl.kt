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
import com.migrely.app.reminder.ReminderActionReceiver
import java.time.LocalDate

/**
 * Migrely's notifications (BRD D66, D67), in two channels so the user can silence one kind in system settings. Nothing
 * is posted while notifications are off, so Android 13's permission is never needed at the moment of posting.
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
            .addAction(0, context.getString(R.string.notification_daily_log), openApp())
    )

    override fun showAttackLoggedToday() = notify(
        DAILY_ID,
        builder(CHANNEL_DAILY, R.string.notification_channel_daily)
            .setContentTitle(context.getString(R.string.notification_attack_logged))
            .setOnlyAlertOnce(true)
    )

    override fun cancelDailyReminder() = manager.cancel(DAILY_ID)

    override fun showEffectQuestion(intakeId: String, medicationName: String) = notify(
        effectId(intakeId),
        builder(CHANNEL_EFFECT, R.string.notification_channel_effect)
            .setContentTitle(context.getString(R.string.notification_effect_title, medicationName))
            .setContentText(context.getString(R.string.notification_effect_text))
    )

    override fun cancelEffectQuestion(intakeId: String) = manager.cancel(effectId(intakeId))

    private fun builder(channelId: String, channelName: Int): NotificationCompat.Builder {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                channelId,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                .setName(context.getString(channelName))
                .build()
        )
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openApp())
            .setAutoCancel(true)
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Checked by areEnabled() first, which on Android 13 and later also means the permission is granted.
    @SuppressLint("MissingPermission")
    private fun notify(id: Int, builder: NotificationCompat.Builder) {
        if (areEnabled()) manager.notify(id, builder.build())
    }

    private fun effectId(intakeId: String) = EFFECT_ID_BASE + intakeId.hashCode()

    private companion object {
        const val CHANNEL_DAILY = "daily"
        const val CHANNEL_EFFECT = "effect"
        const val DAILY_ID = 1
        const val EFFECT_ID_BASE = 1000
    }
}
