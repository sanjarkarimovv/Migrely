package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_START
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.RemoveIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.SelectAttackTagRequestDomainModel
import com.migrely.app.attack.domain.model.SetFunctionalImpactRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/** Use cases that only pass a write to their repository; each test checks the request reaches it unchanged. */
class SmallWriteUseCasesTest {
    private val context = CoroutineContextProvider.Default

    @Test
    fun `When selecting a location then the repository selects it`() {
        val repository: SelectAttackTagRepository = mock()
        val tag = AttackTagDomainModel.Location(HeadLocationDomainModel.Forehead)
        given(
            repository.selectAttackTag("a", tag, false)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = SelectAttackTagUseCase(repository, context)
            .executeInBackground(SelectAttackTagRequestDomainModel("a", tag, selected = false))

        assertEquals(WriteResultDomainModel.Written, actual)
    }

    @Test
    fun `When clearing the impact then the repository stores no impact`() {
        val repository: SetFunctionalImpactRepository = mock()
        given(repository.setFunctionalImpact("a", null)).willReturn(WriteResultDomainModel.Written)

        val actual = SetFunctionalImpactUseCase(repository, context)
            .executeInBackground(SetFunctionalImpactRequestDomainModel("a", null))

        assertEquals(WriteResultDomainModel.Written, actual)
    }

    @Test
    fun `When setting bed rest then the repository stores it`() {
        val repository: SetFunctionalImpactRepository = mock()
        given(repository.setFunctionalImpact("a", FunctionalImpactDomainModel.BedRest))
            .willReturn(WriteResultDomainModel.StorageFull)

        val actual = SetFunctionalImpactUseCase(repository, context)
            .executeInBackground(
                SetFunctionalImpactRequestDomainModel("a", FunctionalImpactDomainModel.BedRest)
            )

        assertEquals(WriteResultDomainModel.StorageFull, actual)
    }

    @Test
    fun `When removing a point then the repository removes it`() {
        val repository: RemoveIntensityPointRepository = mock()
        val point = IntensityPointDomainModel(FIXTURE_START, 6)
        given(
            repository.removeIntensityPoint("a", point)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = RemoveIntensityPointUseCase(repository, context)
            .executeInBackground(RemoveIntensityPointRequestDomainModel("a", point))

        assertEquals(WriteResultDomainModel.Written, actual)
    }

    @Test
    fun `When archiving a tag then the repository archives it`() {
        val repository: SetCustomTagArchivedRepository = mock()
        given(repository.setCustomTagArchived("t", true)).willReturn(WriteResultDomainModel.Written)

        assertEquals(
            WriteResultDomainModel.Written,
            ArchiveCustomTagUseCase(repository, context).executeInBackground("t")
        )
    }

    @Test
    fun `When deleting an attack then the repository deletes it`() {
        val repository: DeleteAttackRepository = mock()
        given(repository.deleteAttack("a")).willReturn(WriteResultDomainModel.Written)

        assertEquals(
            WriteResultDomainModel.Written,
            DeleteAttackUseCase(repository, context).executeInBackground("a")
        )
    }

    @Test
    fun `Given 1000 characters when saving the note then saved`() {
        val repository: UpdateAttackNoteRepository = mock()
        val note = "n".repeat(1_000)
        given(repository.updateAttackNote("a", note)).willReturn(WriteResultDomainModel.Written)

        val actual = UpdateAttackNoteUseCase(repository, context)
            .executeInBackground(UpdateAttackNoteRequestDomainModel("a", note))

        assertEquals(UpdateAttackNoteResultDomainModel.Saved, actual)
    }

    @Test
    fun `Given 1001 characters when saving the note then too long and nothing is stored`() {
        val repository: UpdateAttackNoteRepository = mock()

        val actual = UpdateAttackNoteUseCase(repository, context)
            .executeInBackground(UpdateAttackNoteRequestDomainModel("a", "n".repeat(1_001)))

        assertEquals(UpdateAttackNoteResultDomainModel.TooLong, actual)
        verify(repository, never()).updateAttackNote(any(), any())
    }
}
