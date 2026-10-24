package com.migrely.app.architecture.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        LaunchedEffect(navController) {
            viewModel.navigationEvent.collect { navigationEvent ->
                navigationMapper.toUi(navigationEvent).navigate(navController)
            }
        }

        LaunchedEffect(Unit) {
            viewModel.notification.collect { notification ->
                notificationMapper.toUi(notification).present()
            }
        }
    }
}
