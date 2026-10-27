package com.migrely.app.test.launcher

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.migrely.app.test.test.TypedAndroidComposeTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun <ACTIVITY : ComponentActivity> fromComposable(
    composeContentTestRule: TypedAndroidComposeTestRule<ACTIVITY>,
    composable: @Composable (ACTIVITY) -> Unit
) = AppLauncher {
    val activity = composeContentTestRule.activity
    activity.findViewById<ViewGroup>(android.R.id.content)?.let { root ->
        runBlocking(Dispatchers.Main) {
            root.removeAllViews()
        }
    }
    composeContentTestRule.setContent { composable(activity) }
}
