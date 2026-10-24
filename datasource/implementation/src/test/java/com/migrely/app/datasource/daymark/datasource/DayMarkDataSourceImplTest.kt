package com.migrely.app.datasource.daymark.datasource

import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.daymark.dao.DayMarkDao
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-24T09:20:00Z")

class DayMarkDataSourceImplTest {
    private val dayMarkDao: DayMarkDao = mock()
    private val classUnderTest = DayMarkDataSourceImpl(
        dayMarkDao,
        DayMarkLocalMapper(),
        Clock.fixed(NOW, ZoneOffset.UTC)
    )

    @Test
    fun `When reading a range then passes ISO dates and skips unreadable rows`() {
        given(dayMarkDao.between("2026-10-01", "2026-10-31")).willReturn(
            listOf(
                DayMarkLocalModel("2026-10-14", period = true, painFree = false, 1L),
                DayMarkLocalModel("not a date", period = true, painFree = true, 1L)
            )
        )

        assertEquals(
            listOf(DayMarkDataModel(LocalDate.of(2026, 10, 14), period = true, painFree = false)),
            classUnderTest.marksBetween(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))
        )
    }

    @Test
    fun `When setting a period mark then changes only the period flag at the current time`() {
        classUnderTest.setPeriod(LocalDate.of(2026, 10, 14), marked = true)

        val change = argumentCaptor<(DayMarkLocalModel) -> DayMarkLocalModel>()
        verify(dayMarkDao).update(eq("2026-10-14"), eq(NOW.toEpochMilli()), change.capture())
        assertEquals(
            DayMarkLocalModel("2026-10-14", period = true, painFree = true, 5L),
            change.firstValue(DayMarkLocalModel("2026-10-14", period = false, painFree = true, 5L))
        )
    }

    @Test
    fun `When removing a pain-free mark then changes only the pain-free flag`() {
        classUnderTest.setPainFree(LocalDate.of(2026, 10, 6), marked = false)

        val change = argumentCaptor<(DayMarkLocalModel) -> DayMarkLocalModel>()
        verify(dayMarkDao).update(eq("2026-10-06"), eq(NOW.toEpochMilli()), change.capture())
        assertEquals(
            DayMarkLocalModel("2026-10-06", period = true, painFree = false, 5L),
            change.firstValue(DayMarkLocalModel("2026-10-06", period = true, painFree = true, 5L))
        )
    }

    @Test(expected = StorageFullDataException::class)
    fun `Given storage is full when setting a mark then StorageFull`() {
        given(dayMarkDao.update(any(), any(), any())).willThrow(SQLiteFullException())

        classUnderTest.setPeriod(LocalDate.of(2026, 10, 14), marked = true)
    }
}
