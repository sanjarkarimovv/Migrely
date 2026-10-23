package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")

class AttackDetailsDomainResolverTest {
    private val classUnderTest = AttackDetailsDomainResolver(
        HeadLocationDomainMapper(),
        BuiltInSymptomDomainMapper(),
        BuiltInTriggerDomainMapper(),
        CustomTagDomainMapper(),
        FunctionalImpactDomainMapper()
    )

    private fun attack(tags: List<AttackTagDataModel>, zone: String = "Asia/Tashkent") =
        AttackDataModel(
            id = "a",
            start = START,
            startZoneId = zone,
            end = START.plusSeconds(3_600),
            intensityPoints = listOf(IntensityPointDataModel(START.plusSeconds(60), 7)),
            longCheckConfirmedAt = null,
            longCheckDisabled = false,
            tags = tags,
            functionalImpact = 2,
            note = "Bright office"
        )

    @Test
    fun `When resolving then every stored detail reaches the domain`() {
        val tags = listOf(
            AttackTagDataModel.Location(HeadLocationDataModel.Left),
            AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Aura),
            AttackTagDataModel.CustomSymptom("s"),
            AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Stress),
            AttackTagDataModel.CustomTrigger("t")
        )
        val custom = listOf(
            CustomTagDataModel("s", CustomTagKindDataModel.Symptom, "Jaw pain", false),
            CustomTagDataModel("t", CustomTagKindDataModel.Trigger, "Perfume", true)
        )

        val actual = classUnderTest.toDomain(attack(tags), custom)

        assertEquals(
            AttackDetailsDomainModel(
                id = "a",
                start = START,
                startZone = ZoneId.of("Asia/Tashkent"),
                end = START.plusSeconds(3_600),
                intensityPoints = listOf(IntensityPointDomainModel(START.plusSeconds(60), 7)),
                locations = setOf(HeadLocationDomainModel.Left),
                symptoms = setOf(
                    SymptomDomainModel.BuiltIn(BuiltInSymptomDomainModel.Aura),
                    SymptomDomainModel.Custom(
                        CustomTagDomainModel("s", TagKindDomainModel.Symptom, "Jaw pain", false)
                    )
                ),
                triggers = setOf(
                    TriggerDomainModel.BuiltIn(BuiltInTriggerDomainModel.Stress),
                    TriggerDomainModel.Custom(
                        CustomTagDomainModel("t", TagKindDomainModel.Trigger, "Perfume", true)
                    )
                ),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office"
            ),
            actual
        )
    }

    @Test
    fun `Given a custom tag that cannot be found when resolving then it is left out`() {
        val actual = classUnderTest
            .toDomain(attack(listOf(AttackTagDataModel.CustomSymptom("gone"))), emptyList())

        assertEquals(emptySet<SymptomDomainModel>(), actual.symptoms)
    }

    @Test
    fun `Given an unknown zone id when resolving then falls back to UTC`() {
        assertEquals(
            ZoneOffset.UTC,
            classUnderTest
                .toDomain(attack(emptyList(), zone = "Mars/Base"), emptyList()).startZone
        )
    }
}
