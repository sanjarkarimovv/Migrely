package com.migrely.app.onboarding.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.model.AttackFrequencyDataModel
import com.migrely.app.datasource.preferences.model.GoalDataModel
import com.migrely.app.onboarding.data.mapper.AttackFrequencyDataMapper
import com.migrely.app.onboarding.data.mapper.OnboardingGoalDataMapper
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.model.PhoneRecordsDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class OnboardingRepositoryTest {
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val backupDataSource: BackupDataSource = mock()
    private val classUnderTest = OnboardingRepository(
        preferencesDataSource,
        backupDataSource,
        OnboardingGoalDataMapper(),
        AttackFrequencyDataMapper()
    )

    @Test
    fun `When asking about the phone then some, none or failed`() {
        given(backupDataSource.hasRecords()).willReturn(true)
        assertEquals(PhoneRecordsDomainModel.Some, classUnderTest.phoneRecords())

        given(backupDataSource.hasRecords()).willReturn(false)
        assertEquals(PhoneRecordsDomainModel.None, classUnderTest.phoneRecords())

        given(backupDataSource.hasRecords()).willThrow(DatabaseDataException())
        assertEquals(PhoneRecordsDomainModel.Failed, classUnderTest.phoneRecords())
    }

    @Test
    fun `When saving the answers then the preferences hold them`() {
        given(preferencesDataSource.onboardingFinished()).willReturn(true)

        assertEquals(true, classUnderTest.onboardingFinished())
        assertEquals(
            OnboardingWriteResultDomainModel.Done,
            classUnderTest.setGoal(OnboardingGoalDomainModel.RecordForDoctor)
        )
        classUnderTest.setAttackFrequency(AttackFrequencyDomainModel.FourToFourteen)
        classUnderTest.setPeriodTracking(false)
        classUnderTest.finishOnboarding()

        verify(preferencesDataSource).setGoal(GoalDataModel.RecordForDoctor)
        verify(preferencesDataSource).setAttackFrequency(AttackFrequencyDataModel.FourToFourteen)
        verify(preferencesDataSource).setPeriodTracking(false)
        verify(preferencesDataSource).finishOnboarding()
    }

    @Test
    fun `Given a write is not saved then failed`() {
        whenever(preferencesDataSource.finishOnboarding()).doThrow(FileDataException())

        assertEquals(OnboardingWriteResultDomainModel.Failed, classUnderTest.finishOnboarding())
    }
}
