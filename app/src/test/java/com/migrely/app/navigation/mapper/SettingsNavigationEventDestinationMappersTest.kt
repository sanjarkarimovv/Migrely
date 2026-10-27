package com.migrely.app.navigation.mapper

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.ui.navigation.exception.UnhandledNavigationException
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel
import com.migrely.app.settings.presentation.navigation.DeleteAllDataPresentationNavigationEvent
import com.migrely.app.settings.presentation.navigation.SettingsPagePresentationNavigationEvent
import com.migrely.app.settings.presentation.navigation.SettingsPresentationNavigationEvent
import com.migrely.app.ui.main.route.AboutMigrely
import com.migrely.app.ui.main.route.DeleteAllData
import com.migrely.app.ui.main.route.Licences
import com.migrely.app.ui.main.route.Privacy
import com.migrely.app.ui.main.route.Reminders
import com.migrely.app.ui.main.route.Settings
import com.migrely.app.ui.main.route.WarningSigns
import com.migrely.app.ui.main.route.YourData
import kotlin.reflect.KClass
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
import org.mockito.kotlin.verifyNoInteractions

/** Settings opens its items once each; Back leaves the screen (change add-data-export-import). */
@RunWith(Parameterized::class)
class SettingsNavigationEventDestinationMappersTest(
    private val event: SettingsPresentationNavigationEvent,
    private val route: Any?
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters() = listOf(
            arrayOf(SettingsPresentationNavigationEvent.OnReminders, Reminders),
            arrayOf(SettingsPresentationNavigationEvent.OnYourData, YourData),
            arrayOf(SettingsPresentationNavigationEvent.OnWarningSigns, WarningSigns),
            arrayOf(SettingsPresentationNavigationEvent.OnAboutMigrely, AboutMigrely),
            arrayOf(SettingsPresentationNavigationEvent.OnPrivacy, Privacy),
            arrayOf(SettingsPresentationNavigationEvent.OnLicences, Licences),
            arrayOf(SettingsPresentationNavigationEvent.OnDeleteAllData, DeleteAllData),
            arrayOf(SettingsPresentationNavigationEvent.OnBack, null)
        )
    }

    @Test
    fun `When toUi then the matching destination`() {
        // Given
        val navController: NavController = mock()

        // When
        SettingsNavigationEventDestinationMapper().toUi(event).navigate(navController)

        // Then
        if (route == null) {
            verify(navController).popBackStack(Settings::class, true, false)
        } else {
            val options = argumentCaptor<NavOptions>()
            verify(
                navController
            ).navigate(eq(route), options.capture(), anyOrNull<Navigator.Extras>())
            assertTrue(options.firstValue.shouldLaunchSingleTop())
        }
    }
}

class DeleteAllDataNavigationEventDestinationMapperTest {
    @Test
    fun `Given all data is deleted when toUi then the app starts again, not a screen`() {
        // Given
        val navController: NavController = mock()
        var restarts = 0

        // When
        DeleteAllDataNavigationEventDestinationMapper { restarts++ }
            .toUi(DeleteAllDataPresentationNavigationEvent.OnAllDataDeleted)
            .navigate(navController)

        // Then
        assertEquals(1, restarts)
        verifyNoInteractions(navController)
    }

    @Test
    fun `Given going back when toUi then leaves the confirmation, and nothing restarts`() {
        // Given
        val navController: NavController = mock()
        var restarts = 0

        // When
        DeleteAllDataNavigationEventDestinationMapper { restarts++ }
            .toUi(DeleteAllDataPresentationNavigationEvent.OnBack)
            .navigate(navController)

        // Then
        verify(navController).popBackStack(DeleteAllData::class, true, false)
        assertEquals(0, restarts)
    }
}

/** Back from a page leaves that page only (change add-settings-and-safety). */
@RunWith(Parameterized::class)
class SettingsPageNavigationEventDestinationMapperTest(
    private val page: SettingsPagePresentationModel,
    private val route: KClass<*>
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters() = listOf(
            arrayOf(SettingsPagePresentationModel.WarningSigns, WarningSigns::class),
            arrayOf(SettingsPagePresentationModel.AboutMigrely, AboutMigrely::class),
            arrayOf(SettingsPagePresentationModel.Privacy, Privacy::class),
            arrayOf(SettingsPagePresentationModel.Licences, Licences::class)
        )
    }

    @Test
    fun `Given going back from a page when toUi then that page is left`() {
        // Given
        val navController: NavController = mock()

        // When
        SettingsPageNavigationEventDestinationMapper()
            .toUi(SettingsPagePresentationNavigationEvent.OnBack(page))
            .navigate(navController)

        // Then
        verify(navController).popBackStack(route, true, false)
    }
}

class YourDataNavigationEventDestinationMapperTest {
    @Test
    fun `Given going back when toUi then leaves Your data`() {
        // Given
        val navController: NavController = mock()

        // When
        YourDataNavigationEventDestinationMapper().toUi(YourDataPresentationNavigationEvent.OnBack)
            .navigate(navController)

        // Then
        verify(navController).popBackStack(YourData::class, true, false)
    }

    @Test(expected = UnhandledNavigationException::class)
    fun `Given an event Your data does not own when toUi then throws`() {
        YourDataNavigationEventDestinationMapper().toUi(object : PresentationNavigationEvent {})
    }
}
