package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FROM_MAPPER = "baseDurationMillis = behaviour.baseDurationMillis,"
private const val READING = "baseDurationMillis = READING_MILLIS,"
private const val BELOW_BAR = "topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR"

/**
 * Pins, in the source, how each screen's message is set up (BRD D158). The behaviour of each value
 * is tested where it lives: a mapper's storage-full message has no duration in that mapper's test,
 * and a message with no duration ignores a tap on its body in MessagePlaceAndTapTest. These screens
 * have no test that draws their message, so this is the link between the two.
 */
class MessageCallSitesTest {
    private val duration = mapOf(
        "HomeMessages" to FROM_MAPPER,
        "AttackMessages" to FROM_MAPPER,
        "HistoryMessages" to FROM_MAPPER,
        "MedicationMessages" to FROM_MAPPER,
        "ReportScreen" to FROM_MAPPER,
        "YourDataScreen" to FROM_MAPPER,
        "RemindersScreen" to FROM_MAPPER,
        "PaywallScreen" to FROM_MAPPER,
        "SettingsScreen" to READING,
        "OnboardingScreen" to READING
    )
    private val offset = mapOf(
        "AttackMessages" to BELOW_BAR,
        "HistoryMessages" to BELOW_BAR,
        "MedicationMessages" to BELOW_BAR,
        "ReportScreen" to BELOW_BAR,
        "YourDataScreen" to BELOW_BAR,
        "RemindersScreen" to BELOW_BAR,
        "SettingsScreen" to BELOW_BAR,
        "HomeMessages" to "topOffset = topOffset",
        "PaywallScreen" to "topOffset = MESSAGE_BELOW_CLOSE_ROW"
    )

    private fun source(name: String) =
        Konsist.scopeFromProduction().files.single { it.name == name }.text

    @Test
    fun `Every screen's message says where its duration comes from`() {
        val callers = Konsist.scopeFromProduction().files
            .filter { it.name != "MigrelySnackbar" && "MigrelySnackbar(" in it.text }
            .map { it.name }
            .toSet()

        assertEquals(duration.keys, callers)
        duration.forEach { (name, line) ->
            assertTrue("$name: $line", line in source(name))
        }
    }

    @Test
    fun `Every screen with a bar moves its message below the bar`() {
        offset.forEach { (name, line) ->
            assertTrue("$name: $line", line in source(name))
        }
        assertTrue("topOffset" !in source("OnboardingScreen"))
    }

    @Test
    fun `The paywall's message clears the Close row and has the shorter guard`() {
        val paywall = source("PaywallScreen")

        assertTrue("private val MESSAGE_BELOW_CLOSE_ROW = 48.dp" in paywall)
        assertTrue("private const val MESSAGE_GUARD_MILLIS = 400L" in paywall)
        assertTrue("guardMillis = MESSAGE_GUARD_MILLIS" in paywall)
    }

    @Test
    fun `Home decides its message's place with its tested rule`() {
        assertTrue(
            "if (messagesClearSettingsRow(uiState.mode, isCompactHeight())) {" in
                source("HomeScreen")
        )
    }
}
