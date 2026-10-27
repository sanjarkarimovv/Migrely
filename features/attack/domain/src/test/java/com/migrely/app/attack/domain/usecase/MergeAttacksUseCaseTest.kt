package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackMergeDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksRequestDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.MergeAttacksRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val NOW: Instant = at("18:00")
private val DEVICE_ZONE: ZoneId = ZoneId.of("Europe/London")

private fun at(time: String): Instant = Instant.parse("2026-10-23T$time:00Z")

class MergeAttacksUseCaseTest {
    private lateinit var classUnderTest: MergeAttacksUseCase
    private val getAttackDetailsRepository: GetAttackDetailsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val overlaps = FakeOverlaps()
    private val storePastAttackRepository: StorePastAttackRepository = mock()
    private val updateAttackTimesRepository: UpdateAttackTimesRepository = mock()
    private val mergeAttacksRepository: MergeAttacksRepository = mock()
    private val updateAttackNoteRepository: UpdateAttackNoteRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = MergeAttacksUseCase(
            getAttackDetailsRepository,
            getCurrentTimeRepository,
            overlaps,
            storePastAttackRepository,
            updateAttackTimesRepository,
            mergeAttacksRepository,
            updateAttackNoteRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(NOW, DEVICE_ZONE))
        given(mergeAttacksRepository.mergeAttacks(any())).willReturn(WriteResultDomainModel.Written)
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.Stored("new"))
        given(updateAttackTimesRepository.updateAttackTimes(any(), any(), anyOrNull(), any()))
            .willReturn(WriteResultDomainModel.Written)
        given(updateAttackNoteRepository.updateAttackNote(any(), any()))
            .willReturn(WriteResultDomainModel.Written)
    }

    private fun givenStored(vararg attacks: AttackDetailsDomainModel) {
        overlaps.attacks = attacks.toList()
        attacks.forEach { attack ->
            given(getAttackDetailsRepository.attackDetails(attack.id))
                .willReturn(AttackDetailsLookupResultDomainModel.Found(attack))
        }
    }

    private fun merge(
        attackId: String?,
        start: Instant,
        end: Instant?,
        editedNote: String? = null
    ) = classUnderTest.executeInBackground(
        MergeAttacksRequestDomainModel(attackId, start, end, editedNote)
    )

    private fun written(): AttackMergeDomainModel {
        val captor = argumentCaptor<AttackMergeDomainModel>()
        verify(mergeAttacksRepository).mergeAttacks(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `Given a new attack over one attack when merging then that attack grows to cover both`() {
        val point = IntensityPointDomainModel(at("12:30"), 6)
        givenStored(
            attackDetails(
                "a",
                at("12:00"),
                at("13:30"),
                listOf(point),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office"
            )
        )

        val actual = merge(null, at("13:00"), at("14:30"))

        assertEquals(MergeAttacksResultDomainModel.Merged("a"), actual)
        assertEquals(
            AttackMergeDomainModel(
                survivorId = "a",
                absorbedIds = emptyList(),
                start = at("12:00"),
                startZone = FIXTURE_ZONE,
                end = at("14:30"),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office"
            ),
            written()
        )
    }

    @Test
    fun `Given a new attack over two attacks when merging then the earliest survives with both`() {
        givenStored(
            attackDetails(
                "b",
                at("12:00"),
                at("13:30"),
                functionalImpact = FunctionalImpactDomainModel.BedRest,
                note = "Skipped lunch"
            ),
            attackDetails(
                "a",
                at("09:00"),
                at("10:00"),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office\n"
            )
        )

        merge(null, at("08:00"), at("14:00"))

        assertEquals(
            AttackMergeDomainModel(
                survivorId = "a",
                absorbedIds = listOf("b"),
                start = at("08:00"),
                // The new attack's start is the earliest, so the device's zone wins.
                startZone = DEVICE_ZONE,
                end = at("14:00"),
                functionalImpact = FunctionalImpactDomainModel.BedRest,
                note = "Bright office\n\nSkipped lunch"
            ),
            written()
        )
    }

    @Test
    fun `Given an edited attack when merging then it survives and takes the earliest zone`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        givenStored(
            attackDetails("edited", at("14:00"), at("16:00"), note = "Mine"),
            attackDetails("other", at("11:00"), at("13:00"), startZone = tokyo)
        )

        val actual = merge("edited", at("12:00"), at("16:00"))

        assertEquals(MergeAttacksResultDomainModel.Merged("edited"), actual)
        assertEquals(
            AttackMergeDomainModel(
                survivorId = "edited",
                absorbedIds = listOf("other"),
                start = at("11:00"),
                startZone = tokyo,
                end = at("16:00"),
                functionalImpact = null,
                note = "Mine"
            ),
            written()
        )
    }

    @Test
    fun `Given an ended attack over the one in progress when merging then it stays in progress`() {
        givenStored(
            attackDetails("edited", at("10:00"), at("11:00")),
            attackDetails("open", at("11:30"), null)
        )

        merge("edited", at("10:00"), at("12:00"))

        val merge = written()
        assertEquals("edited", merge.survivorId)
        assertEquals(listOf("open"), merge.absorbedIds)
        assertEquals(at("10:00"), merge.start)
        assertEquals(null, merge.end)
    }

    @Test
    fun `Given the edited attack is in progress when merging then its end stays empty`() {
        givenStored(
            attackDetails("open", at("11:30"), null),
            attackDetails("earlier", at("09:00"), at("10:30"))
        )

        merge("open", at("10:00"), NOW.plusSeconds(3_600))

        assertEquals(null, written().end)
        assertEquals(at("09:00"), written().start)
    }

    @Test
    fun `Given a pain entry outside its attack when merging then the range still holds it`() {
        givenStored(
            attackDetails(
                "a",
                at("12:00"),
                at("13:30"),
                listOf(IntensityPointDomainModel(at("14:40"), 4))
            )
        )

        merge(null, at("13:00"), at("14:00"))

        assertEquals(at("14:40"), written().end)
    }

    @Test
    fun `Given the range reaches another attack when merging then it grows until stable`() {
        // Stored attacks never overlap, but a merge must still settle if they did.
        givenStored(
            attackDetails("a", at("11:00"), at("13:00")),
            attackDetails("b", at("12:30"), at("14:00"))
        )

        merge(null, at("10:00"), at("11:30"))

        assertEquals(listOf("b"), written().absorbedIds)
        assertEquals(at("14:00"), written().end)
    }

    @Test
    fun `Given the range never settles when merging then failed`() {
        overlaps.endless = true

        assertEquals(
            MergeAttacksResultDomainModel.Failed,
            merge(null, at("10:00"), at("11:00"))
        )
        verify(mergeAttacksRepository, never()).mergeAttacks(any())
    }

    @Test
    fun `Given long notes when merging then cut at 1,000 characters`() {
        givenStored(
            attackDetails("a", at("09:00"), at("10:00"), note = "a".repeat(700)),
            attackDetails("b", at("11:00"), at("12:00"), note = "b".repeat(700))
        )

        merge(null, at("09:30"), at("11:30"))

        assertEquals("a".repeat(700) + "\n\n" + "b".repeat(298), written().note)
    }

    @Test
    fun `Given the cut falls inside an emoji when merging then the whole emoji is left out`() {
        givenStored(
            attackDetails("a", at("09:00"), at("10:00"), note = "a".repeat(997)),
            attackDetails("b", at("11:00"), at("12:00"), note = "😀 later")
        )

        merge(null, at("09:30"), at("11:30"))

        assertEquals("a".repeat(997), written().note)
    }

    @Test
    fun `Given blank notes when merging then they are skipped`() {
        givenStored(
            attackDetails("a", at("09:00"), at("10:00"), note = "  "),
            attackDetails("b", at("11:00"), at("12:00"), note = "Only one")
        )

        merge(null, at("09:30"), at("11:30"))

        assertEquals("Only one", written().note)
    }

    @Test
    fun `Given nothing overlaps a new attack any more when merging then it is stored as picked`() {
        val actual = merge(null, at("13:00"), at("14:00"))

        assertEquals(MergeAttacksResultDomainModel.Saved("new"), actual)
        verify(storePastAttackRepository).storePastAttack(at("13:00"), DEVICE_ZONE, at("14:00"))
        verify(mergeAttacksRepository, never()).mergeAttacks(any())
    }

    @Test
    fun `Given nothing overlaps an edited attack any more when merging then it never shrinks`() {
        givenStored(attackDetails("edited", at("10:00"), at("16:00")))

        val actual = merge("edited", at("11:00"), at("16:00"))

        assertEquals(MergeAttacksResultDomainModel.Saved("edited"), actual)
        verify(updateAttackTimesRepository).updateAttackTimes(
            "edited",
            at("10:00"),
            at("16:00"),
            emptyList()
        )
    }

    @Test
    fun `Given a neighbour overlapping by seconds when merging then it is not absorbed`() {
        givenStored(
            attackDetails("touching", at("11:00"), at("13:00").plusSeconds(30)),
            attackDetails("overlapping", at("14:00"), at("14:30"))
        )

        merge(null, at("13:00"), at("14:15"))

        val merge = written()
        assertEquals("overlapping", merge.survivorId)
        assertEquals(emptyList<String>(), merge.absorbedIds)
        assertEquals(at("13:00").plusSeconds(30), merge.start)
    }

    @Test
    fun `Given the edited attack is gone when merging then not found`() {
        given(getAttackDetailsRepository.attackDetails("gone"))
            .willReturn(AttackDetailsLookupResultDomainModel.NotFound)

        assertEquals(
            MergeAttacksResultDomainModel.NotFound,
            merge("gone", at("10:00"), at("11:00"))
        )
    }

    @Test
    fun `Given an end beyond the tolerance when merging then in the future`() {
        assertEquals(
            MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.End),
            merge(null, at("14:00"), NOW.plusSeconds(360))
        )
    }

    @Test
    fun `Given a start 2 minutes after now when merging then the start is in the future`() {
        assertEquals(
            MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            merge(null, NOW.plusSeconds(120), NOW.plusSeconds(240))
        )
    }

    @Test
    fun `Given a start beyond the tolerance when merging then the start is in the future`() {
        assertEquals(
            MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            merge(null, NOW.plusSeconds(600), NOW.plusSeconds(900))
        )
    }

    @Test
    fun `Given an end not after the start when merging then end before start`() {
        assertEquals(
            MergeAttacksResultDomainModel.EndBeforeStart,
            merge(null, at("14:00"), at("14:00"))
        )
    }

    @Test
    fun `Given a new attack without an end when merging then failed`() {
        assertEquals(MergeAttacksResultDomainModel.Failed, merge(null, at("14:00"), null))
    }

    @Test
    fun `Given storage is full when merging then storage full`() {
        givenStored(attackDetails("a", at("12:00"), at("13:30")))
        given(mergeAttacksRepository.mergeAttacks(any()))
            .willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(
            MergeAttacksResultDomainModel.StorageFull,
            merge(null, at("13:00"), at("14:00"))
        )
    }

    @Test
    fun `Given the merge fails when merging then failed`() {
        givenStored(attackDetails("a", at("12:00"), at("13:30")))
        given(mergeAttacksRepository.mergeAttacks(any())).willReturn(WriteResultDomainModel.Failed)

        assertEquals(MergeAttacksResultDomainModel.Failed, merge(null, at("13:00"), at("14:00")))
    }

    @Test
    fun `Given the lookup fails when merging then failed`() {
        overlaps.failing = true

        assertEquals(MergeAttacksResultDomainModel.Failed, merge(null, at("13:00"), at("14:00")))
    }

    @Test
    fun `Given another attack was stored in between when saving then failed`() {
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.Overlapping)

        assertEquals(MergeAttacksResultDomainModel.Failed, merge(null, at("13:00"), at("14:00")))
    }

    @Test
    fun `Given three long notes when merging then the joined text is cut from its end`() {
        givenStored(
            attackDetails("a", at("09:00"), at("10:00"), note = "a".repeat(600)),
            attackDetails("b", at("11:00"), at("12:00"), note = "b".repeat(600)),
            attackDetails("c", at("13:00"), at("14:00"), note = "c".repeat(10))
        )

        merge(null, at("09:30"), at("13:30"))

        assertEquals("a".repeat(600) + "\n\n" + "b".repeat(398), written().note)
    }

    @Test
    fun `Given only one note when merging then it is kept exactly`() {
        givenStored(
            attackDetails("a", at("09:00"), at("10:00"), note = "Bright office\n"),
            attackDetails("b", at("11:00"), at("12:00"))
        )

        merge(null, at("09:30"), at("11:30"))

        assertEquals("Bright office\n", written().note)
    }

    @Test
    fun `Given a note typed on the edited attack when merging then it is stored and joined`() {
        givenStored(
            attackDetails("other", at("11:00"), at("13:00"), note = "Bright office"),
            attackDetails("edited", at("14:00"), at("16:00"), note = "Took")
        )

        merge("edited", at("12:00"), at("16:00"), editedNote = "Took ibuprofen")

        verify(updateAttackNoteRepository).updateAttackNote("edited", "Took ibuprofen")
        assertEquals("Bright office\n\nTook ibuprofen", written().note)
    }

    @Test
    fun `Given the note on screen is already stored when merging then it is not written again`() {
        givenStored(
            attackDetails("other", at("11:00"), at("13:00")),
            attackDetails("edited", at("14:00"), at("16:00"), note = "Took")
        )

        merge("edited", at("12:00"), at("16:00"), editedNote = "Took")

        verify(updateAttackNoteRepository, never()).updateAttackNote(any(), any())
        assertEquals("Took", written().note)
    }

    @Test
    fun `Given the disk is full when storing the note on screen then storage full and no merge`() {
        givenStored(
            attackDetails("other", at("11:00"), at("13:00")),
            attackDetails("edited", at("14:00"), at("16:00"))
        )
        given(updateAttackNoteRepository.updateAttackNote(any(), any()))
            .willReturn(WriteResultDomainModel.StorageFull)

        val actual = merge("edited", at("12:00"), at("16:00"), editedNote = "Took ibuprofen")

        assertEquals(MergeAttacksResultDomainModel.StorageFull, actual)
        verify(mergeAttacksRepository, never()).mergeAttacks(any())
    }

    @Test
    fun `Given the same merge again when merging then the merged attack stays as it is`() {
        // After a first merge of 13:00 to 14:30 into 12:00 to 13:30, only the grown attack is left.
        givenStored(
            attackDetails(
                "a",
                at("12:00"),
                at("14:30"),
                listOf(IntensityPointDomainModel(at("12:30"), 6)),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office"
            )
        )

        val actual = merge(null, at("13:00"), at("14:30"))

        assertEquals(MergeAttacksResultDomainModel.Merged("a"), actual)
        assertEquals(
            AttackMergeDomainModel(
                survivorId = "a",
                absorbedIds = emptyList(),
                start = at("12:00"),
                startZone = FIXTURE_ZONE,
                end = at("14:30"),
                functionalImpact = FunctionalImpactDomainModel.HadToStop,
                note = "Bright office"
            ),
            written()
        )
    }

    /** The overlap rule of the datasource: in progress is open-ended, touching does not count. */
    private class FakeOverlaps : FindOverlappingAttacksRepository {
        var attacks: List<AttackDetailsDomainModel> = emptyList()
        var endless = false
        var failing = false
        private var lookups = 0

        override fun overlappingAttacks(
            excludingId: String?,
            start: Instant,
            end: Instant
        ): OverlapLookupResultDomainModel {
            lookups++
            if (failing) return OverlapLookupResultDomainModel.Failed
            if (endless) {
                return OverlapLookupResultDomainModel.Found(
                    listOf(attackDetails("x$lookups", start, end))
                )
            }
            val found = attacks
                .filter { it.id != excludingId }
                .filter { it.start < end && (it.end == null || it.end!! > start) }
                .sortedBy(AttackDetailsDomainModel::start)
            return if (found.isEmpty()) {
                OverlapLookupResultDomainModel.None
            } else {
                OverlapLookupResultDomainModel.Found(found)
            }
        }
    }
}
