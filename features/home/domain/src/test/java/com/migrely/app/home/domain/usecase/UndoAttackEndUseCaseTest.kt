package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.repository.ReopenAttackRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class UndoAttackEndUseCaseTest(
    private val reopenResult: ReopenResultDomainModel,
    private val expected: UndoResultDomainModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Given {0} then {1}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf(ReopenResultDomainModel.Reopened, UndoResultDomainModel.Undone),
            arrayOf(ReopenResultDomainModel.AnotherInProgress, UndoResultDomainModel.NotPossible),
            arrayOf(ReopenResultDomainModel.StorageFull, UndoResultDomainModel.Failed),
            arrayOf(ReopenResultDomainModel.Failed, UndoResultDomainModel.Failed)
        )
    }

    private lateinit var classUnderTest: UndoAttackEndUseCase
    private val reopenAttackRepository: ReopenAttackRepository = mock()

    @Before
    fun setUp() {
        classUnderTest =
            UndoAttackEndUseCase(reopenAttackRepository, CoroutineContextProvider.Default)
    }

    @Test
    fun `When executed then maps the reopen result`() {
        given(reopenAttackRepository.reopenAttack("a")).willReturn(reopenResult)

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(expected, actual)
    }
}
