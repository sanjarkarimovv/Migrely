package com.migrely.app.onboarding.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.onboarding.data.mapper.AttackFrequencyDataMapper
import com.migrely.app.onboarding.data.mapper.OnboardingGoalDataMapper
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.model.PhoneRecordsDomainModel
import com.migrely.app.onboarding.domain.repository.FinishOnboardingRepository
import com.migrely.app.onboarding.domain.repository.GetOnboardingFinishedRepository
import com.migrely.app.onboarding.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.onboarding.domain.repository.SetAttackFrequencyRepository
import com.migrely.app.onboarding.domain.repository.SetGoalRepository
import com.migrely.app.onboarding.domain.repository.SetPeriodTrackingRepository

/** Onboarding's answers in the app's preferences, and whether the phone already holds records (BRD D76–D79). */
class OnboardingRepository(
    private val preferencesDataSource: PreferencesDataSource,
    private val backupDataSource: BackupDataSource,
    private val onboardingGoalDataMapper: OnboardingGoalDataMapper,
    private val attackFrequencyDataMapper: AttackFrequencyDataMapper
) : GetOnboardingFinishedRepository,
    GetPhoneRecordsRepository,
    FinishOnboardingRepository,
    SetGoalRepository,
    SetAttackFrequencyRepository,
    SetPeriodTrackingRepository {
    override fun onboardingFinished(): Boolean = preferencesDataSource.onboardingFinished()

    override fun phoneRecords(): PhoneRecordsDomainModel = try {
        when (backupDataSource.hasRecords()) {
            true -> PhoneRecordsDomainModel.Some
            false -> PhoneRecordsDomainModel.None
        }
    } catch (_: DataException) {
        PhoneRecordsDomainModel.Failed
    }

    override fun finishOnboarding() = write { preferencesDataSource.finishOnboarding() }

    override fun setGoal(goal: OnboardingGoalDomainModel) =
        write { preferencesDataSource.setGoal(onboardingGoalDataMapper.toData(goal)) }

    override fun setAttackFrequency(frequency: AttackFrequencyDomainModel) = write {
        preferencesDataSource.setAttackFrequency(attackFrequencyDataMapper.toData(frequency))
    }

    override fun setPeriodTracking(on: Boolean) = write {
        preferencesDataSource.setPeriodTracking(on)
    }

    private inline fun write(block: () -> Unit): OnboardingWriteResultDomainModel = try {
        block()
        OnboardingWriteResultDomainModel.Done
    } catch (_: DataException) {
        OnboardingWriteResultDomainModel.Failed
    }
}
