package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class AttackDetailsNavigationEventDestinationMapperTest(
    private val event: AttackDetailsPresentationNavigationEvent
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(AttackDetailsPresentationNavigationEvent.OnLeave),
            arrayOf<Any>(AttackDetailsPresentationNavigationEvent.OnAttackDeleted)
        )
    }

    @Test
    fun `When leaving the details screen then goes back`() {
        // Given
        val navController: NavController = mock()

        // When
        AttackDetailsNavigationEventDestinationMapper().toUi(event).navigate(navController)

        // Then
        verify(navController).popBackStack()
    }
}
