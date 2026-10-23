package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent

/** Both ways out of the details screen return to where it was opened from; home then offers any undo (BRD D23). */
class AttackDetailsNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<AttackDetailsPresentationNavigationEvent>(
        AttackDetailsPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: AttackDetailsPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        AttackDetailsPresentationNavigationEvent.OnLeave,
        AttackDetailsPresentationNavigationEvent.OnAttackDeleted -> UiDestination {
            it.popBackStack()
        }
    }
}
