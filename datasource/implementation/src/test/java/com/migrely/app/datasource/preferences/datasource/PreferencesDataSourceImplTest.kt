package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.datasource.preferences.model.ReviewRequestsDataModel
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
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PreferencesDataSourceImplTest {
    private val editor: SharedPreferences.Editor = mock {
        on { putBoolean(any(), any()) } doReturn it
        on { putString(any(), anyOrNull()) } doReturn it
        on { putInt(any(), any()) } doReturn it
        on { putLong(any(), any()) } doReturn it
        on { clear() } doReturn it
        on { commit() } doReturn true
    }
    private val preferences: SharedPreferences = mock {
        on { edit() } doReturn editor
        on { getBoolean("period_tracking", true) } doReturn true
        on { getBoolean("onboarding_finished", false) } doReturn false
    }
    private val entitlementEditor: SharedPreferences.Editor = mock {
        on { clear() } doReturn it
        on { commit() } doReturn true
    }
    private val entitlementPreferences: SharedPreferences = mock {
        on { edit() } doReturn entitlementEditor
    }
    private val classUnderTest = PreferencesDataSourceImpl(preferences, entitlementPreferences)

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
    fun `Given no request for a rating yet then none, with no time`() {
        assertEquals(ReviewRequestsDataModel(0, null), classUnderTest.reviewRequests())
    }

    @Test
    fun `Given requests made then their count and the last time`() {
        given(preferences.getInt("review_request_count", 0)).willReturn(2)
        given(preferences.contains("review_last_requested")).willReturn(true)
        given(preferences.getLong("review_last_requested", 0L)).willReturn(1_700_000_000_000L)

        assertEquals(
            ReviewRequestsDataModel(2, 1_700_000_000_000L),
            classUnderTest.reviewRequests()
        )
    }

    @Test
    fun `When a request is made then the count goes up with its time, in one write`() {
        given(preferences.getInt("review_request_count", 0)).willReturn(1)

        classUnderTest.markReviewRequested(1_700_000_000_000L)

        inOrder(editor) {
            verify(editor).putInt("review_request_count", 2)
            verify(editor).putLong("review_last_requested", 1_700_000_000_000L)
            verify(editor).commit()
        }
    }

    @Test
    fun `When clearing then every setting goes in one write, and the entitlement's file first`() {
        classUnderTest.clear()

        inOrder(entitlementEditor, editor) {
            verify(entitlementEditor).clear()
            verify(entitlementEditor).commit()
            verify(editor).clear()
            verify(editor).commit()
        }
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
