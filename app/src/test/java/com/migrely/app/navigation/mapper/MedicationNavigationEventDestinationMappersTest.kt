package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent
import com.migrely.app.ui.main.route.EditMedication
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Medications
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MedicationNavigationEventDestinationMappersTest {
    private fun assertOpensOnce(destination: (NavController) -> Unit, route: Any) {
        val navController: NavController = mock()

        destination(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(eq(route), options.capture(), anyOrNull<Navigator.Extras>())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }

    @Test
    fun `Given attack mode when home logs a medication then the dark room for the attack`() {
        val mapper = HomeNavigationEventDestinationMapper()

        assertOpensOnce(
            { mapper.toUi(HomePresentationNavigationEvent.OnLogMedication("a")).navigate(it) },
            LogMedication("a", darkRoom = true)
        )
        assertOpensOnce(
            { mapper.toUi(HomePresentationNavigationEvent.OnLogMedication(null)).navigate(it) },
            LogMedication(null, darkRoom = false)
        )
    }

    @Test
    fun `When details log a medication then the normal screen for the attack`() {
        val mapper = AttackDetailsNavigationEventDestinationMapper()

        assertOpensOnce(
            {
                mapper.toUi(
                    AttackDetailsPresentationNavigationEvent.OnLogMedication("a")
                ).navigate(it)
            },
            LogMedication("a", darkRoom = false)
        )
    }

    @Test
    fun `When Medication taken manages or is done then the list once, or back`() {
        val mapper = LogMedicationNavigationEventDestinationMapper()
        assertOpensOnce(
            {
                mapper.toUi(
                    LogMedicationPresentationNavigationEvent.OnManageMedications
                ).navigate(it)
            },
            Medications
        )

        val navController: NavController = mock()
        mapper.toUi(LogMedicationPresentationNavigationEvent.OnDone).navigate(navController)
        verify(navController).popBackStack(LogMedication::class, true, false)
    }

    @Test
    fun `When the list adds, edits or leaves then the editor once, or back`() {
        val mapper = MedicationsNavigationEventDestinationMapper()
        assertOpensOnce(
            { mapper.toUi(MedicationsPresentationNavigationEvent.OnAddMedication).navigate(it) },
            EditMedication(null)
        )
        assertOpensOnce(
            {
                mapper.toUi(
                    MedicationsPresentationNavigationEvent.OnEditMedication("m")
                ).navigate(it)
            },
            EditMedication("m")
        )

        val navController: NavController = mock()
        mapper.toUi(MedicationsPresentationNavigationEvent.OnLeave).navigate(navController)
        verify(navController).popBackStack(Medications::class, true, false)
    }

    @Test
    fun `When the editor leaves then pops itself`() {
        val navController: NavController = mock()

        EditMedicationNavigationEventDestinationMapper()
            .toUi(EditMedicationPresentationNavigationEvent.OnLeave)
            .navigate(navController)

        verify(navController).popBackStack(EditMedication::class, true, false)
    }

    @Test(expected = UnhandledNavigationException::class)
    fun `Given a foreign event when Medication taken maps it then throws`() {
        LogMedicationNavigationEventDestinationMapper().toUi(
            object : PresentationNavigationEvent {}
        )
    }
}
