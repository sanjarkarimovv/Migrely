package com.migrely.app.datasource.doctorvisit.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TODAY = "2026-11-10"

/** Doctor visit queries (change add-doctor-visit-mode, design D2). */
@RunWith(AndroidJUnit4::class)
class DoctorVisitDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: DoctorVisitDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.doctorVisitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun dates() = database.query("SELECT date FROM doctor_visit ORDER BY date", null).use {
        buildList { while (it.moveToNext()) add(it.getString(0)) }
    }

    @Test
    fun givenPastAndUpcomingVisits_whenRead_thenTheFirstUpcomingAndTheLatestPast() {
        listOf("2026-05-01", "2026-08-20", "2026-11-20").forEachIndexed { index, date ->
            classUnderTest.insert(DoctorVisitLocalModel("v$index", date))
        }

        assertEquals(DoctorVisitLocalModel("v2", "2026-11-20"), classUnderTest.next(TODAY))
        assertEquals("2026-08-20", classUnderTest.lastDate(TODAY))
    }

    @Test
    fun givenAVisitToday_whenRead_thenItIsNextAndTheOneBeforeIsLast() {
        classUnderTest.insert(DoctorVisitLocalModel("a", "2026-08-20"))
        classUnderTest.insert(DoctorVisitLocalModel("b", TODAY))

        assertEquals("b", classUnderTest.next(TODAY)?.id)
        assertEquals("2026-08-20", classUnderTest.lastDate(TODAY))
    }

    @Test
    fun givenTwoUpcomingFromAnOlderState_whenANextVisitIsSaved_thenOnlyItIsUpcoming() {
        classUnderTest.insert(DoctorVisitLocalModel("old1", "2026-11-20"))
        classUnderTest.insert(DoctorVisitLocalModel("old2", "2026-12-01"))
        classUnderTest.insert(DoctorVisitLocalModel("past", "2026-08-20"))

        classUnderTest.replaceUpcoming(DoctorVisitLocalModel("new", "2026-11-27"), TODAY)

        assertEquals(listOf("2026-08-20", "2026-11-27"), dates())
    }

    @Test
    fun givenAVisitToday_whenALaterDateIsSaved_thenTodaysStaysAndTheLaterIsNext() {
        classUnderTest.insert(DoctorVisitLocalModel("today", TODAY))

        classUnderTest.replaceUpcoming(DoctorVisitLocalModel("later", "2026-11-27"), TODAY)

        assertEquals(listOf(TODAY, "2026-11-27"), dates())
        assertEquals("later", classUnderTest.next(TODAY)?.id)
        // From the next day on, today's visit is the last one, which the report starts from.
        assertEquals(TODAY, classUnderTest.lastDate("2026-11-11"))
    }

    @Test
    fun givenOnlyAVisitToday_whenRemoved_thenItStaysAsTheVisitOfToday() {
        classUnderTest.insert(DoctorVisitLocalModel("today", TODAY))

        assertNull(classUnderTest.removeUpcoming(TODAY))

        assertEquals(listOf(TODAY), dates())
        assertEquals("today", classUnderTest.next(TODAY)?.id)
    }

    @Test
    fun givenAVisitTodayAndALaterOne_whenRemoved_thenOnlyTheLaterGoes() {
        classUnderTest.insert(DoctorVisitLocalModel("today", TODAY))
        classUnderTest.insert(DoctorVisitLocalModel("later", "2026-11-27"))

        assertEquals("later", classUnderTest.removeUpcoming(TODAY)?.id)

        assertEquals(listOf(TODAY), dates())
    }

    @Test
    fun givenTheClockSetBackToAVisitDay_whenANewDateIsSaved_thenThatVisitStays() {
        // The visit was on November 20; the clock now reads November 20 again.
        classUnderTest.insert(DoctorVisitLocalModel("past", "2026-11-20"))

        classUnderTest.replaceUpcoming(DoctorVisitLocalModel("new", "2026-11-27"), "2026-11-20")

        assertEquals(listOf("2026-11-20", "2026-11-27"), dates())
    }

    @Test
    fun givenANextVisit_whenAPastVisitIsPutBack_thenTheNextOneStays() {
        classUnderTest.insert(DoctorVisitLocalModel("next", "2026-11-27"))

        classUnderTest.replaceUpcoming(DoctorVisitLocalModel("past", "2026-11-08"), TODAY)

        assertEquals(listOf("2026-11-08", "2026-11-27"), dates())
    }

    @Test
    fun givenTwoUpcoming_whenTheNextIsRemoved_thenBothGoAndTheFirstIsReturned() {
        classUnderTest.insert(DoctorVisitLocalModel("old1", "2026-11-20"))
        classUnderTest.insert(DoctorVisitLocalModel("old2", "2026-12-01"))

        assertEquals("old1", classUnderTest.removeUpcoming(TODAY)?.id)
        assertEquals(emptyList<String>(), dates())
        assertNull(classUnderTest.removeUpcoming(TODAY))
    }

    @Test
    fun givenEachKindOfEntry_whenAskedWhetherAnythingIsLogged_thenOnlyThoseInThePeriodCount() {
        // The period: 1000 to 2000 ms, and its dates 2026-08-20 to 2026-11-10.
        fun logged() = classUnderTest.anythingLogged(1_000L, 2_000L, "2026-08-20", TODAY)
        assertFalse(logged())

        database.dayMarkDao().upsert(
            DayMarkLocalModel("2026-08-19", period = false, painFree = true, 1L)
        )
        database.dayMarkDao().upsert(
            DayMarkLocalModel("2026-09-01", period = true, painFree = false, 1L)
        )
        assertFalse("a mark before the period, and a period mark, do not count", logged())

        database.dayMarkDao().upsert(
            DayMarkLocalModel("2026-09-02", period = false, painFree = true, 1L)
        )
        assertTrue(logged())
    }

    @Test
    fun givenAttacksAndIntakes_whenAskedWhetherAnythingIsLogged_thenTheyCountUnlessBeingDeleted() {
        fun logged() = classUnderTest.anythingLogged(1_000L, 2_000L, "2026-08-20", TODAY)
        database.attackDao().insertAttack(attack("deleted", 1_500L, 1_600L, deleted = 3_000L))
        database.attackDao().insertAttack(attack("before", 100L, 900L))
        assertFalse(logged())

        database.attackDao().insertAttack(attack("touching", 500L, 1_000L))
        assertTrue(logged())
    }

    @Test
    fun givenAnIntakeInThePeriod_whenAskedWhetherAnythingIsLogged_thenItCounts() {
        database.medicationDao().insert(
            MedicationLocalModel(
                "m", "Ibuprofen", "ibuprofen", "ACUTE", "SIMPLE", null, false, 1L, 1L
            )
        )
        database.medicationDao().insertIntake(
            MedicationIntakeLocalModel("i", "m", null, 1_500L, "Asia/Tashkent", null, 1_500L)
        )

        assertTrue(classUnderTest.anythingLogged(1_000L, 2_000L, "2026-08-20", TODAY))
        assertFalse(classUnderTest.anythingLogged(1_501L, 2_000L, "2026-08-20", TODAY))
    }

    private fun attack(id: String, start: Long, end: Long?, deleted: Long? = null) =
        AttackLocalModel(
            id = id,
            startEpochMillis = start,
            startZoneId = "Asia/Tashkent",
            endEpochMillis = end,
            longCheckConfirmedEpochMillis = null,
            longCheckDisabled = false,
            createdEpochMillis = start,
            updatedEpochMillis = start,
            deletedEpochMillis = deleted
        )
}
