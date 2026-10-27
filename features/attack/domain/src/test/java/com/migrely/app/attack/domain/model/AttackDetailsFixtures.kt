package com.migrely.app.attack.domain.model

import java.time.Instant
import java.time.ZoneId

val FIXTURE_START: Instant = Instant.parse("2026-10-23T09:00:00Z")
val FIXTURE_NOW: Instant = Instant.parse("2026-10-23T15:00:00Z")
val FIXTURE_ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

@Suppress("LongParameterList")
fun attackDetails(
    id: String = "a",
    start: Instant = FIXTURE_START,
    end: Instant? = FIXTURE_START.plusSeconds(3 * 3_600),
    intensityPoints: List<IntensityPointDomainModel> = emptyList(),
    startZone: ZoneId = FIXTURE_ZONE,
    functionalImpact: FunctionalImpactDomainModel? = null,
    note: String = ""
) = AttackDetailsDomainModel(
    id = id,
    start = start,
    startZone = startZone,
    end = end,
    intensityPoints = intensityPoints,
    locations = emptySet(),
    symptoms = emptySet(),
    triggers = emptySet(),
    functionalImpact = functionalImpact,
    note = note
)

fun customTag(
    id: String = "t",
    kind: TagKindDomainModel = TagKindDomainModel.Symptom,
    name: String = "Jaw pain",
    archived: Boolean = false
) = CustomTagDomainModel(id = id, kind = kind, name = name, archived = archived)
