package com.migrely.app

import android.content.Intent
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.ui.main.AppNavHost
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.Home
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Intent action of the "Migraine started" launcher shortcut (res/xml/shortcuts.xml). */
const val ACTION_START_ATTACK = "com.migrely.app.action.START_ATTACK"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appNavHostDependencies: AppNavHostDependencies

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        // Honour the shortcut only on a fresh launch; a recreated activity restores its back stack instead.
        val startAttack = savedInstanceState == null && intent?.action == ACTION_START_ATTACK
        setContent {
            MigrelyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val controller = rememberNavController().also { navController = it }
                    appNavHostDependencies.AppNavHost(
                        navController = controller,
                        startDestination = Home(startAttack = startAttack)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_START_ATTACK) {
            navController?.navigate(Home(startAttack = true)) {
                popUpTo<Home> { inclusive = true }
            }
        }
    }
}
