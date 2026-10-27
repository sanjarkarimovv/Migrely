package com.migrely.app.navigation.mapper

import androidx.navigation.navOptions
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Medications

/** "Done" or back returns to the opener; "Manage medications" opens the list once on top (BRD D34). */
class LogMedicationNavigationEventDestinationMapper :
    NavigationEventDestinationMapper<LogMedicationPresentationNavigationEvent>(
        LogMedicationPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(
        navigationEvent: LogMedicationPresentationNavigationEvent
    ): UiDestination = when (navigationEvent) {
        LogMedicationPresentationNavigationEvent.OnManageMedications -> UiDestination {
            it.navigate(Medications, navOptions { launchSingleTop = true })
        }

        LogMedicationPresentationNavigationEvent.OnDone -> UiDestination {
            it.popBackStack(LogMedication::class, inclusive = true)
        }
    }
}
