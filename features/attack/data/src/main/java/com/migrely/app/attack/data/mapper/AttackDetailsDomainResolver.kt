package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagDataModel
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Builds an attack's details from the stored attack and the custom tags it references. A custom tag that cannot be
 * found is left out rather than shown without a name.
 */
class AttackDetailsDomainResolver(
    private val headLocationDomainMapper: HeadLocationDomainMapper,
    private val builtInSymptomDomainMapper: BuiltInSymptomDomainMapper,
    private val builtInTriggerDomainMapper: BuiltInTriggerDomainMapper,
    private val customTagDomainMapper: CustomTagDomainMapper,
    private val functionalImpactDomainMapper: FunctionalImpactDomainMapper
) {
    fun toDomain(
        attack: AttackDataModel,
        customTags: List<CustomTagDataModel>
    ): AttackDetailsDomainModel {
        val tagsById = customTags.associateBy(CustomTagDataModel::id)
        val locations = mutableSetOf<HeadLocationDomainModel>()
        val symptoms = mutableSetOf<SymptomDomainModel>()
        val triggers = mutableSetOf<TriggerDomainModel>()
        attack.tags.forEach { tag ->
            when (tag) {
                is AttackTagDataModel.Location ->
                    locations += headLocationDomainMapper
                        .toDomain(tag.location)
                is AttackTagDataModel.BuiltInSymptom ->
                    symptoms += SymptomDomainModel.BuiltIn(
                        builtInSymptomDomainMapper
                            .toDomain(tag.symptom)
                    )

                is AttackTagDataModel.BuiltInTrigger ->
                    triggers += TriggerDomainModel.BuiltIn(
                        builtInTriggerDomainMapper
                            .toDomain(tag.trigger)
                    )

                is AttackTagDataModel.CustomSymptom -> tagsById[tag.tagId]?.let { custom ->
                    symptoms += SymptomDomainModel.Custom(customTagDomainMapper.toDomain(custom))
                }

                is AttackTagDataModel.CustomTrigger -> tagsById[tag.tagId]?.let { custom ->
                    triggers += TriggerDomainModel.Custom(customTagDomainMapper.toDomain(custom))
                }
            }
        }
        return AttackDetailsDomainModel(
            id = attack.id,
            start = attack.start,
            startZone = zoneOf(attack.startZoneId),
            end = attack.end,
            intensityPoints = attack.intensityPoints.map {
                IntensityPointDomainModel(it.at, it.value)
            },
            locations = locations,
            symptoms = symptoms,
            triggers = triggers,
            functionalImpact = attack.functionalImpact?.let(functionalImpactDomainMapper::toDomain),
            note = attack.note.orEmpty()
        )
    }

    // A zone id the device no longer knows falls back to UTC, as on the home screen.
    private fun zoneOf(id: String): ZoneId = try {
        ZoneId.of(id)
    } catch (_: DateTimeException) {
        ZoneOffset.UTC
    }
}
