package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.RestoreIntakeRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.RemoveIntakeRepository
import com.migrely.app.attack.domain.repository.RestoreIntakeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val INTAKE = AttackIntakeDomainModel(
    "i",
    "m",
    "Ibuprofen",
    "400 mg",
    Instant.parse("2026-10-24T09:20:00Z"),
    ZoneId.of("Asia/Tashkent")
)

class IntakeUseCasesTest {
    @Test
    fun `When removing an intake then the repository removes it`() {
        val repository: RemoveIntakeRepository = mock()
        given(repository.removeIntake("i")).willReturn(WriteResultDomainModel.Written)

        assertEquals(
            WriteResultDomainModel.Written,
            RemoveIntakeUseCase(
                repository,
                CoroutineContextProvider.Default
            ).executeInBackground("i")
        )
    }

    @Test
    fun `When restoring an intake then the same intake is stored for the attack`() {
        val repository: RestoreIntakeRepository = mock()
        given(repository.restoreIntake("a", INTAKE)).willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(
            WriteResultDomainModel.StorageFull,
            RestoreIntakeUseCase(repository, CoroutineContextProvider.Default)
                .executeInBackground(RestoreIntakeRequestDomainModel("a", INTAKE))
        )
    }
}
