package com.migrely.app.datasource.doctorvisit.datasource

import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.doctorvisit.dao.DoctorVisitDao
import com.migrely.app.datasource.doctorvisit.mapper.DoctorVisitLocalMapper
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitLocalModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitsDataModel
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val TODAY: LocalDate = LocalDate.of(2026, 11, 10)

/** Doctor visits over Room (BRD D168, design D2); the queries themselves are tested on a device (DoctorVisitDaoTest). */
class DoctorVisitDataSourceImplTest {
    private val doctorVisitDao: DoctorVisitDao = mock()
    private val classUnderTest =
        DoctorVisitDataSourceImpl(doctorVisitDao, DoctorVisitLocalMapper()) { "new-id" }

    @Test
    fun `When the visits are read then the next and the last by today`() {
        given(
            doctorVisitDao.next("2026-11-10")
        ).willReturn(DoctorVisitLocalModel("v", "2026-11-20"))
        given(doctorVisitDao.lastDate("2026-11-10")).willReturn("2026-08-20")

        assertEquals(
            DoctorVisitsDataModel(
                DoctorVisitDataModel("v", LocalDate.of(2026, 11, 20)),
                LocalDate.of(2026, 8, 20)
            ),
            classUnderTest.visits(TODAY)
        )
    }

    @Test
    fun `Given a row whose date cannot be read when the visits are read then it is left out`() {
        given(doctorVisitDao.next(any())).willReturn(DoctorVisitLocalModel("v", "20.11.2026"))

        assertEquals(DoctorVisitsDataModel(null, null), classUnderTest.visits(TODAY))
    }

    @Test
    fun `When a next visit is set then it replaces every upcoming visit under a new ID`() {
        classUnderTest.setNextVisit(LocalDate.of(2026, 11, 20), TODAY)

        verify(
            doctorVisitDao
        ).replaceUpcoming(DoctorVisitLocalModel("new-id", "2026-11-20"), "2026-11-10")
    }

    @Test
    fun `When a removed visit is restored then it comes back with its own ID`() {
        classUnderTest.restoreNextVisit(
            DoctorVisitDataModel("v", LocalDate.of(2026, 11, 20)),
            TODAY
        )

        verify(
            doctorVisitDao
        ).replaceUpcoming(DoctorVisitLocalModel("v", "2026-11-20"), "2026-11-10")
    }

    @Test
    fun `When the next visit is removed then the removed one is returned, or null`() {
        given(
            doctorVisitDao.removeUpcoming("2026-11-10")
        ).willReturn(DoctorVisitLocalModel("v", "2026-11-20"))
        assertEquals(
            DoctorVisitDataModel("v", LocalDate.of(2026, 11, 20)),
            classUnderTest.removeNextVisit(TODAY)
        )

        given(doctorVisitDao.removeUpcoming("2026-11-10")).willReturn(null)
        assertNull(classUnderTest.removeNextVisit(TODAY))
    }

    @Test
    fun `When asked whether anything was logged then the times and dates go to the query`() {
        given(
            doctorVisitDao.anythingLogged(1_000L, 2_000L, "2026-08-20", "2026-11-18")
        ).willReturn(true)

        assertTrue(
            classUnderTest.anythingLoggedBetween(
                Instant.ofEpochMilli(1_000L),
                Instant.ofEpochMilli(2_000L),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 11, 18)
            )
        )
    }

    @Test
    fun `Given a full phone when a visit is set then a storage failure`() {
        given(doctorVisitDao.replaceUpcoming(any(), any())).willThrow(SQLiteFullException())

        assertThrows(StorageFullDataException::class.java) {
            classUnderTest.setNextVisit(LocalDate.of(2026, 11, 20), TODAY)
        }
    }
}
