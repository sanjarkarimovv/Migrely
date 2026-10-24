package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent
import com.migrely.app.ui.main.route.YourData

class YourDataNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<YourDataPresentationNavigationEvent>(
        YourDataPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: YourDataPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        YourDataPresentationNavigationEvent.OnBack -> UiDestination {
            it.popBackStack(YourData::class, inclusive = true)
        }
    }
}
