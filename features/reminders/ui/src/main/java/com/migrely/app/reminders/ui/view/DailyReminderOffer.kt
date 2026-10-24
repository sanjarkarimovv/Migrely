package com.migrely.app.reminders.ui.view

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.presentation.model.DailyReminderOfferViewState
import com.migrely.app.reminders.ui.R
import com.migrely.app.reminders.ui.di.DailyReminderOfferDependencies
import com.migrely.app.reminders.ui.model.DailyReminderOfferEventUiModel

private const val DEFAULT_MINUTE_OF_DAY = 20 * 60

/**
 * The daily reminder offer (BRD REM-F1, D80), with "Turn on" and "Not now". Turning it on asks for notification
 * permission only when needed, as the Reminders screen does; [onDone] runs once the offer is answered either way.
 */
@Composable
fun DailyReminderOfferDependencies.DailyReminderOffer(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = dailyReminderOfferViewModel
    LaunchedEffect(viewModel) { viewModel.onEnter() }
    val viewState by viewModel.viewState.collectAsState(DailyReminderOfferViewState.Loading)
    val time = (
        dailyReminderOfferUiMapper.toUi(
            viewState
        )?.minuteOfDay ?: DEFAULT_MINUTE_OF_DAY
        ).toReminderTime()
    val currentTime by rememberUpdatedState(time)
    val currentOnDone by rememberUpdatedState(onDone)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onPermissionResultAction(granted, currentTime)
        }
    LaunchedEffect(viewModel) {
        viewModel.notification.collect { notification ->
            when (dailyReminderOfferEventUiMapper.toUi(notification)) {
                DailyReminderOfferEventUiModel.AskForPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Android 12 and older have no such request: notifications are off in settings.
                        viewModel.onPermissionResultAction(false, currentTime)
                    }
                DailyReminderOfferEventUiModel.Done -> currentOnDone()
            }
        }
    }
    DailyReminderOfferContents(
        time = time.format(reminderTimePattern()),
        onTurnOn = { viewModel.onTurnOnAction(time) },
        onNotNow = viewModel::onNotNowAction,
        modifier = modifier
    )
}

/** The offer itself, without its view model. [time] is already formatted. */
@Composable
fun DailyReminderOfferContents(
    time: String,
    onTurnOn: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onTurnOn,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(stringResource(R.string.reminders_offer_turn_on, time))
        }
        TextButton(
            onClick = onNotNow,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                stringResource(R.string.reminders_offer_not_now),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(name = "Daily reminder offer")
@Composable
private fun DailyReminderOfferPreview() {
    MigrelyTheme {
        DailyReminderOfferContents(time = "8:00 PM", onTurnOn = {}, onNotNow = {})
    }
}
