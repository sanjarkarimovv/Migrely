package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class AddPastAttackNavigationEventDestinationMapperTest(
    private val event: AddPastAttackPresentationNavigationEvent,
    private val expectedRoute: AttackDetails
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AddPastAttackPresentationNavigationEvent.OnPastAttackAdded("new"),
                AttackDetails("new")
            ),
            arrayOf<Any>(
                AddPastAttackPresentationNavigationEvent.OnAttacksMerged("old"),
                AttackDetails("old", announceMerge = true)
            )
        )
    }

    @Test
    fun `When an attack is ready then its details replace the add screen`() {
        // Given
        val navController: NavController = mock()

        // When
        AddPastAttackNavigationEventDestinationMapper().toUi(event).navigate(navController)

        // Then
        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(expectedRoute),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertEquals(AddPastAttack::class, options.firstValue.popUpToRouteClass)
        assertTrue(options.firstValue.isPopUpToInclusive())
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }
}

class AddPastAttackLeaveNavigationTest {
    @Test
    fun `When leaving the add screen then pops up to and including it`() {
        // Given
        val navController: NavController = mock()

        // When
        AddPastAttackNavigationEventDestinationMapper()
            .toUi(AddPastAttackPresentationNavigationEvent.OnLeave)
            .navigate(navController)

        // Then
        verify(navController).popBackStack(AddPastAttack::class, true, false)
    }
}
