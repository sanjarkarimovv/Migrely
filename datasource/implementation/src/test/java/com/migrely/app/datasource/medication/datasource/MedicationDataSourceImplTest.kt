package com.migrely.app.datasource.medication.datasource

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.mapper.OveruseNoticeLocalMapper
import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.AcuteIntakeLocalModel
import com.migrely.app.datasource.medication.model.EarliestAcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.EarliestAcuteIntakeLocalModel
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionLocalModel
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
        overuseNoticeLocalMapper = OveruseNoticeLocalMapper(),
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
    fun `When reading an attack's intakes then maps instants, names and answers`() {
        given(medicationDao.intakesOfAttack("a")).willReturn(
            listOf(
                IntakeWithMedicationLocalModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "a",
                    NOW.toEpochMilli(),
                    "UTC",
                    "400 mg",
                    effect = "A_LOT",
                    effectClosed = true
                ),
                IntakeWithMedicationLocalModel(
                    "j",
                    "m",
                    "Ibuprofen",
                    "a",
                    NOW.toEpochMilli(),
                    "UTC",
                    null,
                    effect = "SOMETHING_NEWER",
                    effectClosed = false
                )
            )
        )

        assertEquals(
            listOf(
                IntakeDataModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "a",
                    NOW,
                    "UTC",
                    "400 mg",
                    IntakeEffectDataModel.ALot,
                    effectClosed = true
                ),
                IntakeDataModel("j", "m", "Ibuprofen", "a", NOW, "UTC", null)
            ),
            classUnderTest.intakesOfAttack("a")
        )
    }

    @Test
    fun `When restoring an intake then its answer is stored again`() {
        classUnderTest.restoreIntake(
            IntakeDataModel(
                "i",
                "m",
                "Ibuprofen",
                null,
                NOW,
                "UTC",
                null,
                IntakeEffectDataModel.NotAtAll,
                effectClosed = true
            )
        )

        verify(medicationDao).insertIntake(
            MedicationIntakeLocalModel(
                id = "i",
                medicationId = "m",
                attackId = null,
                takenEpochMillis = NOW.toEpochMilli(),
                takenZoneId = "UTC",
                dose = null,
                createdEpochMillis = NOW.toEpochMilli(),
                effect = "NOT_AT_ALL",
                effectClosed = true
            )
        )
    }

    @Test
    fun `When reading the pending question then passes the window and maps it`() {
        given(medicationDao.pendingEffectQuestion(1_000L, 2_000L)).willReturn(
            PendingEffectQuestionLocalModel("i", "Ibuprofen", "400 mg", 1_500L, "UTC")
        )

        assertEquals(
            PendingEffectQuestionDataModel(
                "i",
                "Ibuprofen",
                "400 mg",
                Instant.ofEpochMilli(1_500L),
                "UTC"
            ),
            classUnderTest.pendingEffectQuestion(
                Instant.ofEpochMilli(1_000L),
                Instant.ofEpochMilli(2_000L)
            )
        )
    }

    @Test
    fun `When setting or clearing an answer then stores its key`() {
        classUnderTest.setIntakeEffect("i", IntakeEffectDataModel.Completely)
        classUnderTest.setIntakeEffect("j", null)

        verify(medicationDao).setEffect("i", "COMPLETELY")
        verify(medicationDao).setEffect("j", null)
    }

    @Test
    fun `When skipping a question then closes it`() {
        classUnderTest.closeEffectQuestion("i")

        verify(medicationDao).closeEffectQuestion("i")
    }

    @Test
    fun `When reading acute intakes then maps categories with Not sure as null`() {
        given(medicationDao.acuteIntakesBetween(1_000L, 2_000L)).willReturn(
            listOf(
                AcuteIntakeLocalModel(1_100L, "UTC", "TRIPTAN"),
                AcuteIntakeLocalModel(1_200L, "Asia/Tokyo", null)
            )
        )

        assertEquals(
            listOf(
                AcuteIntakeDataModel(
                    Instant.ofEpochMilli(1_100L),
                    "UTC",
                    MedicationCategoryDataModel.Triptan
                ),
                AcuteIntakeDataModel(Instant.ofEpochMilli(1_200L), "Asia/Tokyo", null)
            ),
            classUnderTest.acuteIntakesTakenBetween(
                Instant.ofEpochMilli(1_000L),
                Instant.ofEpochMilli(2_000L)
            )
        )
    }

    @Test
    fun `When reading the earliest acute intake then its instant and zone`() {
        given(medicationDao.earliestAcuteIntake())
            .willReturn(EarliestAcuteIntakeLocalModel(1_100L, "Asia/Tokyo"))

        assertEquals(
            EarliestAcuteIntakeDataModel(Instant.ofEpochMilli(1_100L), "Asia/Tokyo"),
            classUnderTest.earliestAcuteIntake()
        )
    }

    @Test
    fun `Given no acute intake when reading the earliest then null`() {
        assertNull(classUnderTest.earliestAcuteIntake())
    }

    @Test(expected = DatabaseDataException::class)
    fun `Given SQLite fails when reading the earliest intake then a database failure`() {
        whenever(medicationDao.earliestAcuteIntake()).thenThrow(SQLiteException())

        classUnderTest.earliestAcuteIntake()
    }

    @Test
    fun `When reading notices then skips keys this version does not know`() {
        given(medicationDao.overuseNotices()).willReturn(
            listOf(
                OveruseNoticeLocalModel("TRIPTAN", "DOCTOR", 5_000L),
                OveruseNoticeLocalModel("OPIOID", "DOCTOR", 5_000L),
                OveruseNoticeLocalModel("MULTIPLE", "URGENT", 5_000L)
            )
        )

        assertEquals(
            listOf(
                OveruseNoticeDataModel(
                    OveruseCategoryDataModel.Triptan,
                    OveruseLevelDataModel.Doctor,
                    Instant.ofEpochMilli(5_000L)
                )
            ),
            classUnderTest.overuseNotices()
        )
    }

    @Test
    fun `When acknowledging a warning then upserts its notice`() {
        classUnderTest.acknowledgeOveruse(
            OveruseCategoryDataModel.Multiple,
            OveruseLevelDataModel.Soft,
            Instant.ofEpochMilli(7_000L)
        )

        verify(
            medicationDao
        ).upsertOveruseNotice(OveruseNoticeLocalModel("MULTIPLE", "SOFT", 7_000L))
    }
}
