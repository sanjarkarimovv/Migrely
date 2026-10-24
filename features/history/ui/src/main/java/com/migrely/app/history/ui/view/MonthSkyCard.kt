package com.migrely.app.history.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.di.MonthSkyDependencies
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.model.MonthSkyViewStateUiModel
import java.time.LocalDate

/**
 * This month's calendar for home's slot under today's card (spec: today-sky, design D1). It loads whenever
 * [refreshKey] changes, which home does every time it loads, so it follows home's data. A month that cannot be read
 * shows nothing; home reports its own failures.
 */
@Composable
fun MonthSkyDependencies.MonthSky(
    navController: NavController,
    refreshKey: Int,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    LaunchedEffect(refreshKey) { monthSkyViewModel.onEnter() }
    val viewState by monthSkyViewModel.viewState.collectAsState(MonthSkyViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(monthSkyViewStateUiMapper.toUi(viewState))
    }
    MonthSkyContents(
        viewState = uiState,
        mappers = historyCalendarMappers,
        onMonth = monthSkyViewModel::onMonthAction,
        onDay = { epochDay -> monthSkyViewModel.onDayAction(LocalDate.ofEpochDay(epochDay)) },
        modifier = modifier
    )
}

@Composable
fun MonthSkyContents(
    viewState: MonthSkyViewStateUiModel,
    mappers: HistoryCalendarMappers,
    onMonth: () -> Unit,
    onDay: (epochDay: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val month = (viewState as? MonthSkyViewStateUiModel.Ready)?.month ?: return
    val monthName = mappers.historyTimeUiFormatter.formatMonthName(month.year, month.month)
    val description = stringResource(R.string.month_card_open, monthName)
    Column(modifier = modifier.padding(top = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                // Merged into the clickable, so the title stays focusable for keyboards and switch access (EC-UX-19).
                .semantics {
                    contentDescription = description
                    heading()
                }
                .clickable(role = Role.Button, onClick = onMonth)
                .padding(start = 8.dp, end = 4.dp)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Icon(
                MigrelyIcons.ChevronEnd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        HistoryCalendar(
            month = month,
            selectedEpochDay = null,
            mappers = mappers,
            onDay = onDay,
            modifier = Modifier.padding(top = 8.dp)
        )
        // The current month's progress, by History's rule; home's attack mode has no month card (BRD D55, EC-UX-13).
        month.stats.progress?.let {
            ProgressLine(it, mappers.historyTimeUiFormatter, Modifier.padding(top = 8.dp))
        }
    }
}

@Preview(name = "Month card")
@Composable
private fun MonthSkyCardPreview() {
    MigrelyTheme {
        MonthSkyContents(
            viewState = MonthSkyViewStateUiModel.Ready(previewMonth()),
            mappers = previewMappers,
            onMonth = {},
            onDay = {}
        )
    }
}
