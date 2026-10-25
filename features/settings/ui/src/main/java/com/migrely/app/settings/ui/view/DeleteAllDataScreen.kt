package com.migrely.app.settings.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyDarkRoom
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.di.DeleteAllDataDependencies

/** What the user typed and what the screen does with it. */
data class DeleteAllDataActions(
    val onTyped: (String) -> Unit,
    val onDelete: () -> Unit,
    val onBack: () -> Unit
)

/**
 * "Delete all data" (BRD EC-STO-03, D96), a screen of its own so that its last frame before the restart is its
 * progress, and a rotation brings it back as it was.
 *
 * The view model lives as long as the app, so the screen asks it where the deletion stands each time it is composed,
 * once its listeners above run, instead of once per visit: a rebuilt screen hears of a failure it missed, or is sent
 * on to the restart (ARCHITECTURE section 13).
 */
@Composable
fun DeleteAllDataDependencies.DeleteAllData(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    val viewState by deleteAllDataViewModel.viewState.collectAsState(
        DeleteAllDataViewState.Confirming
    )
    var typed by rememberSaveable { mutableStateOf("") }
    var failed by rememberSaveable { mutableStateOf(false) }
    // Set on confirming and cleared only by a failure: the progress stays until the app starts again.
    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(deleteAllDataEventPresenter) {
        deleteAllDataEventPresenter.notDeleted.collect {
            started = false
            failed = true
            deleteAllDataViewModel.onFailureShownAction()
        }
    }
    LaunchedEffect(deleteAllDataViewModel) { deleteAllDataViewModel.onEnter() }
    val deleting = deleteProgressUiResolver.resolve(
        started,
        deleteAllDataViewStateUiMapper.toUi(viewState)
    )
    // Nothing leaves the screen while it deletes.
    BackHandler(enabled = deleting) { }
    DeleteAllDataContents(
        typed = typed,
        word = deleteConfirmationResolver.word,
        confirmed = deleteConfirmationResolver.resolve(typed),
        deleting = deleting,
        failed = failed,
        actions = DeleteAllDataActions(
            onTyped = {
                typed = it
                failed = false
            },
            onDelete = {
                failed = false
                started = true
                deleteAllDataViewModel.onDeleteAction(typed)
            },
            onBack = deleteAllDataViewModel::onBackAction
        ),
        modifier = modifier
    )
}

/**
 * The screen itself. The form starts with a one-line warning and the field, and "Delete everything" sits below the
 * scrolling part, so the field and the button show together even in landscape at twice the font size; a line above the
 * button shows while there is more to read. While [deleting], the screen is dark in every theme, like the onboarding it
 * restarts into, so the settings clearing to the dark theme changes nothing on it.
 */
@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAllDataContents(
    typed: String,
    word: String,
    confirmed: Boolean,
    deleting: Boolean,
    failed: Boolean,
    actions: DeleteAllDataActions,
    modifier: Modifier = Modifier
) = MigrelyDarkRoom(active = deleting) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // A fresh bar when deleting starts: the bar animates a change of colour, which would fade it from light to
        // dark over the already dark screen.
        key(deleting) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_delete_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    if (!deleting) {
                        IconButton(onClick = actions.onBack) {
                            Icon(
                                MigrelyIcons.Back,
                                contentDescription = stringResource(R.string.settings_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
        }
        if (deleting) {
            Deleting()
        } else {
            Confirmation(typed, word, confirmed, failed, actions)
        }
    }
}

@Composable
private fun ColumnScope.Confirmation(
    typed: String,
    word: String,
    confirmed: Boolean,
    failed: Boolean,
    actions: DeleteAllDataActions
) {
    val scrollState = rememberScrollState()
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        ConfirmationBody(typed, word, failed, actions.onTyped, scrollState)
    }
    // There is more to read above the button.
    if (scrollState.canScrollForward) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = actions.onDelete,
            enabled = confirmed,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.settings_delete_confirm))
        }
    }
}

@Composable
private fun ConfirmationBody(
    typed: String,
    word: String,
    failed: Boolean,
    onTyped: (String) -> Unit,
    scrollState: ScrollState
) {
    // Kept tight, so the warning and the field fit above the button in landscape at twice the font size.
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = CONTENT_MAX_WIDTH)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_delete_warning),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = typed,
            onValueChange = onTyped,
            singleLine = true,
            label = { Text(stringResource(R.string.settings_delete_field, word)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (failed) {
            Text(
                text = stringResource(R.string.settings_not_deleted),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        Text(
            text = stringResource(R.string.settings_delete_what),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.settings_android_backup),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** In place of the form, until the app starts again. */
@Composable
private fun ColumnScope.Deleting() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_deleting),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
            )
        }
    }
}

private val PREVIEW_ACTIONS = DeleteAllDataActions({}, {}, {})

@Preview(name = "Delete all data")
@Composable
private fun DeleteAllDataPreview() {
    MigrelyTheme {
        DeleteAllDataContents(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = false,
            failed = false,
            actions = PREVIEW_ACTIONS
        )
    }
}

@Preview(name = "Delete all data, failed, light")
@Composable
private fun DeleteAllDataFailedPreview() {
    MigrelyTheme(dark = false) {
        DeleteAllDataContents(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = false,
            failed = true,
            actions = PREVIEW_ACTIONS
        )
    }
}

@Preview(name = "Delete all data, deleting, in the light theme")
@Composable
private fun DeleteAllDataDeletingPreview() {
    MigrelyTheme(dark = false) {
        DeleteAllDataContents(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = true,
            failed = false,
            actions = PREVIEW_ACTIONS
        )
    }
}
