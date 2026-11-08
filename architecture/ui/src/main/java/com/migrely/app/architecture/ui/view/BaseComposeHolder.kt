package com.migrely.app.architecture.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper

abstract class BaseComposeHolder<VIEW_STATE : Any, NOTIFICATION : PresentationNotification>(
    private val viewModel: BaseViewModel<VIEW_STATE, NOTIFICATION>,
    private val navigationMapper: NavigationEventDestinationMapper<*>,
    private val notificationMapper: NotificationUiMapper<NOTIFICATION>
) {
    // Collecting inside LaunchedEffect (instead of collectAsState) keeps two identical events in a
    // row from being dropped. See docs/ARCHITECTURE.md section 8.
    @Composable
    fun ViewModelObserver(navController: NavController) {
        val screen = LocalLifecycleOwner.current
        LaunchedEffect(navController) {
            viewModel.navigationEvent.collect { navigationEvent ->
                if (isCurrentScreen(navController, screen)) {
                    navigationMapper.toUi(navigationEvent).navigate(navController)
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.notification.collect { notification ->
                notificationMapper.toUi(notification).present()
            }
        }
    }

    /**
     * A screen stays composed through its exit fade, so a late async result, a semantics click or a
     * touch sent just before the screen left can still reach here after it has navigated. Touches
     * on a leaving screen are stopped in `:app`; this is what is left. Only the top of the back
     * stack may navigate; a screen fading in is the top already. Compared by id, because
     * `launchSingleTop` replaces the top entry's object and keeps its id. Outside a `NavHost` there
     * is no back stack.
     */
    private fun isCurrentScreen(navController: NavController, screen: LifecycleOwner): Boolean {
        val entry = screen as? NavBackStackEntry ?: return true
        return navController.currentBackStackEntry?.id == entry.id
    }
}
