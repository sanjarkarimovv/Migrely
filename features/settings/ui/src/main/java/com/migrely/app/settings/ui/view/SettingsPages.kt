package com.migrely.app.settings.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.di.SettingsPageDependencies

/** Decoration only: each sign is read on its own, without it. */
private const val BULLET = "•"

/** "Warning signs" (BRD SAF-F1, D95): the signs that need urgent care. No number is dialled; it differs by country. */
@Composable
fun SettingsPageDependencies.WarningSigns(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    WarningSignsContents(
        onBack = { settingsPageViewModel.onBackAction(SettingsPagePresentationModel.WarningSigns) },
        modifier = modifier
    )
}

/** "About Migrely" (BRD D97): the full disclaimer. */
@Composable
fun SettingsPageDependencies.AboutMigrely(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    AboutMigrelyContents(
        onBack = { settingsPageViewModel.onBackAction(SettingsPagePresentationModel.AboutMigrely) },
        modifier = modifier
    )
}

/** "Privacy" (BRD D93, D97): what Migrely keeps, where, and what leaves the phone. */
@Composable
fun SettingsPageDependencies.Privacy(navController: NavController, modifier: Modifier = Modifier) {
    ViewModelObserver(navController)
    PrivacyContents(
        onBack = { settingsPageViewModel.onBackAction(SettingsPagePresentationModel.Privacy) },
        modifier = modifier
    )
}

/** "Open-source licences" (BRD D97): what the app is built with, by licence. */
@Composable
fun SettingsPageDependencies.Licences(navController: NavController, modifier: Modifier = Modifier) {
    ViewModelObserver(navController)
    LicencesContents(
        onBack = { settingsPageViewModel.onBackAction(SettingsPagePresentationModel.Licences) },
        modifier = modifier
    )
}

@Composable
fun WarningSignsContents(onBack: () -> Unit, modifier: Modifier = Modifier) = SettingsPage(
    title = stringResource(R.string.settings_warning_signs),
    onBack = onBack,
    modifier = modifier
) {
    Text(
        text = stringResource(R.string.settings_warning_signs_call),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    stringArrayResource(R.array.settings_warning_signs_list).forEach { sign -> Bullet(sign) }
}

@Composable
fun AboutMigrelyContents(onBack: () -> Unit, modifier: Modifier = Modifier) = SettingsPage(
    title = stringResource(R.string.settings_about),
    onBack = onBack,
    modifier = modifier
) {
    Paragraph(stringResource(R.string.settings_about_diary))
    Paragraph(stringResource(R.string.settings_about_not_medical))
    Paragraph(stringResource(R.string.settings_about_doctor))
    Paragraph(stringResource(R.string.settings_about_urgent))
}

@Composable
fun PrivacyContents(onBack: () -> Unit, modifier: Modifier = Modifier) = SettingsPage(
    title = stringResource(R.string.settings_privacy),
    onBack = onBack,
    modifier = modifier
) {
    Paragraph(stringResource(R.string.settings_privacy_offline))
    Paragraph(stringResource(R.string.settings_privacy_phone))
    Paragraph(stringResource(R.string.settings_privacy_files))
    Paragraph(stringResource(R.string.settings_privacy_delete))
}

@Composable
fun LicencesContents(onBack: () -> Unit, modifier: Modifier = Modifier) = SettingsPage(
    title = stringResource(R.string.settings_licences),
    onBack = onBack,
    modifier = modifier
) {
    Paragraph(stringResource(R.string.settings_licences_intro))
    LicenceGroup(
        licence = stringResource(R.string.settings_licence_apache),
        names = stringArrayResource(R.array.settings_licences_apache)
    )
    LicenceGroup(
        licence = stringResource(R.string.settings_licence_ofl),
        names = stringArrayResource(R.array.settings_licences_ofl)
    )
}

/** A page of text with a title and a way back. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPage(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(title, modifier = Modifier.semantics { heading() }) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        MigrelyIcons.Back,
                        contentDescription = stringResource(R.string.settings_back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            )
        )
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Bullet(text: String) {
    Row {
        Text(
            text = BULLET,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 12.dp)
                .clearAndSetSemantics { }
        )
        Paragraph(text)
    }
}

@Composable
private fun LicenceGroup(licence: String, names: Array<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = licence,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics { heading() }
        )
        names.forEach { Paragraph(it) }
    }
}

@Preview(name = "Warning signs")
@Composable
private fun WarningSignsPreview() {
    MigrelyTheme { WarningSignsContents(onBack = {}) }
}

@Preview(name = "About Migrely, light")
@Composable
private fun AboutMigrelyPreview() {
    MigrelyTheme(dark = false) { AboutMigrelyContents(onBack = {}) }
}

@Preview(name = "Privacy")
@Composable
private fun PrivacyPreview() {
    MigrelyTheme { PrivacyContents(onBack = {}) }
}

@Preview(name = "Open-source licences")
@Composable
private fun LicencesPreview() {
    MigrelyTheme { LicencesContents(onBack = {}) }
}
