package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsScreenDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.CustomTagsResultDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_NOW
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.model.customTag
import com.migrely.app.attack.domain.repository.GetActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class GetAttackDetailsUseCaseTest {
    private lateinit var classUnderTest: GetAttackDetailsUseCase
    private val getAttackDetailsRepository: GetAttackDetailsRepository = mock()
    private val getActiveCustomTagsRepository: GetActiveCustomTagsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = GetAttackDetailsUseCase(
            getAttackDetailsRepository,
            getActiveCustomTagsRepository,
            getCurrentTimeRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(FIXTURE_NOW, FIXTURE_ZONE))
    }

    @Test
    fun `Given the attack and tags load when executing then returns the screen`() {
        val symptom = customTag(id = "s", kind = TagKindDomainModel.Symptom)
        val trigger = customTag(id = "t", kind = TagKindDomainModel.Trigger, name = "Perfume")
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.Found(attackDetails()))
        given(getActiveCustomTagsRepository.activeCustomTags(TagKindDomainModel.Symptom))
            .willReturn(CustomTagsResultDomainModel.Loaded(listOf(symptom)))
        given(getActiveCustomTagsRepository.activeCustomTags(TagKindDomainModel.Trigger))
            .willReturn(CustomTagsResultDomainModel.Loaded(listOf(trigger)))

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(
            AttackDetailsResultDomainModel.Loaded(
                AttackDetailsScreenDomainModel(
                    attackDetails(),
                    listOf(symptom),
                    listOf(trigger),
                    FIXTURE_NOW
                )
            ),
            actual
        )
    }

    @Test
    fun `Given the attack is gone when executing then not found`() {
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.NotFound)

        assertEquals(
            AttackDetailsResultDomainModel.NotFound,
            classUnderTest.executeInBackground("a")
        )
    }

    @Test
    fun `Given custom tags fail to load when executing then failed`() {
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.Found(attackDetails()))
        given(getActiveCustomTagsRepository.activeCustomTags(TagKindDomainModel.Symptom))
            .willReturn(CustomTagsResultDomainModel.Failed)

        assertEquals(AttackDetailsResultDomainModel.Failed, classUnderTest.executeInBackground("a"))
    }
}
