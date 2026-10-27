package com.migrely.app.attack.data.repository

import com.migrely.app.attack.data.mapper.AttackDetailsDomainResolver
import com.migrely.app.attack.data.mapper.AttackIntakeDomainMapper
import com.migrely.app.attack.data.mapper.AttackIntakeEffectDataMapper
import com.migrely.app.attack.data.mapper.AttackTagDataMapper
import com.migrely.app.attack.data.mapper.BuiltInSymptomDataMapper
import com.migrely.app.attack.data.mapper.BuiltInSymptomDomainMapper
import com.migrely.app.attack.data.mapper.BuiltInTriggerDataMapper
import com.migrely.app.attack.data.mapper.BuiltInTriggerDomainMapper
import com.migrely.app.attack.data.mapper.CustomTagDomainMapper
import com.migrely.app.attack.data.mapper.FunctionalImpactDomainMapper
import com.migrely.app.attack.data.mapper.HeadLocationDataMapper
import com.migrely.app.attack.data.mapper.HeadLocationDomainMapper
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.AttackIntakeEffectDomainModel
import com.migrely.app.attack.domain.model.AttackMergeDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.CreateEndedDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.tag.datasource.TagDataSource
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")
private val END: Instant = START.plusSeconds(3_600)

class AttackDetailsRepositoryTest {
    private lateinit var classUnderTest: AttackDetailsRepository
    private val attackDataSource: AttackDataSource = mock()
    private val tagDataSource: TagDataSource = mock()
    private val medicationDataSource: MedicationDataSource = mock()
    private val clockDataSource: ClockDataSource = mock()

    @Before
    fun setUp() {
        classUnderTest = AttackDetailsRepository(
            attackDataSource,
            tagDataSource,
            clockDataSource,
            AttackDetailsDomainResolver(
                HeadLocationDomainMapper(),
                BuiltInSymptomDomainMapper(),
                BuiltInTriggerDomainMapper(),
                CustomTagDomainMapper(),
                FunctionalImpactDomainMapper()
            ),
            AttackTagDataMapper(
                HeadLocationDataMapper(),
                BuiltInSymptomDataMapper(),
                BuiltInTriggerDataMapper()
            ),
            medicationDataSource,
            AttackIntakeDomainMapper(),
            AttackIntakeEffectDataMapper()
        )
    }

    private fun stored(tags: List<AttackTagDataModel> = emptyList()) = AttackDataModel(
        id = "a",
        start = START,
        startZoneId = "Asia/Tashkent",
        end = END,
        intensityPoints = emptyList(),
        longCheckConfirmedAt = null,
        longCheckDisabled = false,
        tags = tags
    )

    @Test
    fun `Given a stored attack with a custom tag when reading then resolves only that tag id`() {
        given(attackDataSource.byId("a")).willReturn(
            stored(
                listOf(
                    AttackTagDataModel.Location(HeadLocationDataModel.Forehead),
                    AttackTagDataModel.CustomTrigger("t")
                )
            )
        )
        given(tagDataSource.customTags(listOf("t"))).willReturn(emptyList())

        val actual = classUnderTest.attackDetails("a")

        assertTrue(actual is AttackDetailsLookupResultDomainModel.Found)
        assertEquals(
            setOf(HeadLocationDomainModel.Forehead),
            (actual as AttackDetailsLookupResultDomainModel.Found).details.locations
        )
    }

    @Test
    fun `Given no attack when reading then not found`() {
        assertEquals(
            AttackDetailsLookupResultDomainModel.NotFound,
            classUnderTest.attackDetails("a")
        )
    }

    @Test
    fun `Given the database fails when reading then failed`() {
        whenever(attackDataSource.byId("a")).doThrow(DatabaseDataException(RuntimeException()))

        assertEquals(AttackDetailsLookupResultDomainModel.Failed, classUnderTest.attackDetails("a"))
    }

    @Test
    fun `Given two attacks overlap when checking then returns both with their custom tags`() {
        val first = stored(listOf(AttackTagDataModel.CustomSymptom("t"))).copy(id = "b")
        val second = stored(
            listOf(AttackTagDataModel.CustomSymptom("t"))
        ).copy(id = "c", end = null)
        given(attackDataSource.overlapping("a", START, END)).willReturn(listOf(first, second))
        given(tagDataSource.customTags(listOf("t"))).willReturn(emptyList())

        val actual = classUnderTest.overlappingAttacks("a", START, END)

        assertTrue(actual is OverlapLookupResultDomainModel.Found)
        assertEquals(
            listOf("b" to END, "c" to null),
            (actual as OverlapLookupResultDomainModel.Found).attacks.map { it.id to it.end }
        )
        verify(tagDataSource).customTags(listOf("t"))
    }

    @Test
    fun `Given nothing overlaps when checking then none`() {
        given(attackDataSource.overlapping(null, START, END)).willReturn(emptyList())

        assertEquals(
            OverlapLookupResultDomainModel.None,
            classUnderTest.overlappingAttacks(null, START, END)
        )
    }

    @Test
    fun `Given the overlap check fails when checking then failed`() {
        whenever(attackDataSource.overlapping(anyOrNull(), any(), any())).doThrow(
            DatabaseDataException(RuntimeException())
        )

        assertEquals(
            OverlapLookupResultDomainModel.Failed,
            classUnderTest.overlappingAttacks("a", START, END)
        )
    }

    @Test
    fun `When changing times then passes the points to remove to the datasource`() {
        val point = IntensityPointDomainModel(END, 8)

        val actual = classUnderTest.updateAttackTimes("a", START, END, listOf(point))

        assertEquals(WriteResultDomainModel.Written, actual)
        verify(attackDataSource).updateTimes(
            "a",
            START,
            END,
            listOf(IntensityPointDataModel(END, 8))
        )
    }

    @Test
    fun `Given storage is full when restoring times then storage full`() {
        whenever(attackDataSource.restoreTimes(any(), any(), anyOrNull(), any()))
            .doThrow(StorageFullDataException(RuntimeException()))

        assertEquals(
            WriteResultDomainModel.StorageFull,
            classUnderTest.restoreAttackTimes("a", START, null, emptyList())
        )
    }

    @Test
    fun `Given a 10 exists when checking then recorded`() {
        given(attackDataSource.maximumRecorded()).willReturn(true)

        assertEquals(MaximumRecordedResultDomainModel.Recorded, classUnderTest.maximumRecorded())
    }

    @Test
    fun `Given the check fails when checking for a 10 then failed`() {
        whenever(attackDataSource.maximumRecorded()).doThrow(
            DatabaseDataException(RuntimeException())
        )

        assertEquals(MaximumRecordedResultDomainModel.Failed, classUnderTest.maximumRecorded())
    }

    @Test
    fun `When selecting a location then stores the data tag`() {
        classUnderTest.selectAttackTag(
            "a",
            AttackTagDomainModel.Location(HeadLocationDomainModel.WholeHead),
            true
        )

        verify(attackDataSource).setTag(
            "a",
            AttackTagDataModel.Location(HeadLocationDataModel.WholeHead),
            true
        )
    }

    @Test
    fun `When setting bed rest then stores level 3`() {
        classUnderTest.setFunctionalImpact("a", FunctionalImpactDomainModel.BedRest)

        verify(attackDataSource).setFunctionalImpact("a", 3)
    }

    @Test
    fun `Given the database fails when saving the note then failed`() {
        whenever(attackDataSource.setNote(any(), any())).doThrow(
            DatabaseDataException(RuntimeException())
        )

        assertEquals(WriteResultDomainModel.Failed, classUnderTest.updateAttackNote("a", "note"))
    }

    @Test
    fun `When deleting then deletes softly`() {
        assertEquals(WriteResultDomainModel.Written, classUnderTest.deleteAttack("a"))
        verify(attackDataSource).softDelete("a")
    }

    @Test
    fun `When purging expired deletions then the datasource purges them, and a failure is quiet`() {
        classUnderTest.purgeExpiredDeletions()
        verify(attackDataSource).purgeExpiredDeletions()

        whenever(attackDataSource.purgeExpiredDeletions()).doThrow(StorageFullDataException())
        classUnderTest.purgeExpiredDeletions()
        verify(attackDataSource, times(2)).purgeExpiredDeletions()
    }

    @Test
    fun `When removing a point then passes it to the datasource`() {
        classUnderTest.removeIntensityPoint("a", IntensityPointDomainModel(START, 6))

        verify(attackDataSource).removeIntensityPoint("a", IntensityPointDataModel(START, 6))
    }

    @Test
    fun `Given storage is full when adding a point then storage full`() {
        whenever(attackDataSource.addIntensityPoint(any(), any(), any()))
            .doThrow(StorageFullDataException(RuntimeException()))

        assertEquals(
            WriteResultDomainModel.StorageFull,
            classUnderTest.addIntensityPoint("a", IntensityPointDomainModel(START, 6))
        )
    }

    @Test
    fun `Given nothing overlaps when storing a past attack then stored`() {
        given(attackDataSource.createEnded(START, "Asia/Tashkent", END))
            .willReturn(CreateEndedDataModel.Created("new"))

        assertEquals(
            StorePastAttackResultDomainModel.Stored("new"),
            classUnderTest.storePastAttack(START, ZoneId.of("Asia/Tashkent"), END)
        )
    }

    @Test
    fun `Given an overlap appeared when storing a past attack then overlapping`() {
        given(attackDataSource.createEnded(any(), any(), any()))
            .willReturn(CreateEndedDataModel.Overlapping)

        assertEquals(
            StorePastAttackResultDomainModel.Overlapping,
            classUnderTest.storePastAttack(START, ZoneId.of("UTC"), END)
        )
    }

    @Test
    fun `Given the disk is full when storing a past attack then storage full`() {
        whenever(attackDataSource.createEnded(any(), any(), any()))
            .doThrow(StorageFullDataException(RuntimeException()))

        assertEquals(
            StorePastAttackResultDomainModel.StorageFull,
            classUnderTest.storePastAttack(START, ZoneId.of("UTC"), END)
        )
    }

    @Test
    fun `Given the database fails when storing a past attack then failed`() {
        whenever(attackDataSource.createEnded(any(), any(), any()))
            .doThrow(DatabaseDataException(RuntimeException()))

        assertEquals(
            StorePastAttackResultDomainModel.Failed,
            classUnderTest.storePastAttack(START, ZoneId.of("UTC"), END)
        )
    }

    @Test
    fun `When merging then passes the levels and zone id to the datasource`() {
        val actual = classUnderTest.mergeAttacks(
            AttackMergeDomainModel(
                survivorId = "a",
                absorbedIds = listOf("b"),
                start = START,
                startZone = ZoneId.of("Asia/Tokyo"),
                end = null,
                functionalImpact = FunctionalImpactDomainModel.BedRest,
                note = "Note"
            )
        )

        assertEquals(WriteResultDomainModel.Written, actual)
        verify(attackDataSource).merge("a", listOf("b"), START, "Asia/Tokyo", null, 3, "Note")
    }

    @Test
    fun `Given the disk is full when merging then storage full`() {
        whenever(
            attackDataSource.merge(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any())
        ).doThrow(StorageFullDataException(RuntimeException()))

        assertEquals(
            WriteResultDomainModel.StorageFull,
            classUnderTest.mergeAttacks(
                AttackMergeDomainModel("a", emptyList(), START, ZoneId.of("UTC"), END, null, "")
            )
        )
    }

    @Test
    fun `Given intakes when reading details then they come with the attack, oldest first`() {
        val at = Instant.parse("2026-10-24T09:20:00Z")
        given(attackDataSource.byId("intake-attack")).willReturn(
            AttackDataModel(
                "intake-attack",
                at,
                "Asia/Tashkent",
                null,
                emptyList(),
                null,
                false
            )
        )
        given(medicationDataSource.intakesOfAttack("intake-attack")).willReturn(
            listOf(
                IntakeDataModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "intake-attack",
                    at,
                    "Mars/Base",
                    "400 mg",
                    IntakeEffectDataModel.ALittle,
                    effectClosed = true
                )
            )
        )

        val actual = classUnderTest.attackDetails("intake-attack")
            as AttackDetailsLookupResultDomainModel.Found

        assertEquals(
            listOf(
                AttackIntakeDomainModel(
                    "i",
                    "m",
                    "Ibuprofen",
                    "400 mg",
                    at,
                    ZoneOffset.UTC,
                    AttackIntakeEffectDomainModel.ALittle,
                    effectQuestionClosed = true
                )
            ),
            actual.details.intakes
        )
    }

    @Test
    fun `When removing and restoring an intake then deleted and stored again for the attack`() {
        val at = Instant.parse("2026-10-24T09:20:00Z")
        val intake =
            AttackIntakeDomainModel("i", "m", "Ibuprofen", null, at, ZoneId.of("Asia/Tashkent"))

        assertEquals(WriteResultDomainModel.Written, classUnderTest.removeIntake("i"))
        assertEquals(WriteResultDomainModel.Written, classUnderTest.restoreIntake("a", intake))

        verify(medicationDataSource).deleteIntake("i")
        verify(medicationDataSource).restoreIntake(
            IntakeDataModel("i", "m", "Ibuprofen", "a", at, "Asia/Tashkent", null)
        )
    }

    @Test
    fun `When restoring an answered intake then its answer stays and the question closed`() {
        val at = Instant.parse("2026-10-24T09:20:00Z")
        val intake = AttackIntakeDomainModel(
            "i",
            "m",
            "Ibuprofen",
            null,
            at,
            ZoneId.of("Asia/Tashkent"),
            AttackIntakeEffectDomainModel.ALot
        )

        classUnderTest.restoreIntake("a", intake)

        verify(medicationDataSource).restoreIntake(
            IntakeDataModel(
                "i",
                "m",
                "Ibuprofen",
                "a",
                at,
                "Asia/Tashkent",
                null,
                IntakeEffectDataModel.ALot,
                effectClosed = true
            )
        )
    }

    @Test
    fun `When restoring a skipped intake then its question stays closed`() {
        val at = Instant.parse("2026-10-24T09:20:00Z")
        val intake = AttackIntakeDomainModel(
            "i",
            "m",
            "Ibuprofen",
            null,
            at,
            ZoneId.of("Asia/Tashkent"),
            effect = null,
            effectQuestionClosed = true
        )

        classUnderTest.restoreIntake("a", intake)

        verify(medicationDataSource).restoreIntake(
            IntakeDataModel(
                "i",
                "m",
                "Ibuprofen",
                "a",
                at,
                "Asia/Tashkent",
                null,
                effectClosed = true
            )
        )
    }

    @Test
    fun `When setting an intake's answer then stores it`() {
        assertEquals(
            WriteResultDomainModel.Written,
            classUnderTest.setIntakeEffect("i", AttackIntakeEffectDomainModel.NotAtAll)
        )

        verify(medicationDataSource).setIntakeEffect("i", IntakeEffectDataModel.NotAtAll)
    }

    @Test
    fun `Given storage is full when setting an answer then StorageFull`() {
        given(medicationDataSource.setIntakeEffect("i", IntakeEffectDataModel.Completely))
            .willThrow(StorageFullDataException())

        assertEquals(
            WriteResultDomainModel.StorageFull,
            classUnderTest.setIntakeEffect("i", AttackIntakeEffectDomainModel.Completely)
        )
    }
}
