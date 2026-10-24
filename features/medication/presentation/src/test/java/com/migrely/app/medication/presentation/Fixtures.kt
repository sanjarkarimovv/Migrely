package com.migrely.app.medication.presentation

import com.migrely.app.medication.domain.model.IntakeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.presentation.model.IntakePresentationModel
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel
import java.time.Instant
import java.time.ZoneId

val FIXTURE_ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
val FIXTURE_TIME: Instant = Instant.parse("2026-10-24T09:20:00Z")

val IBUPROFEN_DOMAIN = MedicationDomainModel(
    "m",
    "Ibuprofen",
    MedicationKindDomainModel.Acute,
    MedicationCategoryDomainModel.SimplePainkiller,
    "400 mg",
    archived = false
)

val IBUPROFEN = MedicationPresentationModel(
    "m",
    "Ibuprofen",
    MedicationKindPresentationModel.Acute,
    MedicationCategoryPresentationModel.SimplePainkiller,
    "400 mg"
)

val INTAKE_DOMAIN =
    IntakeDomainModel("i", "m", "Ibuprofen", "400 mg", FIXTURE_TIME, FIXTURE_ZONE, "a")

val INTAKE = IntakePresentationModel("i", "Ibuprofen", "400 mg", FIXTURE_TIME, FIXTURE_ZONE, "a")
