package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.DeletedAttackDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.attack.model.RestoreDataModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW: Instant = Instant.parse("2026-10-23T09:00:00Z")
private const val TWO_MINUTES = 120_000L

/** The functions added for the details screen (change add-attack-details). */
class AttackDataSourceDetailsTest {
    private lateinit var classUnderTest: AttackDataSourceImpl
    private val attackDao: AttackDao = mock()

    @Before
    fun setUp() {
        classUnderTest = AttackDataSourceImpl(
            attackDao = attackDao,
            attackLocalMapper = AttackLocalMapper(),
            clock = Clock.fixed(NOW, ZoneOffset.UTC)
        )
    }

    @Test
    fun `When selecting a symptom then inserts its row`() {
        classUnderTest.setTag(
            "a",
            AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Aura),
            true
        )

        verify(attackDao).insertTag(AttackTagLocalModel("a", "SYMPTOM", "aura"))
    }

    @Test
    fun `When clearing a custom trigger then deletes its row`() {
        classUnderTest.setTag("a", AttackTagDataModel.CustomTrigger("t"), false)

        verify(attackDao).deleteTag("a", "TRIGGER", "custom:t")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given an impact of 4 when storing then a programming error`() {
        classUnderTest.setFunctionalImpact("a", 4)
    }

    @Test
    fun `When clearing the impact then stores null`() {
        classUnderTest.setFunctionalImpact("a", null)

        verify(attackDao).setFunctionalImpact("a", null, NOW.toEpochMilli())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a note of 1001 characters when storing then a programming error`() {
        classUnderTest.setNote("a", "n".repeat(1_001))
    }

    @Test
    fun `When deleting softly then purges expired deletions first`() {
        classUnderTest.softDelete("a")

        inOrder(attackDao) {
            verify(attackDao).purgeDeletedBefore(NOW.toEpochMilli() - TWO_MINUTES)
            verify(attackDao).softDelete("a", NOW.toEpochMilli())
        }
    }

    @Test
    fun `When asking for a recent deletion then never looks further back than the retention`() {
        val local = AttackLocalModel(
            id = "a",
            startEpochMillis = 1L,
            startZoneId = "UTC",
            endEpochMillis = 2L,
            longCheckConfirmedEpochMillis = null,
            longCheckDisabled = false,
            createdEpochMillis = 1L,
            updatedEpochMillis = 1L,
            deletedEpochMillis = NOW.toEpochMilli() - 1_000L
        )
        given(attackDao.recentlyDeleted(NOW.toEpochMilli() - TWO_MINUTES)).willReturn(local)

        val actual = classUnderTest.recentlyDeleted(Instant.EPOCH)

        assertEquals(DeletedAttackDataModel("a", NOW.minusMillis(1_000L)), actual)
    }

    @Test
    fun `Given the dao refuses when restoring then not possible`() {
        given(attackDao.restoreIfPossible("a", NOW.toEpochMilli())).willReturn(false)

        assertEquals(RestoreDataModel.NotPossible, classUnderTest.restore("a"))
    }

    @Test
    fun `When changing times then removes the given points in the same call`() {
        val start = NOW.minusSeconds(3_600)
        val removed = IntensityPointDataModel(NOW.minusSeconds(60), 8)

        classUnderTest.updateTimes("a", start, NOW.minusSeconds(600), listOf(removed))

        verify(attackDao).updateTimesRemovingPoints(
            attackId = "a",
            startEpochMillis = start.toEpochMilli(),
            endEpochMillis = NOW.minusSeconds(600).toEpochMilli(),
            pointsToRemove = listOf(
                IntensityPointLocalModel(
                    attackId = "a",
                    atEpochMillis = removed.at.toEpochMilli(),
                    value = 8
                )
            ),
            nowEpochMillis = NOW.toEpochMilli()
        )
    }

    @Test
    fun `Given the purge fails when asking for a recent deletion then still answers`() {
        whenever(
            attackDao.purgeDeletedBefore(NOW.toEpochMilli() - TWO_MINUTES)
        ).doThrow(SQLiteFullException())

        assertEquals(null, classUnderTest.recentlyDeleted(Instant.EPOCH))
    }
}
