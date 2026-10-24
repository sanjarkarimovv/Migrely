package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.History
import java.time.LocalDate
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
class HistoryNavigationEventDestinationMapperTest(
    private val event: HistoryPresentationNavigationEvent,
    private val expectedRoute: Any
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                HistoryPresentationNavigationEvent.OnViewAttackDetails("a"),
                AttackDetails("a")
            ),
            arrayOf(
                HistoryPresentationNavigationEvent.OnAddPastAttack(LocalDate.ofEpochDay(20_381L)),
                AddPastAttack(20_381L)
            ),
            arrayOf(HistoryPresentationNavigationEvent.OnAddPastAttack(null), AddPastAttack())
        )
    }

    private val classUnderTest = HistoryNavigationEventDestinationMapper()

    @Test
    fun `When toUi then opens the route once on top of History`() {
        val navController: NavController = mock()

        classUnderTest.toUi(event).navigate(navController)

        val options = argumentCaptor<NavOptions>()
        verify(navController).navigate(
            eq(expectedRoute),
            options.capture(),
            anyOrNull<Navigator.Extras>()
        )
        assertTrue(options.firstValue.shouldLaunchSingleTop())
    }
}

class HistoryNavigationEventDestinationMapperLeaveTest {
    private val classUnderTest = HistoryNavigationEventDestinationMapper()

    @Test
    fun `Given leaving when toUi then pops History`() {
        val navController: NavController = mock()

        classUnderTest.toUi(HistoryPresentationNavigationEvent.OnLeave).navigate(navController)

        verify(navController).popBackStack(History::class, true, false)
    }

    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event History does not own when toUi then throws`() {
        classUnderTest.toUi(object : PresentationNavigationEvent {})
    }
}
