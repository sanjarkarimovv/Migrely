package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PreferencesDataSourceImplTest {
    private val editor: SharedPreferences.Editor = mock {
        on { putBoolean(any(), any()) } doReturn it
        on { putString(any(), anyOrNull()) } doReturn it
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
    }
}
