package com.migrely.app.datasource.schedule.datasource

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val NOW: Instant = Instant.parse("2026-11-10T15:01:00Z")
private val TODAY: LocalDate = LocalDate.of(2026, 11, 10)

/**
 * The daily reminder as WorkManager unique work `daily` (BRD D176, change keep-late-daily-reminder): an app start keeps
 * a planned run, even a late one, and plans one only when none is waiting; every other planning replaces it. On the JVM
 * for the reason given in [VisitWorkTest].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DailyWorkTest {
    private lateinit var workManager: WorkManager
    private lateinit var classUnderTest: ReminderScheduleDataSourceImpl

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )
        workManager = WorkManager.getInstance(context)
        classUnderTest = ReminderScheduleDataSourceImpl(
            workManager,
            object : ClockDataSource {
                override fun now() = NOW

                override fun zone(): ZoneId = ZoneId.of("Asia/Tashkent")
            }
        )
    }

    private fun waiting(): List<WorkInfo> = workManager.getWorkInfosForUniqueWork("daily").get()
        .filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun `Given a daily run is waiting when planned only if none then that run is kept`() {
        classUnderTest.scheduleDaily(NOW.plus(Duration.ofMinutes(5)), TODAY)
        val planned = waiting().single().id

        classUnderTest.scheduleDailyIfNone(NOW.plus(Duration.ofDays(1)), TODAY.plusDays(1))

        assertEquals(planned, waiting().single().id)
        assertEquals(Duration.ofMinutes(5).toMillis(), waiting().single().initialDelayMillis)
    }

    @Test
    fun `Given no daily run when planned only if none then one is planned`() {
        classUnderTest.scheduleDailyIfNone(NOW.plus(Duration.ofMinutes(10)), TODAY)

        assertEquals(Duration.ofMinutes(10).toMillis(), waiting().single().initialDelayMillis)
    }

    @Test
    fun `Given a daily run is waiting when planned then it is replaced`() {
        classUnderTest.scheduleDaily(NOW.plus(Duration.ofMinutes(5)), TODAY)
        val planned = waiting().single().id

        classUnderTest.scheduleDaily(NOW.plus(Duration.ofMinutes(35)), TODAY)

        assertNotEquals(planned, waiting().single().id)
        assertEquals(Duration.ofMinutes(35).toMillis(), waiting().single().initialDelayMillis)
    }
}
