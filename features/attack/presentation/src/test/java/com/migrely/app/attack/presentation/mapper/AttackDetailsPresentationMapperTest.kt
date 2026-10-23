package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsScreenDomainModel
import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel
import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel
import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.presentation.model.SymptomPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.presentation.model.TriggerPresentationModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")

class AttackDetailsPresentationMapperTest {
    private val classUnderTest = AttackDetailsPresentationMapper(
        HeadLocationPresentationMapper(),
        BuiltInSymptomPresentationMapper(),
        BuiltInTriggerPresentationMapper(),
        CustomTagPresentationMapper(TagKindPresentationMapper()),
        FunctionalImpactPresentationMapper(),
        IntensityPointPresentationMapper()
    )
    private val active = CustomTagDomainModel(
        "active",
        TagKindDomainModel.Symptom,
        "Ear pain",
        false
    )
    private val archived = CustomTagDomainModel("old", TagKindDomainModel.Symptom, "Jaw pain", true)

    private fun screen() = AttackDetailsScreenDomainModel(
        details = AttackDetailsDomainModel(
            id = "a",
            start = START,
            startZone = ZoneId.of("Asia/Tashkent"),
            end = START.plusSeconds(7_200),
            intensityPoints = listOf(
                IntensityPointDomainModel(START.plusSeconds(3_600), 8),
                IntensityPointDomainModel(START.plusSeconds(600), 5)
            ),
            locations = setOf(HeadLocationDomainModel.Left),
            symptoms = setOf(
                SymptomDomainModel.BuiltIn(BuiltInSymptomDomainModel.Aura),
                SymptomDomainModel.Custom(archived)
            ),
            triggers = emptySet(),
            functionalImpact = FunctionalImpactDomainModel.BedRest,
            note = "Note"
        ),
        customSymptoms = listOf(active),
        customTriggers = emptyList(),
        now = START.plusSeconds(9_000)
    )

    @Test
    fun `When mapping then points are in time order and the peak is the highest`() {
        val actual = classUnderTest.toPresentation(screen())

        assertEquals(
            listOf(
                IntensityPointPresentationModel(START.plusSeconds(600), 5),
                IntensityPointPresentationModel(START.plusSeconds(3_600), 8)
            ),
            actual.intensityPoints
        )
        assertEquals(8, actual.peakIntensity)
        assertEquals(setOf(HeadLocationPresentationModel.Left), actual.selectedLocations)
        assertEquals(FunctionalImpactPresentationModel.BedRest, actual.functionalImpact)
    }

    @Test
    fun `When mapping then choices hold built-ins, active tags and archived tags used here`() {
        val actual = classUnderTest.toPresentation(screen())

        val expectedCustom = listOf(
            CustomTagPresentationModel(
                "active",
                TagKindPresentationModel.Symptom,
                "Ear pain",
                false
            ),
            CustomTagPresentationModel("old", TagKindPresentationModel.Symptom, "Jaw pain", true)
        )
        assertEquals(
            BuiltInSymptomPresentationModel.entries.map(SymptomPresentationModel::BuiltIn) +
                expectedCustom.map(SymptomPresentationModel::Custom),
            actual.symptomChoices
        )
        assertEquals(
            setOf(
                SymptomPresentationModel.BuiltIn(BuiltInSymptomPresentationModel.Aura),
                SymptomPresentationModel.Custom(expectedCustom[1])
            ),
            actual.selectedSymptoms
        )
    }

    @Test
    fun `Given no custom triggers when mapping then trigger choices are the built-ins`() {
        assertEquals(
            BuiltInTriggerPresentationModel.entries.map(TriggerPresentationModel::BuiltIn),
            classUnderTest.toPresentation(screen()).triggerChoices
        )
    }
}
