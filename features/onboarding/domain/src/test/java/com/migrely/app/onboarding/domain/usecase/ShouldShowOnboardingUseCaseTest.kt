package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.model.PhoneRecordsDomainModel
import com.migrely.app.onboarding.domain.repository.FinishOnboardingRepository
import com.migrely.app.onboarding.domain.repository.GetOnboardingFinishedRepository
import com.migrely.app.onboarding.domain.repository.GetPhoneRecordsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ShouldShowOnboardingUseCaseTest {
    private val getOnboardingFinishedRepository: GetOnboardingFinishedRepository = mock()
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository = mock()
    private val finishOnboardingRepository: FinishOnboardingRepository = mock {
        on { finishOnboarding() } doReturn OnboardingWriteResultDomainModel.Done
    }
    private val classUnderTest = ShouldShowOnboardingUseCase(
        getOnboardingFinishedRepository,
        getPhoneRecordsRepository,
        finishOnboardingRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given a new phone then onboarding shows`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.None)

        assertEquals(true, classUnderTest.executeInBackground(false))
        verify(finishOnboardingRepository, never()).finishOnboarding()
    }

    @Test
    fun `Given the app opens to start an attack then no onboarding, and nothing is read`() {
        assertEquals(false, classUnderTest.executeInBackground(true))
        verifyNoInteractions(getOnboardingFinishedRepository, getPhoneRecordsRepository)
    }

    @Test
    fun `Given onboarding was finished then it does not show`() {
        given(getOnboardingFinishedRepository.onboardingFinished()).willReturn(true)

        assertEquals(false, classUnderTest.executeInBackground(false))
        verifyNoInteractions(getPhoneRecordsRepository)
    }

    @Test
    fun `Given records on the phone then onboarding is finished without showing`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Some)

        assertEquals(false, classUnderTest.executeInBackground(false))
        verify(finishOnboardingRepository).finishOnboarding()
    }

    @Test
    fun `Given the records cannot be read then home, and onboarding is not finished`() {
        given(getPhoneRecordsRepository.phoneRecords()).willReturn(PhoneRecordsDomainModel.Failed)

        assertEquals(false, classUnderTest.executeInBackground(false))
        verify(finishOnboardingRepository, never()).finishOnboarding()
    }
}
