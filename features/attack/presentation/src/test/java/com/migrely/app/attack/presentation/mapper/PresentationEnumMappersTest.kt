package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test

/** Every enum value maps to the value of the same name and back again. */
class PresentationEnumMappersTest {
    @Test
    fun `Locations round-trip`() = HeadLocationDomainModel.entries.forEach { domain ->
        val presentation = HeadLocationPresentationMapper().toPresentation(domain)
        assertEquals(domain.name, presentation.name)
        assertEquals(domain, HeadLocationDomainMapper().toDomain(presentation))
    }

    @Test
    fun `Symptoms round-trip`() = BuiltInSymptomDomainModel.entries.forEach { domain ->
        val presentation = BuiltInSymptomPresentationMapper().toPresentation(domain)
        assertEquals(domain.name, presentation.name)
        assertEquals(domain, BuiltInSymptomDomainMapper().toDomain(presentation))
    }

    @Test
    fun `Triggers round-trip`() = BuiltInTriggerDomainModel.entries.forEach { domain ->
        val presentation = BuiltInTriggerPresentationMapper().toPresentation(domain)
        assertEquals(domain.name, presentation.name)
        assertEquals(domain, BuiltInTriggerDomainMapper().toDomain(presentation))
    }

    @Test
    fun `Impacts round-trip`() = FunctionalImpactDomainModel.entries.forEach { domain ->
        val presentation = FunctionalImpactPresentationMapper().toPresentation(domain)
        assertEquals(domain.name, presentation.name)
        assertEquals(domain, FunctionalImpactDomainMapper().toDomain(presentation))
    }

    @Test
    fun `Tag kinds round-trip`() = TagKindDomainModel.entries.forEach { domain ->
        val presentation = TagKindPresentationMapper().toPresentation(domain)
        assertEquals(domain.name, presentation.name)
        assertEquals(domain, TagKindDomainMapper().toDomain(presentation))
    }

    @Test
    fun `Time fields keep their name`() = TimeFieldDomainModel.entries.forEach { domain ->
        assertEquals(domain.name, TimeFieldPresentationMapper().toPresentation(domain).name)
    }
}
