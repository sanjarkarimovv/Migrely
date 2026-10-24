package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TASHKENT
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.StartAttackRepository
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class StartAttackUseCaseTest(private val repositoryResult: StartAttackResultDomainModel) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Given repository returns {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf(StartAttackResultDomainModel.Started(attack())),
            arrayOf(StartAttackResultDomainModel.AlreadyInProgress(attack())),
            arrayOf(StartAttackResultDomainModel.StorageFull),
            arrayOf(StartAttackResultDomainModel.Failed)
        )
    }

    private lateinit var classUnderTest: StartAttackUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val startAttackRepository: StartAttackRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = StartAttackUseCase(
            getCurrentTimeRepository,
            startAttackRepository,
            CoroutineContextProvider.Default
        )
    }

    @Test
    fun `When executed then starts now in the current zone`() {
        // Given
        val now = Instant.parse("2026-10-23T09:00:00Z")
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(now, TASHKENT))
        given(
            startAttackRepository.startAttackIfNoneInProgress(now, TASHKENT)
        ).willReturn(repositoryResult)

        // When
        val actual = classUnderTest.executeInBackground(Unit)

        // Then
        assertEquals(repositoryResult, actual)
    }
}
