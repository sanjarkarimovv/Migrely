package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Every built-in value maps to the value of the same name and back, so no stored key is lost or swapped. */
class EnumMappersTest {
    @Test
    fun `Locations map both ways by name`() {
        HeadLocationDataModel.entries.forEach { data ->
            val domain = HeadLocationDomainMapper().toDomain(data)
            assertEquals(data.name, domain.name)
            assertEquals(data, HeadLocationDataMapper().toData(domain))
        }
        assertEquals(HeadLocationDataModel.entries.size, HeadLocationDomainModel.entries.size)
    }

    @Test
    fun `Symptoms map both ways by name`() {
        BuiltInSymptomDataModel.entries.forEach { data ->
            val domain = BuiltInSymptomDomainMapper().toDomain(data)
            assertEquals(data.name, domain.name)
            assertEquals(data, BuiltInSymptomDataMapper().toData(domain))
        }
        assertEquals(BuiltInSymptomDataModel.entries.size, BuiltInSymptomDomainModel.entries.size)
    }

    @Test
    fun `Triggers map both ways by name`() {
        BuiltInTriggerDataModel.entries.forEach { data ->
            val domain = BuiltInTriggerDomainMapper().toDomain(data)
            assertEquals(data.name, domain.name)
            assertEquals(data, BuiltInTriggerDataMapper().toData(domain))
        }
        assertEquals(BuiltInTriggerDataModel.entries.size, BuiltInTriggerDomainModel.entries.size)
    }

    @Test
    fun `Tag kinds map by name`() {
        TagKindDomainModel.entries.forEach { kind ->
            assertEquals(kind.name, TagKindDataMapper().toData(kind).name)
        }
        assertEquals(TagKindDomainModel.entries.size, CustomTagKindDataModel.entries.size)
    }

    @Test
    fun `Impact levels 0 to 3 map and an unknown level reads as none`() {
        (0..3).forEach { level ->
            assertEquals(level, FunctionalImpactDomainMapper().toDomain(level)?.level)
        }
        assertEquals(
            FunctionalImpactDomainModel.BedRest,
            FunctionalImpactDomainMapper()
                .toDomain(3)
        )
        assertNull(FunctionalImpactDomainMapper().toDomain(7))
    }
}
