package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AddCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.AddCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.CreateCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagCountResultDomainModel
import com.migrely.app.attack.domain.model.CustomTagLookupResultDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.model.customTag
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.CreateCustomTagRepository
import com.migrely.app.attack.domain.repository.FindCustomTagRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
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

class AddCustomTagUseCaseTest {
    private lateinit var classUnderTest: AddCustomTagUseCase
    private val findCustomTagRepository: FindCustomTagRepository = mock()
    private val countActiveCustomTagsRepository: CountActiveCustomTagsRepository = mock()
    private val createCustomTagRepository: CreateCustomTagRepository = mock()
    private val setCustomTagArchivedRepository: SetCustomTagArchivedRepository = mock()
    private val selectAttackTagRepository: SelectAttackTagRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = AddCustomTagUseCase(
            findCustomTagRepository,
            countActiveCustomTagsRepository,
            createCustomTagRepository,
            setCustomTagArchivedRepository,
            selectAttackTagRepository,
            CoroutineContextProvider.Default
        )
        given(
            selectAttackTagRepository.selectAttackTag(any(), any(), any())
        ).willReturn(WriteResultDomainModel.Written)
        given(
            findCustomTagRepository.customTag(any(), any())
        ).willReturn(CustomTagLookupResultDomainModel.None)
        given(countActiveCustomTagsRepository.activeCustomTagCount(any()))
            .willReturn(CustomTagCountResultDomainModel.Counted(3))
    }

    private fun add(name: String, kind: TagKindDomainModel = TagKindDomainModel.Symptom) =
        classUnderTest.executeInBackground(AddCustomTagRequestDomainModel("a", kind, name))

    @Test
    fun `Given a new name when adding then creates it trimmed and selects it`() {
        val created = customTag(id = "n", name = "Jaw pain")
        given(
            createCustomTagRepository.createCustomTag(
                TagKindDomainModel.Symptom,
                "Jaw pain",
                "jaw pain"
            )
        )
            .willReturn(CreateCustomTagResultDomainModel.Created(created))

        val actual = add("  Jaw pain ")

        assertEquals(AddCustomTagResultDomainModel.Added(created), actual)
        verify(selectAttackTagRepository).selectAttackTag(
            "a",
            AttackTagDomainModel.Symptom(SymptomDomainModel.Custom(created)),
            true
        )
    }

    @Test
    fun `Given the same name in another case when adding then reuses the existing tag`() {
        val existing = customTag(id = "e", name = "Jaw pain")
        given(findCustomTagRepository.customTag(TagKindDomainModel.Symptom, "jaw pain"))
            .willReturn(CustomTagLookupResultDomainModel.Found(existing))

        val actual = add("jaw PAIN ")

        assertEquals(AddCustomTagResultDomainModel.Added(existing), actual)
        verify(createCustomTagRepository, never()).createCustomTag(any(), any(), any())
    }

    @Test
    fun `Given an archived match when adding then brings it back to the list`() {
        val archived =
            customTag(
                id = "e",
                kind = TagKindDomainModel.Trigger,
                name = "Perfume",
                archived = true
            )
        given(findCustomTagRepository.customTag(TagKindDomainModel.Trigger, "perfume"))
            .willReturn(CustomTagLookupResultDomainModel.Found(archived))
        given(
            setCustomTagArchivedRepository.setCustomTagArchived("e", false)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = add("Perfume", TagKindDomainModel.Trigger)

        val restored = archived.copy(archived = false)
        assertEquals(AddCustomTagResultDomainModel.Added(restored), actual)
        verify(selectAttackTagRepository).selectAttackTag(
            "a",
            AttackTagDomainModel.Trigger(TriggerDomainModel.Custom(restored)),
            true
        )
    }

    @Test
    fun `Given only spaces when adding then invalid`() {
        assertEquals(AddCustomTagResultDomainModel.Invalid, add("   "))
    }

    @Test
    fun `Given 41 characters when adding then invalid`() {
        assertEquals(AddCustomTagResultDomainModel.Invalid, add("x".repeat(41)))
    }

    @Test
    fun `Given 40 characters when adding then created`() {
        val name = "x".repeat(40)
        given(createCustomTagRepository.createCustomTag(any(), any(), any()))
            .willReturn(CreateCustomTagResultDomainModel.Created(customTag(name = name)))

        assertEquals(AddCustomTagResultDomainModel.Added(customTag(name = name)), add(name))
    }

    @Test
    fun `Given 100 active tags when adding a new one then limit reached`() {
        given(countActiveCustomTagsRepository.activeCustomTagCount(TagKindDomainModel.Symptom))
            .willReturn(CustomTagCountResultDomainModel.Counted(100))

        assertEquals(AddCustomTagResultDomainModel.LimitReached, add("Jaw pain"))
        verify(createCustomTagRepository, never()).createCustomTag(any(), any(), any())
    }

    @Test
    fun `Given storage is full when creating then storage full`() {
        given(createCustomTagRepository.createCustomTag(any(), any(), any()))
            .willReturn(CreateCustomTagResultDomainModel.StorageFull)

        assertEquals(AddCustomTagResultDomainModel.StorageFull, add("Jaw pain"))
    }
}
