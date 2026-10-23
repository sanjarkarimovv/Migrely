package com.migrely.app.home.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.home.data.mapper.AttackDomainMapper
import com.migrely.app.home.data.mapper.IntensityPointDomainMapper
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

// 2026-10-23 20:00 in Tashkent (UTC+5).
private val NOW: Instant = Instant.parse("2026-10-23T15:00:00Z")

class HomeRepositoryTest {
    private lateinit var classUnderTest: HomeRepository
    private val attackDataSource: AttackDataSource = mock()
    private val clockDataSource: ClockDataSource = mock()
    private val mapper = AttackDomainMapper(IntensityPointDomainMapper())

    @Before
    fun setUp() {
        classUnderTest = HomeRepository(attackDataSource, clockDataSource, mapper)
        given(clockDataSource.now()).willReturn(NOW)
        given(clockDataSource.zone()).willReturn(TASHKENT)
    }

    @Test
    fun `When today then queries the local day in the device zone and maps the snapshot`() {
        // Given: the Tashkent day 2026-10-23 runs from 22 Oct 19:00Z to 23 Oct 19:00Z
        val from = Instant.parse("2026-10-22T19:00:00Z")
        val to = Instant.parse("2026-10-23T19:00:00Z")
        val inProgress = attackData("now")
        val ended = attackData("earlier", end = NOW.minusSeconds(3_600))
        given(attackDataSource.inProgress()).willReturn(inProgress)
        given(attackDataSource.endedTouching(from, to)).willReturn(listOf(ended))

        // When
        val actual = classUnderTest.today()

        // Then
        assertEquals(
            TodaySnapshotResultDomainModel.Loaded(
                TodaySnapshotDomainModel(
                    attackInProgress = mapper.toDomain(inProgress),
                    endedAttacksTouchingToday = listOf(mapper.toDomain(ended)),
                    now = NOW
                )
            ),
            actual
        )
    }

    @Test
    fun `Given the datasource fails when today then Failed`() {
        given(attackDataSource.inProgress()).willThrow(DatabaseDataException())

        assertEquals(TodaySnapshotResultDomainModel.Failed, classUnderTest.today())
    }

    @Test
    fun `Given a new attack when starting then Started with the zone id stored`() {
        val started = attackData("a")
        given(
            attackDataSource.startIfNoneInProgress(NOW, "Asia/Tashkent")
        ).willReturn(AttackStartDataModel.Started(started))

        val actual = classUnderTest.startAttackIfNoneInProgress(NOW, TASHKENT)

        assertEquals(StartAttackResultDomainModel.Started(mapper.toDomain(started)), actual)
    }

    @Test
    fun `Given one in progress when starting then AlreadyInProgress`() {
        val existing = attackData("a")
        given(
            attackDataSource.startIfNoneInProgress(any(), any())
        ).willReturn(AttackStartDataModel.AlreadyInProgress(existing))

        val actual = classUnderTest.startAttackIfNoneInProgress(NOW, TASHKENT)

        assertEquals(
            StartAttackResultDomainModel.AlreadyInProgress(mapper.toDomain(existing)),
            actual
        )
    }

    @Test
    fun `Given storage is full when starting then StorageFull`() {
        given(
            attackDataSource.startIfNoneInProgress(any(), any())
        ).willThrow(StorageFullDataException())

        assertEquals(
            StartAttackResultDomainModel.StorageFull,
            classUnderTest.startAttackIfNoneInProgress(NOW, TASHKENT)
        )
    }

    @Test
    fun `Given a database failure when starting then Failed`() {
        given(
            attackDataSource.startIfNoneInProgress(any(), any())
        ).willThrow(DatabaseDataException())

        assertEquals(
            StartAttackResultDomainModel.Failed,
            classUnderTest.startAttackIfNoneInProgress(NOW, TASHKENT)
        )
    }

    @Test
    fun `Given none in progress when looking up then None`() {
        given(attackDataSource.inProgress()).willReturn(null)

        assertEquals(AttackLookupResultDomainModel.None, classUnderTest.attackInProgress())
    }

    @Test
    fun `Given a database failure when looking up then Failed`() {
        given(attackDataSource.inProgress()).willThrow(DatabaseDataException())

        assertEquals(AttackLookupResultDomainModel.Failed, classUnderTest.attackInProgress())
    }

    @Test
    fun `When adding a point then writes it and returns Written`() {
        val actual = classUnderTest.addIntensityPoint("a", IntensityPointDomainModel(NOW, 6))

        assertEquals(WriteResultDomainModel.Written, actual)
        verify(attackDataSource).addIntensityPoint("a", NOW, 6)
    }

    @Test
    fun `Given storage is full when replacing a point then StorageFull`() {
        doThrow(
            StorageFullDataException()
        ).whenever(attackDataSource).replaceLatestIntensityPoint(any(), any(), any())

        assertEquals(
            WriteResultDomainModel.StorageFull,
            classUnderTest.replaceLatestIntensityPoint("a", IntensityPointDomainModel(NOW, 9))
        )
    }

    @Test
    fun `Given a database failure when ending then Failed`() {
        doThrow(DatabaseDataException()).whenever(attackDataSource).end(any(), any())

        assertEquals(WriteResultDomainModel.Failed, classUnderTest.endAttack("a", NOW))
    }

    @Test
    fun `When updating the start then Written`() {
        assertEquals(WriteResultDomainModel.Written, classUnderTest.updateAttackStart("a", NOW))
        verify(attackDataSource).updateStart("a", NOW)
    }

    @Test
    fun `When deleting then Written`() {
        assertEquals(WriteResultDomainModel.Written, classUnderTest.deleteAttack("a"))
        verify(attackDataSource).delete("a")
    }

    @Test
    fun `Given a previous end when looking it up then Found`() {
        given(attackDataSource.previousEndBefore("a", NOW)).willReturn(NOW.minusSeconds(60))

        assertEquals(
            PreviousEndResultDomainModel.Found(NOW.minusSeconds(60)),
            classUnderTest.previousAttackEnd("a", NOW)
        )
    }

    @Test
    fun `Given no previous end when looking it up then None`() {
        given(attackDataSource.previousEndBefore("a", NOW)).willReturn(null)

        assertEquals(PreviousEndResultDomainModel.None, classUnderTest.previousAttackEnd("a", NOW))
    }

    @Test
    fun `Given a database failure when looking up the previous end then Failed`() {
        given(attackDataSource.previousEndBefore(any(), any())).willThrow(DatabaseDataException())

        assertEquals(
            PreviousEndResultDomainModel.Failed,
            classUnderTest.previousAttackEnd("a", NOW)
        )
    }

    @Test
    fun `Given another attack in progress when reopening then AnotherInProgress`() {
        given(attackDataSource.reopen("a")).willReturn(ReopenDataModel.AnotherInProgress)

        assertEquals(ReopenResultDomainModel.AnotherInProgress, classUnderTest.reopenAttack("a"))
    }

    @Test
    fun `Given storage is full when reopening then StorageFull`() {
        given(attackDataSource.reopen("a")).willThrow(StorageFullDataException())

        assertEquals(ReopenResultDomainModel.StorageFull, classUnderTest.reopenAttack("a"))
    }

    @Test
    fun `When updating the long attack check then writes both fields`() {
        val actual = classUnderTest.updateLongAttackCheck(
            "a",
            LongAttackCheckDomainModel(NOW, disabled = false)
        )

        assertEquals(WriteResultDomainModel.Written, actual)
        verify(attackDataSource).updateLongAttackCheck("a", NOW, false)
    }

    private fun attackData(id: String, end: Instant? = null) = AttackDataModel(
        id = id,
        start = NOW.minusSeconds(7_200),
        startZoneId = "Asia/Tashkent",
        end = end,
        intensityPoints = emptyList(),
        longCheckConfirmedAt = null,
        longCheckDisabled = false
    )
}
