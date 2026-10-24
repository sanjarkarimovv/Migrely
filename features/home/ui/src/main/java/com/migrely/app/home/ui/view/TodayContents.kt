package com.migrely.app.home.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.sky.SkyAppearance
import com.migrely.app.designsystem.sky.SkyCanvas
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.PeakUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel

private val SkyCardTitle = Color(0xFFC8CCD2)
private val SkyCardSubtitle = Color(0xFF9AA0A9)

@Composable
fun TodayContents(
    today: TodayUiModel,
    dayTitle: String,
    skyAppearanceUiMapper: SkyAppearanceUiMapper,
    onStartAttack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenModifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding()
        .padding(horizontal = 16.dp)
    val appearance = skyAppearanceUiMapper.toAppearance(today.sky)
    if (isCompactHeight()) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = screenModifier) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                DayTitle(dayTitle)
                TodaySkyCard(today = today, appearance = appearance)
                Spacer(Modifier.height(24.dp))
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                StartAttackButton(onStartAttack)
            }
        }
    } else {
        Column(modifier = screenModifier) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                DayTitle(dayTitle)
                TodaySkyCard(today = today, appearance = appearance)
            }
            Spacer(Modifier.height(16.dp))
            StartAttackButton(onStartAttack)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DayTitle(dayTitle: String) {
    Text(
        text = dayTitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 16.dp)
    )
}

@Composable
private fun StartAttackButton(onStartAttack: () -> Unit) {
    Button(
        onClick = onStartAttack,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        Text(
            text = stringResource(R.string.start_attack),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun TodaySkyCard(today: TodayUiModel, appearance: SkyAppearance) {
    val title = if (today.attackCount == 0) {
        stringResource(R.string.today_calm_title)
    } else {
        pluralStringResource(R.plurals.today_attack_count, today.attackCount, today.attackCount)
    }
    val subtitle = when (today.peak) {
        null -> stringResource(R.string.today_calm_subtitle)
        PeakUiModel.Mild -> stringResource(R.string.today_peak_mild)
        PeakUiModel.Moderate -> stringResource(R.string.today_peak_moderate)
        PeakUiModel.Severe -> stringResource(R.string.today_peak_severe)
        PeakUiModel.NotRecorded -> stringResource(R.string.today_peak_not_recorded)
    }
    val skyName = when (today.sky) {
        SkyUiModel.Calm -> stringResource(R.string.sky_calm)
        SkyUiModel.Hazy -> stringResource(R.string.sky_hazy)
        SkyUiModel.Cloudy -> stringResource(R.string.sky_cloudy)
        SkyUiModel.Storm -> stringResource(R.string.sky_storm)
    }
    val description = stringResource(R.string.today_card_description, skyName, title, subtitle)
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 208.dp)
            .clip(RoundedCornerShape(28.dp))
            .clearAndSetSemantics { contentDescription = description }
    ) {
        SkyCanvas(
            appearance = appearance,
            modifier = Modifier.matchParentSize(),
            decorationHeight = 92.dp
        )
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 96.dp, bottom = 20.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = SkyCardTitle)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SkyCardSubtitle
            )
        }
    }
}

@Preview(name = "Today: calm")
@Composable
private fun TodayCalmPreview() {
    MigrelyTheme {
        TodayContents(
            today = TodayUiModel(SkyUiModel.Calm, attackCount = 0, peak = null),
            dayTitle = "Thursday, 23 October",
            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
            onStartAttack = {}
        )
    }
}

@Preview(name = "Today: storm")
@Composable
private fun TodayStormPreview() {
    MigrelyTheme {
        TodayContents(
            today = TodayUiModel(SkyUiModel.Storm, attackCount = 2, peak = PeakUiModel.Severe),
            dayTitle = "Thursday, 23 October",
            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
            onStartAttack = {}
        )
    }
}
