package com.migrely.app.onboarding.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.onboarding.presentation.model.AttackFrequencyPresentationModel
import com.migrely.app.onboarding.presentation.model.OnboardingGoalPresentationModel
import com.migrely.app.onboarding.ui.R
import com.migrely.app.onboarding.ui.di.OnboardingDependencies
import kotlinx.coroutines.delay

private const val STEPS = 4
private const val GOAL = 0
private const val FREQUENCY = 1
private const val PERIOD = 2
private const val REMINDER = 3
private const val MESSAGE_MILLIS = 6_000L
private const val STEP_GUARD_MILLIS = 800L
private val CONTENT_MAX_WIDTH = 600.dp

/** Callbacks from the onboarding steps. */
data class OnboardingActions(
    val onGoal: (OnboardingGoalPresentationModel) -> Unit,
    val onFrequency: (AttackFrequencyPresentationModel) -> Unit,
    val onPeriodTracking: (Boolean) -> Unit,
    val onSkip: () -> Unit,
    val onRestore: () -> Unit
)

/**
 * Onboarding (BRD ONB-F1–F3, EC-BAK-05, D77). Each step can be skipped, an answer moves straight on, and Back returns
 * to the previous step. The fourth step hosts the reminders feature's offer through [reminderOffer], which `:app`
 * fills; it calls its `onDone` once the offer is answered.
 */
@Composable
fun OnboardingDependencies.Onboarding(
    navController: NavController,
    reminderOffer: @Composable (onDone: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenEnterObserver { onboardingViewModel.onEnter() }
    ViewModelObserver(navController)
    var step by rememberSaveable { mutableIntStateOf(GOAL) }
    var notSaved by remember { mutableStateOf(false) }
    LaunchedEffect(onboardingEventPresenter) {
        onboardingEventPresenter.notSaved.collect { notSaved = true }
    }
    LaunchedEffect(notSaved) {
        if (notSaved) {
            delay(MESSAGE_MILLIS)
            notSaved = false
        }
    }
    BackHandler(enabled = step > GOAL) { step-- }
    val next: () -> Unit = {
        if (step < REMINDER) step++ else onboardingViewModel.onFinishAction()
    }
    OnboardingContents(
        step = step,
        actions = OnboardingActions(
            onGoal = { goal ->
                onboardingViewModel.onGoalAction(goal)
                next()
            },
            onFrequency = { frequency ->
                onboardingViewModel.onFrequencyAction(frequency)
                next()
            },
            onPeriodTracking = { on ->
                onboardingViewModel.onPeriodTrackingAction(on)
                next()
            },
            onSkip = next,
            onRestore = onboardingViewModel::onRestoreAction
        ),
        reminderOffer = { reminderOffer(onboardingViewModel::onFinishAction) },
        messages = {
            MigrelySnackbarHost(
                message = Unit.takeIf { notSaved },
                reduceMotion = rememberReduceMotion()
            ) {
                MigrelySnackbar(
                    text = stringResource(R.string.onboarding_not_saved),
                    tone = SnackbarTone.Error,
                    action = null,
                    dismissLabel = stringResource(R.string.onboarding_message_dismiss),
                    onDismiss = { notSaved = false }
                )
            }
        },
        modifier = modifier
    )
}

/** The steps themselves, without their view model. [step] counts from 0. */
@Composable
fun OnboardingContents(
    step: Int,
    actions: OnboardingActions,
    reminderOffer: @Composable () -> Unit,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                StepHeader(step = step, onSkip = actions.onSkip)
                when (step) {
                    GOAL -> GoalStep(actions)
                    FREQUENCY -> FrequencyStep(actions)
                    PERIOD -> PeriodStep(actions)
                    else -> ReminderStep(reminderOffer)
                }
            }
        }
        // Each step draws its choices where the last step's were, so a double tap must not answer the next one
        // (BRD EC-UX-18).
        TapGuard(
            key = step,
            durationMillis = STEP_GUARD_MILLIS,
            modifier = Modifier.matchParentSize()
        )
        messages()
    }
}

@Composable
private fun StepHeader(step: Int, onSkip: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_step, step + 1, STEPS),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        val skip = stringResource(R.string.onboarding_skip_description)
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics { contentDescription = skip }
        ) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        // Read out as each step appears, so the new question is heard, not only its number.
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
            }
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Choice(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun GoalStep(actions: OnboardingActions) {
    Title(stringResource(R.string.onboarding_goal_title))
    Choice(stringResource(R.string.onboarding_goal_doctor)) {
        actions.onGoal(OnboardingGoalPresentationModel.RecordForDoctor)
    }
    Choice(stringResource(R.string.onboarding_goal_triggers)) {
        actions.onGoal(OnboardingGoalPresentationModel.FindTriggers)
    }
    Choice(stringResource(R.string.onboarding_goal_painkillers)) {
        actions.onGoal(OnboardingGoalPresentationModel.FewerPainkillers)
    }
    Text(
        text = stringResource(R.string.onboarding_disclaimer),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
    TextButton(onClick = actions.onRestore, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(stringResource(R.string.onboarding_restore))
    }
}

@Composable
private fun FrequencyStep(actions: OnboardingActions) {
    Title(stringResource(R.string.onboarding_frequency_title))
    Choice(stringResource(R.string.onboarding_frequency_under_four)) {
        actions.onFrequency(AttackFrequencyPresentationModel.FewerThanFour)
    }
    Choice(stringResource(R.string.onboarding_frequency_four_to_fourteen)) {
        actions.onFrequency(AttackFrequencyPresentationModel.FourToFourteen)
    }
    Choice(stringResource(R.string.onboarding_frequency_fifteen_or_more)) {
        actions.onFrequency(AttackFrequencyPresentationModel.FifteenOrMore)
    }
}

@Composable
private fun PeriodStep(actions: OnboardingActions) {
    Title(stringResource(R.string.onboarding_period_title))
    Body(stringResource(R.string.onboarding_period_body))
    Choice(stringResource(R.string.onboarding_period_yes)) { actions.onPeriodTracking(true) }
    Choice(stringResource(R.string.onboarding_period_no)) { actions.onPeriodTracking(false) }
}

@Composable
private fun ReminderStep(reminderOffer: @Composable () -> Unit) {
    Title(stringResource(R.string.onboarding_reminder_title))
    Body(stringResource(R.string.onboarding_reminder_body))
    reminderOffer()
}

@Preview(name = "Onboarding, step 1")
@Composable
private fun OnboardingGoalPreview() {
    MigrelyTheme {
        OnboardingContents(
            step = GOAL,
            actions = OnboardingActions({}, {}, {}, {}, {}),
            reminderOffer = {},
            messages = {}
        )
    }
}

@Preview(name = "Onboarding, step 3")
@Composable
private fun OnboardingPeriodPreview() {
    MigrelyTheme {
        OnboardingContents(
            step = PERIOD,
            actions = OnboardingActions({}, {}, {}, {}, {}),
            reminderOffer = {},
            messages = {}
        )
    }
}
