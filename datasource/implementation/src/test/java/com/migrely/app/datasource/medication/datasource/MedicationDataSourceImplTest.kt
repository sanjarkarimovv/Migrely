package com.migrely.app.datasource.medication.datasource

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-24T09:20:00Z")

class MedicationDataSourceImplTest {
    private val medicationDao: MedicationDao = mock()
    private val classUnderTest = MedicationDataSourceImpl(
        medicationDao = medicationDao,
        medicationLocalMapper = MedicationLocalMapper(),
        clock = Clock.fixed(NOW, ZoneOffset.UTC),
        generateId = { "new" }
    )

    private fun local(kind: String = "ACUTE", category: String? = "TRIPTAN") = MedicationLocalModel(
        "m",
        "Sumatriptan",
        "sumatriptan",
        kind,
        category,
        "50 mg",
        archived = false,
        createdEpochMillis = 1L,
        updatedEpochMillis = 1L
    )

    @Test
    fun `When listing active medications then maps them in the dao's order`() {
        given(medicationDao.activeByLastUse()).willReturn(listOf(local()))

        assertEquals(
            listOf(
                MedicationDataModel(
                    "m",
                    "Sumatriptan",
                    MedicationKindDataModel.Acute,
                    MedicationCategoryDataModel.Triptan,
                    "50 mg",
                    archived = false
                )
            ),
            classUnderTest.activeMedications()
        )
    }

    @Test
    fun `Given unknown stored keys when reading then acute and not sure`() {
        given(medicationDao.byId("m")).willReturn(local(kind = "FUTURE", category = "FUTURE"))

        val actual = checkNotNull(classUnderTest.medication("m"))

        assertEquals(MedicationKindDataModel.Acute, actual.kind)
        assertNull(actual.category)
    }

    @Test
    fun `When creating then stores stable keys and the clock's time`() {
        val actual = classUnderTest.createMedication(
            "Topiramate",
            "topiramate",
            MedicationKindDataModel.Preventive,
            null,
            null
        )

        verify(medicationDao).insert(
            MedicationLocalModel(
                "new",
                "Topiramate",
                "topiramate",
                "PREVENTIVE",
                null,
                null,
                archived = false,
                createdEpochMillis = NOW.toEpochMilli(),
                updatedEpochMillis = NOW.toEpochMilli()
            )
        )
        assertEquals("new", actual.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a 61-character name when creating then a programming error`() {
        classUnderTest.createMedication(
            "x".repeat(61),
            "x",
            MedicationKindDataModel.Acute,
            null,
            null
        )
    }

    @Test(expected = StorageFullDataException::class)
    fun `Given the disk is full when creating then storage full`() {
        whenever(medicationDao.insert(any())).thenThrow(SQLiteFullException())

        classUnderTest.createMedication(
            "Ibuprofen",
            "ibuprofen",
            MedicationKindDataModel.Acute,
            null,
            null
        )
    }

    @Test
    fun `Given no such medication when updating then null`() {
        given(
            medicationDao.update(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any())
        ).willReturn(0)

        assertNull(
            classUnderTest.updateMedication(
                "missing",
                "Ibuprofen",
                "ibuprofen",
                MedicationKindDataModel.Acute,
                MedicationCategoryDataModel.SimplePainkiller,
                null
            )
        )
    }

    @Test
    fun `When updating then writes the keys and returns the stored medication`() {
        given(
            medicationDao.update(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any())
        ).willReturn(1)
        given(medicationDao.byId("m")).willReturn(local())

        val actual = classUnderTest.updateMedication(
            "m",
            "Sumatriptan",
            "sumatriptan",
            MedicationKindDataModel.Acute,
            MedicationCategoryDataModel.Triptan,
            "50 mg"
        )

        verify(medicationDao).update(
            "m",
            "Sumatriptan",
            "sumatriptan",
            "ACUTE",
            "TRIPTAN",
            "50 mg",
            NOW.toEpochMilli()
        )
        assertEquals("m", actual?.id)
    }

    @Test
    fun `When adding an intake then stores it and returns its id`() {
        val actual = classUnderTest.addIntake("m", "a", NOW, "Asia/Tashkent", "400 mg")

        assertEquals("new", actual)
        verify(medicationDao).insertIntake(
            MedicationIntakeLocalModel(
                "new",
                "m",
                "a",
                NOW.toEpochMilli(),
                "Asia/Tashkent",
                "400 mg",
                NOW.toEpochMilli()
            )
        )
    }

    @Test
    fun `When restoring an intake then stores it with its own id`() {
        classUnderTest.restoreIntake(IntakeDataModel("i", "m", "Ibuprofen", null, NOW, "UTC", null))

        verify(medicationDao).insertIntake(
            MedicationIntakeLocalModel(
                "i",
                "m",
                null,
                NOW.toEpochMilli(),
                "UTC",
                null,
                NOW.toEpochMilli()
            )
        )
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given a constraint failure when adding an intake then a database failure`() {
        whenever(medicationDao.insertIntake(any())).thenThrow(SQLiteConstraintException())

        classUnderTest.addIntake("missing", null, NOW, "UTC", null)
    }

    @Test
    fun `When reading an attack's intakes then maps instants and names`() {
        given(medicationDao.intakesOfAttack("a")).willReturn(
            listOf(
                IntakeWithMedicationLocalModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "a",
                    NOW.toEpochMilli(),
                    "UTC",
                    "400 mg"
                )
            )
        )

        assertEquals(
            listOf(IntakeDataModel("i", "m", "Ibuprofen", "a", NOW, "UTC", "400 mg")),
            classUnderTest.intakesOfAttack("a")
        )
    }
}
