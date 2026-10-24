package com.migrely.app.history.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.DeletedAttackDataModel
import com.migrely.app.datasource.attack.model.EarliestAttackDataModel
import com.migrely.app.datasource.attack.model.RestoreDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.history.data.mapper.DayMarkDomainMapper
import com.migrely.app.history.data.mapper.HistoryAttackDomainMapper
import com.migrely.app.history.data.mapper.ZoneDomainMapper
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DayMarkWriteResultDomainModel
import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-23T07:00:00Z")
private val FROM: Instant = Instant.parse("2026-09-28T19:00:00Z")
private val TO: Instant = Instant.parse("2026-11-02T19:00:00Z")
private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

class HistoryRepositoryTest {
    private val attackDataSource: AttackDataSource = mock()
    private val clockDataSource: ClockDataSource = mock()
    private val dayMarkDataSource: DayMarkDataSource = mock()
    private val classUnderTest = HistoryRepository(
        attackDataSource,
        clockDataSource,
        dayMarkDataSource,
        HistoryAttackDomainMapper(ZoneDomainMapper()),
        ZoneDomainMapper(),
        DayMarkDomainMapper()
    )

    @Test
    fun `When reading the current time then the clock's instant and zone`() {
        given(clockDataSource.now()).willReturn(NOW)
        given(clockDataSource.zone()).willReturn(TASHKENT)

        assertEquals(CurrentTimeDomainModel(NOW, TASHKENT), classUnderTest.currentTime())
    }

    @Test
    fun `Given an attack when reading the earliest then found with its zone`() {
        given(attackDataSource.earliestStart())
            .willReturn(EarliestAttackDataModel(NOW, "Asia/Tokyo"))

        assertEquals(
            EarliestAttackResultDomainModel.Found(NOW, ZoneId.of("Asia/Tokyo")),
            classUnderTest.earliestAttack()
        )
    }

    @Test
    fun `Given no attack when reading the earliest then none`() {
        assertEquals(EarliestAttackResultDomainModel.None, classUnderTest.earliestAttack())
    }

    @Test
    fun `Given the database fails when reading the earliest then failed`() {
        whenever(attackDataSource.earliestStart()).thenThrow(DatabaseDataException())

        assertEquals(EarliestAttackResultDomainModel.Failed, classUnderTest.earliestAttack())
    }

    @Test
    fun `When reading a range then every attack the overlap read returns, mapped`() {
        given(attackDataSource.overlapping(null, FROM, TO)).willReturn(
            listOf(
                AttackDataModel("a", NOW, "Asia/Tashkent", null, emptyList(), null, false)
            )
        )

        assertEquals(
            AttacksTouchingResultDomainModel.Loaded(
                listOf(HistoryAttackDomainModel("a", NOW, TASHKENT, null, null))
            ),
            classUnderTest.attacksTouching(FROM, TO)
        )
    }

    @Test
    fun `Given the database fails when reading a range then failed`() {
        whenever(attackDataSource.overlapping(null, FROM, TO)).thenThrow(DatabaseDataException())

        assertEquals(
            AttacksTouchingResultDomainModel.Failed,
            classUnderTest.attacksTouching(FROM, TO)
        )
    }

    @Test
    fun `Given a restorable deletion when reading it then returned`() {
        given(attackDataSource.recentlyDeleted(Instant.EPOCH))
            .willReturn(DeletedAttackDataModel("gone", NOW))

        assertEquals(
            DeletedAttackDomainModel("gone", NOW),
            classUnderTest.recentlyDeletedAttack()
        )
    }

    @Test
    fun `Given the database fails when reading a deletion then none is offered`() {
        whenever(attackDataSource.recentlyDeleted(Instant.EPOCH)).thenThrow(DatabaseDataException())

        assertNull(classUnderTest.recentlyDeletedAttack())
    }

    @Test
    fun `Given the attack can be restored when restoring then undone`() {
        given(attackDataSource.restore("gone")).willReturn(RestoreDataModel.Restored)

        assertEquals(UndoResultDomainModel.Undone, classUnderTest.restoreDeletedAttack("gone"))
    }

    @Test
    fun `Given restoring would overlap when restoring then not possible`() {
        given(attackDataSource.restore("gone")).willReturn(RestoreDataModel.NotPossible)

        assertEquals(UndoResultDomainModel.NotPossible, classUnderTest.restoreDeletedAttack("gone"))
    }

    @Test
    fun `Given the database fails when restoring then failed`() {
        whenever(attackDataSource.restore("gone")).thenThrow(DatabaseDataException())

        assertEquals(UndoResultDomainModel.Failed, classUnderTest.restoreDeletedAttack("gone"))
    }

    @Test
    fun `When reading day marks then maps them`() {
        val from = LocalDate.parse("2026-10-01")
        val to = LocalDate.parse("2026-10-31")
        given(dayMarkDataSource.marksBetween(from, to)).willReturn(
            listOf(DayMarkDataModel(LocalDate.parse("2026-10-14"), period = true, painFree = false))
        )

        assertEquals(
            DayMarksResultDomainModel.Loaded(
                listOf(
                    DayMarkDomainModel(
                        LocalDate.parse("2026-10-14"),
                        period = true,
                        painFree = false
                    )
                )
            ),
            classUnderTest.dayMarks(from, to)
        )
    }

    @Test
    fun `Given the read fails when reading day marks then failed`() {
        given(dayMarkDataSource.marksBetween(any(), any())).willThrow(DatabaseDataException())

        assertEquals(
            DayMarksResultDomainModel.Failed,
            classUnderTest.dayMarks(LocalDate.MIN, LocalDate.MAX)
        )
    }

    @Test
    fun `When setting each kind of mark then writes that mark`() {
        val date = LocalDate.parse("2026-10-06")

        assertEquals(
            DayMarkWriteResultDomainModel.Written,
            classUnderTest.setDayMark(date, DayMarkKindDomainModel.Period, true)
        )
        assertEquals(
            DayMarkWriteResultDomainModel.Written,
            classUnderTest.setDayMark(date, DayMarkKindDomainModel.PainFree, false)
        )

        verify(dayMarkDataSource).setPeriod(date, true)
        verify(dayMarkDataSource).setPainFree(date, false)
    }

    @Test
    fun `Given storage is full when setting a mark then storage full`() {
        val date = LocalDate.parse("2026-10-06")
        given(dayMarkDataSource.setPeriod(date, true)).willThrow(StorageFullDataException())

        assertEquals(
            DayMarkWriteResultDomainModel.StorageFull,
            classUnderTest.setDayMark(date, DayMarkKindDomainModel.Period, true)
        )
    }
}
