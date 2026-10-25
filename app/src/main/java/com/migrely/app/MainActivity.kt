package com.migrely.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.migrely.app.datasource.notification.datasource.NotificationDataSourceImpl
import com.migrely.app.designsystem.theme.DarkScreens
import com.migrely.app.designsystem.theme.LocalDarkScreens
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.start.StartEntryPoint
import com.migrely.app.ui.main.AppNavHost
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Onboarding
import com.migrely.app.ui.theme.mapper.AppThemeUiMapper
import com.migrely.app.ui.theme.mapper.AppearanceUiMapper
import com.migrely.app.ui.theme.mapper.DarkThemeUiResolver
import com.migrely.app.ui.theme.model.AppThemeUiModel
import com.migrely.app.ui.theme.model.AppearanceUiModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Intent action of the "Migraine started" launcher shortcut (res/xml/shortcuts.xml). */
const val ACTION_START_ATTACK = "com.migrely.app.action.START_ATTACK"

private const val STARTS_WITH_ONBOARDING = "starts_with_onboarding"
private const val THEME = "theme"
private const val ATTACK_IN_PROGRESS = "attack_in_progress"
private const val START_DECISION_MILLIS = 2_000L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appNavHostDependencies: AppNavHostDependencies

    private var navController: NavHostController? = null

    /** Null until decided; kept across a recreation, so the restored back stack keeps its start (BRD D76). */
    private var startsWithOnboarding: Boolean? = null

    /** The shortcut arrived while the start was still being decided. */
    private var pendingStartAttack = false

    /**
     * How the app was drawn last (BRD D94): the chosen theme, and whether an attack is in progress, which makes every
     * screen dark. It is read behind the splash and kept across a recreation, so the first frame is always right; the
     * screens then follow each change at once.
     */
    private var appearance = AppearanceUiModel(AppThemeUiModel.Dark, attackInProgress = false)

    private val darkThemeUiResolver = DarkThemeUiResolver()

    private val appearanceChanges: Flow<AppearanceUiModel> by lazy {
        val appearanceUiMapper = AppearanceUiMapper(AppThemeUiMapper())
        EntryPointAccessors.fromApplication(applicationContext, StartEntryPoint::class.java)
            .getAppearanceUseCase()
            .executeInBackground(Unit)
            .map(appearanceUiMapper::toUi)
            .flowOn(Dispatchers.IO)
            // A store that cannot be read keeps the appearance shown.
            .catch { }
    }

    /**
     * Every screen shows a time with its city when its zone differs from the device's (BRD EC-TIM-02). A new device zone,
     * set while the app is open or away, is applied like a configuration change: the activity is recreated, so every
     * time is drawn again and the saved state is kept (BRD D36).
     */
    private lateinit var renderedZone: ZoneId
    private val zoneChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = recreateIfZoneChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        renderedZone = ZoneId.systemDefault()
        // Dark like the splash; the chosen theme's bars follow with the first frame.
        applySystemBars(dark = true)
        super.onCreate(savedInstanceState)
        dismissDailyReminderIfAsked(intent)
        startsWithOnboarding = savedInstanceState
            ?.takeIf { it.containsKey(STARTS_WITH_ONBOARDING) }
            ?.getBoolean(STARTS_WITH_ONBOARDING)
        savedInstanceState?.getString(THEME)
            ?.let { saved -> AppThemeUiModel.entries.firstOrNull { it.name == saved } }
            ?.let { theme ->
                appearance = AppearanceUiModel(
                    theme,
                    attackInProgress = savedInstanceState.getBoolean(ATTACK_IN_PROGRESS)
                )
            }
        // Honour the shortcut only until the start is decided; a recreated activity restores its back stack instead.
        // A recreation while the splash still waits has decided nothing yet, so it keeps the shortcut (review I2).
        val startAttack = startsWithOnboarding == null && intent?.action == ACTION_START_ATTACK
        // Opened to log a migraine, by the shortcut or the daily reminder: nothing holds that up (BRD D76).
        val openedToLog = startAttack ||
            intent?.getBooleanExtra(NotificationDataSourceImpl.EXTRA_DISMISS_DAILY, false) == true
        // The splash stays until the app knows where it starts and in which theme, so home never shows for a moment
        // first, and no frame shows in the other theme.
        splashScreen.setKeepOnScreenCondition { startsWithOnboarding == null }
        if (startsWithOnboarding == null) {
            lifecycleScope.launch {
                coroutineScope {
                    val chosenAppearance = async { readAppearance() }
                    val onboarding = shouldShowOnboarding(openedToLog)
                    appearance = chosenAppearance.await()
                    startsWithOnboarding = onboarding
                }
                showContent(startAttack || pendingStartAttack)
            }
        } else {
            showContent(startAttack)
        }
    }

    /**
     * Asks once where the app starts. Home is the answer whenever the question cannot be answered in time or at all,
     * so a slow or broken store never keeps the user out.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun shouldShowOnboarding(openedToLog: Boolean): Boolean = try {
        val useCase = EntryPointAccessors.fromApplication(
            applicationContext,
            StartEntryPoint::class.java
        )
            .shouldShowOnboardingUseCase()
        withTimeoutOrNull(START_DECISION_MILLIS) {
            withContext(Dispatchers.IO) { useCase.executeInBackground(openedToLog) }
        } ?: false
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        false
    }

    /** The appearance, or the one shown when it cannot be read in time, like the start decision. */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun readAppearance(): AppearanceUiModel = try {
        withTimeoutOrNull(START_DECISION_MILLIS) { appearanceChanges.first() } ?: appearance
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        appearance
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        startsWithOnboarding?.let { outState.putBoolean(STARTS_WITH_ONBOARDING, it) }
        outState.putString(THEME, appearance.theme.name)
        outState.putBoolean(ATTACK_IN_PROGRESS, appearance.attackInProgress)
    }

    private fun showContent(startAttack: Boolean) {
        // Starting an attack wins over onboarding, and the saved start must match the graph that is shown.
        if (startAttack) startsWithOnboarding = false
        val start: Any = if (startsWithOnboarding == true) {
            Onboarding
        } else {
            Home(startAttack = startAttack)
        }
        setContent {
            val shown by appearanceChanges.collectAsState(initial = appearance)
            SideEffect { appearance = shown }
            // During an attack every screen is dark, those opened from attack mode included (BRD D94).
            val dark = darkThemeUiResolver.resolve(shown, isSystemInDarkTheme())
            // Attack mode and the dark room stay dark in the light theme, and the bars with them.
            val darkScreens = remember { DarkScreens() }
            val barsDark = dark || darkScreens.shown > 0
            // Applied with the composition, before the frame it belongs to is drawn.
            DisposableEffect(barsDark) {
                applySystemBars(dark = barsDark)
                onDispose { }
            }
            CompositionLocalProvider(LocalDarkScreens provides darkScreens) {
                MigrelyTheme(dark = dark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        // Dark under attack mode and the dark room, so a fade between two of them never shows the
                        // light theme in between.
                        color = if (darkScreens.shown > 0) {
                            MigrelyColors.AttackModeBackground
                        } else {
                            MaterialTheme.colorScheme.background
                        },
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        val controller = rememberNavController().also { navController = it }
                        appNavHostDependencies.AppNavHost(
                            navController = controller,
                            startDestination = start
                        )
                    }
                }
            }
        }
    }

    /** Light bar icons over a dark screen, dark ones over a light screen; the bars themselves stay see-through. */
    private fun applySystemBars(dark: Boolean) {
        val style = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            zoneChangeReceiver,
            IntentFilter(Intent.ACTION_TIMEZONE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        recreateIfZoneChanged()
    }

    override fun onStop() {
        unregisterReceiver(zoneChangeReceiver)
        super.onStop()
    }

    private fun recreateIfZoneChanged() {
        if (ZoneId.systemDefault() != renderedZone) recreate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dismissDailyReminderIfAsked(intent)
        if (intent.action == ACTION_START_ATTACK) {
            val controller = navController
            if (controller == null) {
                // Still deciding where to start: the attack starts as soon as content is shown.
                pendingStartAttack = true
            } else {
                // Clears the whole stack, onboarding included: starting an attack is never held up (BRD D76).
                controller.navigate(Home(startAttack = true)) {
                    popUpTo(controller.graph.id) { inclusive = true }
                }
            }
        }
    }

    // "Log a migraine" on the daily reminder opens the app; an action button does not clear its own notification.
    private fun dismissDailyReminderIfAsked(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationDataSourceImpl.EXTRA_DISMISS_DAILY, false) ==
            true
        ) {
            NotificationManagerCompat.from(this).cancel(NotificationDataSourceImpl.DAILY_ID)
        }
    }
}
