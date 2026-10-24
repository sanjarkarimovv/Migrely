package com.migrely.app.insights.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.ui.R
import com.migrely.app.insights.ui.di.InsightsDependencies
import com.migrely.app.insights.ui.model.InsightsBuiltInTriggerUiModel
import com.migrely.app.insights.ui.model.InsightsCycleUiModel
import com.migrely.app.insights.ui.model.InsightsMedicationUiModel
import com.migrely.app.insights.ui.model.InsightsMonthUiModel
import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsPercentUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayBarUiModel
import com.migrely.app.insights.ui.model.InsightsTimeOfDayUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerCountUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerUiModel
import com.migrely.app.insights.ui.model.InsightsUiModel
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel
import com.migrely.app.insights.ui.model.InsightsWeekdayBarUiModel
import java.time.DayOfWeek
import java.time.YearMonth

private val CONTENT_MAX_WIDTH = 600.dp
private val MONTH_CHOICES = listOf(3, 6, 12)
private const val DEFAULT_MONTHS = 6

/** Callbacks from the Insights screen. */
data class InsightsActions(val onBack: () -> Unit, val onPeriod: (months: Int) -> Unit)

/**
 * Insights (BRD INS-F1–F6, EC-INS-01–03, D87–D92), opened from History. The chosen months, 6 at first, are kept here.
 * The numbers are read on entering, on every return to the screen and on each new choice.
 */
@Composable
fun InsightsDependencies.Insights(navController: NavController, modifier: Modifier = Modifier) {
    var months by rememberSaveable { mutableIntStateOf(DEFAULT_MONTHS) }
    val currentMonths by rememberUpdatedState(months)
    ScreenEnterObserver { insightsViewModel.onEnter(currentMonths) }
    // A rotation brings a new view model, so the numbers are read again whenever the screen resumes.
    LifecycleResumeEffect(insightsViewModel) {
        insightsViewModel.onResume(currentMonths)
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)
    val viewState by insightsViewModel.viewState.collectAsState(InsightsViewState.Loading)
    val uiState = remember(viewState) { insightsViewStateUiMapper.toUi(viewState) }
    InsightsContents(
        viewState = uiState,
        months = months,
        actions = InsightsActions(
            onBack = insightsViewModel::onBackAction,
            onPeriod = {
                months = it
                insightsViewModel.onPeriodAction(it)
            }
        ),
        modifier = modifier
    )
}

/** The screen itself, without its view model. Insights for months other than the chosen ones show as loading. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InsightsContents(
    viewState: InsightsViewStateUiModel,
    months: Int,
    actions: InsightsActions,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.insights_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.insights_back)
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
                        .padding(16.dp)
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MONTH_CHOICES.forEach { choice ->
                            FilterChip(
                                selected = months == choice,
                                onClick = { if (choice != months) actions.onPeriod(choice) },
                                label = {
                                    Text(
                                        pluralStringResource(
                                            R.plurals.insights_months,
                                            choice,
                                            choice
                                        )
                                    )
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    val insights = (viewState as? InsightsViewStateUiModel.Ready)?.insights
                    when {
                        insights != null && insights.months == months -> InsightsBody(insights)
                        viewState == InsightsViewStateUiModel.Unavailable -> Text(
                            stringResource(R.string.insights_unavailable),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> {
                            val description = stringResource(R.string.insights_loading)
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().semantics {
                                    contentDescription = description
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The progress card or the sections, then the trends, which show either way (BRD D88). */
@Composable
private fun InsightsBody(insights: InsightsUiModel) {
    when (val patterns = insights.patterns) {
        is InsightsPatternsUiModel.NotEnough -> ProgressCard(patterns)
        is InsightsPatternsUiModel.Enough -> {
            TriggersSection(patterns)
            WhenSection(patterns)
            patterns.cycle?.let { CycleSection(it, patterns.migraines) }
            MedicationsSection(patterns.medications)
        }
    }
    TrendsSection(insights.trends)
}

private val PREVIEW_TRENDS = listOf(2, 4, 3, 6, 5, 3, 7, 4, 2, 5, 6, 3).mapIndexed { index, days ->
    val month = YearMonth.of(2025, 11).plusMonths(index.toLong())
    InsightsMonthUiModel(
        month.year,
        month.monthValue,
        days,
        days / 2 + 1,
        days,
        days / 4,
        days / 7f
    )
}

@Preview(name = "Insights", heightDp = 2400)
@Composable
private fun InsightsPreview() {
    val weekdays = listOf(3, 1, 1, 2, 1, 1, 1)
    val times = listOf(1, 4, 3, 2)
    MigrelyTheme {
        InsightsContents(
            viewState = InsightsViewStateUiModel.Ready(
                InsightsUiModel(
                    months = 6,
                    patterns = InsightsPatternsUiModel.Enough(
                        migraines = 10,
                        triggers = listOf(
                            InsightsTriggerCountUiModel(
                                InsightsTriggerUiModel.BuiltIn(
                                    InsightsBuiltInTriggerUiModel.Stress
                                ),
                                6,
                                InsightsPercentUiModel.Whole(60)
                            ),
                            InsightsTriggerCountUiModel(
                                InsightsTriggerUiModel.BuiltIn(
                                    InsightsBuiltInTriggerUiModel.TooLittleSleep
                                ),
                                4,
                                InsightsPercentUiModel.Whole(40)
                            ),
                            InsightsTriggerCountUiModel(
                                InsightsTriggerUiModel.Custom("Long drive"),
                                2,
                                InsightsPercentUiModel.Whole(20)
                            )
                        ),
                        weekdays = DayOfWeek.entries.mapIndexed { index, day ->
                            InsightsWeekdayBarUiModel(day, weekdays[index], weekdays[index] / 3f)
                        },
                        busiestWeekday = DayOfWeek.MONDAY,
                        timesOfDay = InsightsTimeOfDayUiModel.entries.mapIndexed { index, time ->
                            InsightsTimeOfDayBarUiModel(time, times[index], times[index] / 4f)
                        },
                        busiestTimeOfDay = InsightsTimeOfDayUiModel.Morning,
                        cycle = InsightsCycleUiModel(
                            migraines = 4,
                            percent = InsightsPercentUiModel.Whole(40),
                            days = 24,
                            periodDays = 184,
                            daysPercent = InsightsPercentUiModel.Whole(13)
                        ),
                        medications = listOf(
                            InsightsMedicationUiModel.Rated(
                                "Ibuprofen",
                                7,
                                9,
                                InsightsPercentUiModel.Whole(78)
                            ),
                            InsightsMedicationUiModel.NeedsAnswers("Sumatriptan", 2)
                        )
                    ),
                    trends = PREVIEW_TRENDS
                )
            ),
            months = 6,
            actions = InsightsActions({}, {})
        )
    }
}

@Preview(name = "Insights, not enough yet", heightDp = 1600)
@Composable
private fun InsightsNotEnoughPreview() {
    MigrelyTheme {
        InsightsContents(
            viewState = InsightsViewStateUiModel.Ready(
                InsightsUiModel(
                    months = 6,
                    patterns = InsightsPatternsUiModel.NotEnough(
                        missingMigraines = 2,
                        missingDays = 12
                    ),
                    trends = PREVIEW_TRENDS
                )
            ),
            months = 6,
            actions = InsightsActions({}, {})
        )
    }
}

@Preview(name = "Insights, unavailable")
@Composable
private fun InsightsUnavailablePreview() {
    MigrelyTheme {
        InsightsContents(
            viewState = InsightsViewStateUiModel.Unavailable,
            months = 3,
            actions = InsightsActions({}, {})
        )
    }
}
