package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.medication.data.mapper.LinkedAttackDomainMapper
import com.migrely.app.medication.data.mapper.MedicationCategoryDataMapper
import com.migrely.app.medication.data.mapper.MedicationDomainMapper
import com.migrely.app.medication.data.mapper.MedicationKindDataMapper
import com.migrely.app.medication.data.mapper.ZoneDomainMapper
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-24T09:20:00Z")
private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

private val DATA = MedicationDataModel(
    "m",
    "Sumatriptan",
    MedicationKindDataModel.Acute,
    MedicationCategoryDataModel.Triptan,
    "50 mg",
    archived = false
)
private val DOMAIN = MedicationDomainModel(
    "m",
    "Sumatriptan",
    MedicationKindDomainModel.Acute,
    MedicationCategoryDomainModel.Triptan,
    "50 mg",
    archived = false
)

class MedicationRepositoryTest {
    private val medicationDataSource: MedicationDataSource = mock()
    private val attackDataSource: AttackDataSource = mock()
    private val clockDataSource: ClockDataSource = mock()
    private val classUnderTest = MedicationRepository(
        medicationDataSource,
        attackDataSource,
        clockDataSource,
        MedicationDomainMapper(),
        MedicationKindDataMapper(),
        MedicationCategoryDataMapper(),
        LinkedAttackDomainMapper(ZoneDomainMapper())
    )

    private fun attack(end: Instant?) =
        AttackDataModel("a", NOW, "Asia/Tashkent", end, emptyList(), null, false)

    @Test
    fun `When reading the time then the clock's instant and zone`() {
        given(clockDataSource.now()).willReturn(NOW)
        given(clockDataSource.zone()).willReturn(ZONE)

        assertEquals(CurrentTimeDomainModel(NOW, ZONE), classUnderTest.currentTime())
    }

    @Test
    fun `When listing medications then mapped, or failed on a database error`() {
        given(medicationDataSource.activeMedications()).willReturn(listOf(DATA))
        assertEquals(
            MedicationsResultDomainModel.Loaded(listOf(DOMAIN)),
            classUnderTest.activeMedications()
        )

        whenever(medicationDataSource.activeMedications()).thenThrow(DatabaseDataException())
        assertEquals(MedicationsResultDomainModel.Failed, classUnderTest.activeMedications())
    }

    @Test
    fun `When looking up medications then found, not found or failed`() {
        given(medicationDataSource.medication("m")).willReturn(DATA)
        given(medicationDataSource.medicationByNameKey("x")).willReturn(null)

        assertEquals(
            MedicationLookupResultDomainModel.Found(DOMAIN),
            classUnderTest.medication("m")
        )
        assertEquals(
            MedicationLookupResultDomainModel.NotFound,
            classUnderTest.medicationByNameKey("x")
        )

        whenever(medicationDataSource.medication("m")).thenThrow(DatabaseDataException())
        assertEquals(MedicationLookupResultDomainModel.Failed, classUnderTest.medication("m"))
    }

    @Test
    fun `When counting then counted or failed`() {
        given(medicationDataSource.activeMedicationCount()).willReturn(4)
        assertEquals(
            ActiveCountResultDomainModel.Counted(4),
            classUnderTest.activeMedicationCount()
        )

        whenever(medicationDataSource.activeMedicationCount()).thenThrow(DatabaseDataException())
        assertEquals(ActiveCountResultDomainModel.Failed, classUnderTest.activeMedicationCount())
    }

    @Test
    fun `When creating then maps kind and category to the datasource`() {
        given(
            medicationDataSource.createMedication(
                "Sumatriptan",
                "sumatriptan",
                MedicationKindDataModel.Acute,
                MedicationCategoryDataModel.Triptan,
                "50 mg"
            )
        ).willReturn(DATA)

        assertEquals(
            MedicationWriteResultDomainModel.Done(DOMAIN),
            classUnderTest.createMedication(
                "Sumatriptan",
                "sumatriptan",
                MedicationKindDomainModel.Acute,
                MedicationCategoryDomainModel.Triptan,
                "50 mg"
            )
        )
    }

    @Test
    fun `Given the disk is full when creating then storage full`() {
        whenever(
            medicationDataSource.createMedication(
                "Topiramate",
                "topiramate",
                MedicationKindDataModel.Preventive,
                null,
                null
            )
        ).thenThrow(StorageFullDataException())

        assertEquals(
            MedicationWriteResultDomainModel.StorageFull,
            classUnderTest.createMedication(
                "Topiramate",
                "topiramate",
                MedicationKindDomainModel.Preventive,
                null,
                null
            )
        )
    }

    @Test
    fun `Given the medication vanished when updating then failed`() {
        given(
            medicationDataSource.updateMedication(
                "m",
                "Sumatriptan",
                "sumatriptan",
                MedicationKindDataModel.Acute,
                MedicationCategoryDataModel.SimplePainkiller,
                null
            )
        ).willReturn(null)

        assertEquals(
            MedicationWriteResultDomainModel.Failed,
            classUnderTest.updateMedication(
                "m",
                "Sumatriptan",
                "sumatriptan",
                MedicationKindDomainModel.Acute,
                MedicationCategoryDomainModel.SimplePainkiller,
                null
            )
        )
    }

    @Test
    fun `When archiving then done, or failed on a database error`() {
        assertEquals(WriteResultDomainModel.Done, classUnderTest.setMedicationArchived("m", true))

        whenever(medicationDataSource.setArchived("m", false)).thenThrow(DatabaseDataException())
        assertEquals(
            WriteResultDomainModel.Failed,
            classUnderTest.setMedicationArchived("m", false)
        )
    }

    @Test
    fun `When looking up an attack then found with its times, or none`() {
        given(attackDataSource.byId("a")).willReturn(attack(end = null))

        assertEquals(
            AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("a", NOW, null, ZONE)),
            classUnderTest.attack("a")
        )
        assertEquals(AttackLookupResultDomainModel.None, classUnderTest.attack("other"))
    }

    @Test
    fun `When finding the attack during a minute then the one overlapping it`() {
        val minuteLater = NOW.plusSeconds(60)
        given(
            attackDataSource.overlapping(null, NOW, minuteLater)
        ).willReturn(listOf(attack(NOW)))

        assertEquals(
            AttackLookupResultDomainModel.Found(LinkedAttackDomainModel("a", NOW, NOW, ZONE)),
            classUnderTest.attackDuring(NOW, minuteLater)
        )
    }

    @Test
    fun `Given the database fails when finding the attack during a minute then failed`() {
        whenever(
            attackDataSource.overlapping(null, NOW, NOW.plusSeconds(60))
        ).thenThrow(DatabaseDataException())

        assertEquals(
            AttackLookupResultDomainModel.Failed,
            classUnderTest.attackDuring(NOW, NOW.plusSeconds(60))
        )
    }

    @Test
    fun `When adding an intake then added with its id, storage full or failed`() {
        given(
            medicationDataSource.addIntake("m", "a", NOW, "Asia/Tashkent", "50 mg")
        ).willReturn("i")
        assertEquals(
            AddIntakeResultDomainModel.Added("i"),
            classUnderTest.addIntake("m", "a", NOW, ZONE, "50 mg")
        )

        whenever(medicationDataSource.addIntake("m", null, NOW, "Asia/Tashkent", null))
            .thenThrow(StorageFullDataException())
        assertEquals(
            AddIntakeResultDomainModel.StorageFull,
            classUnderTest.addIntake("m", null, NOW, ZONE, null)
        )
    }

    @Test
    fun `When deleting an intake then done, or failed on a database error`() {
        assertEquals(WriteResultDomainModel.Done, classUnderTest.deleteIntake("i"))

        whenever(medicationDataSource.deleteIntake("x")).thenThrow(DatabaseDataException())
        assertEquals(WriteResultDomainModel.Failed, classUnderTest.deleteIntake("x"))
    }
}
