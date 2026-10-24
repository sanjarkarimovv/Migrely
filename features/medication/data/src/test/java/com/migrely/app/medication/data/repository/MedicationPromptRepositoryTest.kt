package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
import com.migrely.app.medication.data.mapper.AcuteIntakeDomainMapper
import com.migrely.app.medication.data.mapper.IntakeEffectDataMapper
import com.migrely.app.medication.data.mapper.OveruseDataMapper
import com.migrely.app.medication.data.mapper.OveruseNoticeDomainMapper
import com.migrely.app.medication.data.mapper.PendingEffectQuestionDomainMapper
import com.migrely.app.medication.data.mapper.ZoneDomainMapper
import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticeDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticesResultDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val FROM: Instant = Instant.parse("2026-10-30T11:00:00Z")
private val TO: Instant = Instant.parse("2026-10-31T09:00:00Z")

class MedicationPromptRepositoryTest {
    private val medicationDataSource: MedicationDataSource = mock()
    private val zoneDomainMapper = ZoneDomainMapper()
    private val classUnderTest = MedicationPromptRepository(
        medicationDataSource,
        PendingEffectQuestionDomainMapper(zoneDomainMapper),
        AcuteIntakeDomainMapper(zoneDomainMapper),
        OveruseNoticeDomainMapper(),
        IntakeEffectDataMapper(),
        OveruseDataMapper()
    )

    @Test
    fun `Given a pending question when reading then found with its zone`() {
        given(medicationDataSource.pendingEffectQuestion(FROM, TO)).willReturn(
            PendingEffectQuestionDataModel("i", "Ibuprofen", "400 mg", FROM, "Asia/Tokyo")
        )

        assertEquals(
            PendingEffectQuestionResultDomainModel.Found(
                PendingEffectQuestionDomainModel(
                    "i",
                    "Ibuprofen",
                    "400 mg",
                    FROM,
                    ZoneId.of("Asia/Tokyo")
                )
            ),
            classUnderTest.pendingEffectQuestion(FROM, TO)
        )
    }

    @Test
    fun `Given no pending question when reading then None`() {
        assertEquals(
            PendingEffectQuestionResultDomainModel.None,
            classUnderTest.pendingEffectQuestion(FROM, TO)
        )
    }

    @Test
    fun `Given the read fails when reading the question then Failed`() {
        given(medicationDataSource.pendingEffectQuestion(FROM, TO))
            .willThrow(DatabaseDataException())

        assertEquals(
            PendingEffectQuestionResultDomainModel.Failed,
            classUnderTest.pendingEffectQuestion(FROM, TO)
        )
    }

    @Test
    fun `When reading acute intakes then maps categories and unknown zones to UTC`() {
        given(medicationDataSource.acuteIntakesTakenBetween(FROM, TO)).willReturn(
            listOf(
                AcuteIntakeDataModel(FROM, "Asia/Tashkent", MedicationCategoryDataModel.Triptan),
                AcuteIntakeDataModel(TO, "Mars/Base", null)
            )
        )

        assertEquals(
            AcuteIntakesResultDomainModel.Loaded(
                listOf(
                    AcuteIntakeDomainModel(
                        FROM,
                        ZoneId.of("Asia/Tashkent"),
                        MedicationCategoryDomainModel.Triptan
                    ),
                    AcuteIntakeDomainModel(TO, ZoneOffset.UTC, null)
                )
            ),
            classUnderTest.acuteIntakesTakenBetween(FROM, TO)
        )
    }

    @Test
    fun `Given the read fails when reading acute intakes then Failed`() {
        given(medicationDataSource.acuteIntakesTakenBetween(FROM, TO))
            .willThrow(DatabaseDataException())

        assertEquals(
            AcuteIntakesResultDomainModel.Failed,
            classUnderTest.acuteIntakesTakenBetween(FROM, TO)
        )
    }

    @Test
    fun `When reading notices then maps them`() {
        given(medicationDataSource.overuseNotices()).willReturn(
            listOf(
                OveruseNoticeDataModel(
                    OveruseCategoryDataModel.Multiple,
                    OveruseLevelDataModel.Soft,
                    FROM
                )
            )
        )

        assertEquals(
            OveruseNoticesResultDomainModel.Loaded(
                listOf(
                    OveruseNoticeDomainModel(
                        OveruseCategoryDomainModel.Multiple,
                        OveruseLevelDomainModel.Soft,
                        FROM
                    )
                )
            ),
            classUnderTest.overuseNotices()
        )
    }

    @Test
    fun `Given the read fails when reading notices then Failed`() {
        given(medicationDataSource.overuseNotices()).willThrow(DatabaseDataException())

        assertEquals(OveruseNoticesResultDomainModel.Failed, classUnderTest.overuseNotices())
    }

    @Test
    fun `When answering then stores the answer`() {
        assertEquals(
            WriteResultDomainModel.Done,
            classUnderTest.setIntakeEffect("i", IntakeEffectDomainModel.ALittle)
        )
        verify(medicationDataSource).setIntakeEffect("i", IntakeEffectDataModel.ALittle)
    }

    @Test
    fun `Given storage is full when skipping then StorageFull`() {
        given(medicationDataSource.closeEffectQuestion("i")).willThrow(StorageFullDataException())

        assertEquals(WriteResultDomainModel.StorageFull, classUnderTest.closeEffectQuestion("i"))
    }

    @Test
    fun `When acknowledging then stores the category, level and time`() {
        assertEquals(
            WriteResultDomainModel.Done,
            classUnderTest.acknowledgeOveruse(
                OveruseCategoryDomainModel.CombinationOrOther,
                OveruseLevelDomainModel.Doctor,
                TO
            )
        )
        verify(medicationDataSource).acknowledgeOveruse(
            OveruseCategoryDataModel.CombinationOrOther,
            OveruseLevelDataModel.Doctor,
            TO
        )
    }

    @Test
    fun `Given the write fails when acknowledging then Failed`() {
        given(
            medicationDataSource.acknowledgeOveruse(
                OveruseCategoryDataModel.Triptan,
                OveruseLevelDataModel.Soft,
                TO
            )
        ).willThrow(DatabaseDataException())

        assertEquals(
            WriteResultDomainModel.Failed,
            classUnderTest.acknowledgeOveruse(
                OveruseCategoryDomainModel.Triptan,
                OveruseLevelDomainModel.Soft,
                TO
            )
        )
    }
}
