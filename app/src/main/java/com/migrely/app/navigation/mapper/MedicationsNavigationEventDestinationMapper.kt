package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent
import com.migrely.app.ui.main.route.EditMedication
import com.migrely.app.ui.main.route.Medications

/** The editor opens once on top of the list; leaving pops the list (BRD MED-F1). */
class MedicationsNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<MedicationsPresentationNavigationEvent>(
        MedicationsPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: MedicationsPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        MedicationsPresentationNavigationEvent.OnAddMedication -> editor(null)
        is MedicationsPresentationNavigationEvent.OnEditMedication ->
            editor(navigationEvent.medicationId)

        MedicationsPresentationNavigationEvent.OnLeave -> UiDestination {
            it.popBackStack(Medications::class, inclusive = true)
        }
    }

    private fun editor(medicationId: String?) = UiDestination {
        it.navigate(EditMedication(medicationId), navOptions { launchSingleTop = true })
    }
}
