package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class GetPastAttackDraftUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val classUnderTest = GetPastAttackDraftUseCase(
        getCurrentTimeRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given seconds past the minute when drafting then two hours to now, rounded down`() {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(Instant.parse("2026-10-23T11:42:30.250Z"), FIXTURE_ZONE)
        )

        val actual = classUnderTest.executeInBackground(Unit)

        assertEquals(
            PastAttackDraftDomainModel(
                start = Instant.parse("2026-10-23T09:42:00Z"),
                end = Instant.parse("2026-10-23T11:42:00Z"),
                zone = FIXTURE_ZONE
            ),
            actual
        )
    }
}
