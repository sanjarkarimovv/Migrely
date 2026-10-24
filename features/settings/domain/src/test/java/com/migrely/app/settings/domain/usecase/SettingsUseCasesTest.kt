package com.migrely.app.settings.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.GetSettingsRepository
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class SettingsUseCasesTest {
    @Test
    fun `When reading and setting then the repositories do it`() {
        val get: GetSettingsRepository = mock()
        val set: SetPeriodTrackingRepository = mock()
        given(get.settings()).willReturn(SettingsDomainModel(periodTracking = false))
        given(set.setPeriodTracking(true)).willReturn(SettingsWriteResultDomainModel.Failed)

        assertEquals(
            SettingsDomainModel(periodTracking = false),
            GetSettingsUseCase(get, CoroutineContextProvider.Default).executeInBackground(Unit)
        )
        assertEquals(
            SettingsWriteResultDomainModel.Failed,
            SetPeriodTrackingUseCase(
                set,
                CoroutineContextProvider.Default
            ).executeInBackground(true)
        )
    }
}
