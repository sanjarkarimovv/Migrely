package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent
import com.migrely.app.ui.main.route.DeleteAllData

/** [restartApp] starts the app again in a new process; in the app it is `restartApp(context)`. */
class DeleteAllDataNavigationEventDestinationMapper(private val restartApp: () -> Unit) :
    NavigationEventDestinationMapper<DeleteAllDataPresentationNavigationEvent>(
        DeleteAllDataPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: DeleteAllDataPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        DeleteAllDataPresentationNavigationEvent.OnBack -> UiDestination {
            it.popBackStack(DeleteAllData::class, inclusive = true)
        }

        DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted -> UiDestination { restartApp() }
    }
}
