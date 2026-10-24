package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PreferencesDataSourceImplTest {
    private val editor: SharedPreferences.Editor = mock {
        on { putBoolean(any(), any()) } doReturn it
        on { putString(any(), anyOrNull()) } doReturn it
        on { clear() } doReturn it
        on { commit() } doReturn true
    }
    private val preferences: SharedPreferences = mock {
        on { edit() } doReturn editor
        on { getBoolean("period_tracking", true) } doReturn true
        on { getBoolean("onboarding_finished", false) } doReturn false
    }
    private val classUnderTest = PreferencesDataSourceImpl(preferences)

    @Test
    fun `Given nothing saved then onboarding is not finished and periods are tracked`() {
        assertEquals(false, classUnderTest.onboardingFinished())
        assertEquals(true, classUnderTest.periodTracking())
        assertNull(classUnderTest.goal())
        assertNull(classUnderTest.attackFrequency())
    }

    @Test
    fun `When saving then the stored keys never change`() {
        classUnderTest.finishOnboarding()
        classUnderTest.setGoal(GoalDataModel.FindTriggers)
        classUnderTest.setAttackFrequency(AttackFrequencyDataModel.FifteenOrMore)
        classUnderTest.setPeriodTracking(false)

        verify(editor).putBoolean("onboarding_finished", true)
        verify(editor).putString("goal", "find_triggers")
        verify(editor).putString("attack_frequency", "15_or_more")
        verify(editor).putBoolean("period_tracking", false)
    }

    @Test
    fun `When reading the answers then known keys, and null for one this version does not know`() {
        given(preferences.getString("goal", null)).willReturn("fewer_painkillers")
        given(preferences.getString("attack_frequency", null)).willReturn("every_day")

        assertEquals(GoalDataModel.FewerPainkillers, classUnderTest.goal())
        assertNull(classUnderTest.attackFrequency())
    }

    @Test
    fun `Given the write does not reach the disk then a file failure`() {
        given(editor.commit()).willReturn(false)

        assertThrows(FileDataException::class.java) { classUnderTest.setPeriodTracking(true) }
        assertThrows(FileDataException::class.java) {
            classUnderTest.setTheme(ThemeDataModel.Light)
        }
        assertThrows(FileDataException::class.java) { classUnderTest.clear() }
    }

    @Test
    fun `Given no theme saved, or one this version does not know, then dark`() {
        assertEquals(ThemeDataModel.Dark, classUnderTest.theme())

        given(preferences.getString("theme", null)).willReturn("sepia")
        assertEquals(ThemeDataModel.Dark, classUnderTest.theme())
    }

    @Test
    fun `When choosing a theme then its stored key never changes, and it reads back`() {
        mapOf(
            ThemeDataModel.Dark to "dark",
            ThemeDataModel.Light to "light",
            ThemeDataModel.System to "system"
        ).forEach { (theme, key) ->
            classUnderTest.setTheme(theme)
            verify(editor).putString("theme", key)

            given(preferences.getString("theme", null)).willReturn(key)
            assertEquals(theme, classUnderTest.theme())
        }
    }

    @Test
    fun `When clearing then every setting goes in one write`() {
        classUnderTest.clear()

        verify(editor).clear()
        verify(editor).commit()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When following the theme then the current one, then each change, until stopped`() =
        runTest {
            val themes = mutableListOf<ThemeDataModel>()
            val following = launch(UnconfinedTestDispatcher(testScheduler)) {
                classUnderTest.themeChanges().toList(themes)
            }
            val listener = argumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener>()
            verify(preferences).registerOnSharedPreferenceChangeListener(listener.capture())

            given(preferences.getString("theme", null)).willReturn("light")
            listener.firstValue.onSharedPreferenceChanged(preferences, "theme")
            // Another setting, or the same theme again, says nothing new.
            listener.firstValue.onSharedPreferenceChanged(preferences, "goal")
            listener.firstValue.onSharedPreferenceChanged(preferences, "theme")
            // Cleared: every key at once.
            given(preferences.getString("theme", null)).willReturn(null)
            listener.firstValue.onSharedPreferenceChanged(preferences, null)
            following.cancel()

            assertEquals(
                listOf(ThemeDataModel.Dark, ThemeDataModel.Light, ThemeDataModel.Dark),
                themes
            )
            verify(preferences).unregisterOnSharedPreferenceChangeListener(listener.firstValue)
        }
}
