package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.RestoreCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.RestoreCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/** "Undo" after "Removed from your list" brings the tag back, unless its kind's list is full (BRD D199, EC-ATK-26). */
class RestoreCustomTagUseCaseTest {
    private lateinit var classUnderTest: RestoreCustomTagUseCase
    private val countActiveCustomTagsRepository: CountActiveCustomTagsRepository = mock()
    private val setCustomTagArchivedRepository: SetCustomTagArchivedRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = RestoreCustomTagUseCase(
            countActiveCustomTagsRepository,
            setCustomTagArchivedRepository,
            CoroutineContextProvider.Default
        )
        given(setCustomTagArchivedRepository.setCustomTagArchived(any(), any()))
            .willReturn(WriteResultDomainModel.Written)
    }

    private fun givenActive(count: Int, kind: TagKindDomainModel = TagKindDomainModel.Trigger) {
        given(countActiveCustomTagsRepository.activeCustomTagCount(kind))
            .willReturn(CustomTagCountResultDomainModel.Counted(count))
    }

    private fun restore(kind: TagKindDomainModel = TagKindDomainModel.Trigger) =
        classUnderTest.executeInBackground(RestoreCustomTagRequestDomainModel("t", kind))

    @Test
    fun `Given room in the list when restoring then un-archives the tag`() {
        givenActive(MAXIMUM_ACTIVE_CUSTOM_TAGS - 1)

        val actual = restore()

        assertEquals(RestoreCustomTagResultDomainModel.Restored, actual)
        verify(setCustomTagArchivedRepository).setCustomTagArchived("t", archived = false)
    }

    @Test
    fun `Given the list of that kind is full when restoring then refuses and writes nothing`() {
        givenActive(MAXIMUM_ACTIVE_CUSTOM_TAGS)

        val actual = restore()

        assertEquals(RestoreCustomTagResultDomainModel.LimitReached, actual)
        verify(setCustomTagArchivedRepository, never()).setCustomTagArchived(any(), any())
    }

    @Test
    fun `Given only the other kind is full when restoring then counts the tag's own kind`() {
        givenActive(MAXIMUM_ACTIVE_CUSTOM_TAGS, TagKindDomainModel.Trigger)
        givenActive(0, TagKindDomainModel.Symptom)

        val actual = restore(TagKindDomainModel.Symptom)

        assertEquals(RestoreCustomTagResultDomainModel.Restored, actual)
    }

    @Test
    fun `Given the count fails when restoring then fails and writes nothing`() {
        given(countActiveCustomTagsRepository.activeCustomTagCount(any()))
            .willReturn(CustomTagCountResultDomainModel.Failed)

        val actual = restore()

        assertEquals(RestoreCustomTagResultDomainModel.Failed, actual)
        verify(setCustomTagArchivedRepository, never()).setCustomTagArchived(any(), any())
    }

    @Test
    fun `Given storage is full when restoring then says so`() {
        givenActive(0)
        given(setCustomTagArchivedRepository.setCustomTagArchived("t", false))
            .willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(RestoreCustomTagResultDomainModel.StorageFull, restore())
    }

    @Test
    fun `Given the write fails when restoring then fails`() {
        givenActive(0)
        given(setCustomTagArchivedRepository.setCustomTagArchived("t", false))
            .willReturn(WriteResultDomainModel.Failed)

        assertEquals(RestoreCustomTagResultDomainModel.Failed, restore())
    }
}
