package com.migrely.app.navigation.mapper

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.ui.main.route.EditMedication

/** Saved or left: back to the list; popping by route makes a late second event do nothing. */
class EditMedicationNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<EditMedicationPresentationNavigationEvent>(
        EditMedicationPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: EditMedicationPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        EditMedicationPresentationNavigationEvent.OnLeave -> UiDestination {
            it.popBackStack(EditMedication::class, inclusive = true)
        }
    }
}
