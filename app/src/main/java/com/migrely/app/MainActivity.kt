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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.migrely.app.datasource.notification.datasource.NotificationDataSourceImpl
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.start.StartEntryPoint
import com.migrely.app.ui.main.AppNavHost
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Onboarding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Intent action of the "Migraine started" launcher shortcut (res/xml/shortcuts.xml). */
const val ACTION_START_ATTACK = "com.migrely.app.action.START_ATTACK"

private const val STARTS_WITH_ONBOARDING = "starts_with_onboarding"
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        dismissDailyReminderIfAsked(intent)
        startsWithOnboarding = savedInstanceState
            ?.takeIf { it.containsKey(STARTS_WITH_ONBOARDING) }
            ?.getBoolean(STARTS_WITH_ONBOARDING)
        // Honour the shortcut only until the start is decided; a recreated activity restores its back stack instead.
        // A recreation while the splash still waits has decided nothing yet, so it keeps the shortcut (review I2).
        val startAttack = startsWithOnboarding == null && intent?.action == ACTION_START_ATTACK
        // Opened to log a migraine, by the shortcut or the daily reminder: nothing holds that up (BRD D76).
        val openedToLog = startAttack ||
            intent?.getBooleanExtra(NotificationDataSourceImpl.EXTRA_DISMISS_DAILY, false) == true
        // The splash stays until the app knows where it starts, so home never shows for a moment first.
        splashScreen.setKeepOnScreenCondition { startsWithOnboarding == null }
        if (startsWithOnboarding == null) {
            lifecycleScope.launch {
                startsWithOnboarding = shouldShowOnboarding(openedToLog)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        startsWithOnboarding?.let { outState.putBoolean(STARTS_WITH_ONBOARDING, it) }
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
            MigrelyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
