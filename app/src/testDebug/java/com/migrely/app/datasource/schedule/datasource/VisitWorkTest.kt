package com.migrely.app.datasource.schedule.datasource

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.reminder.ReminderWorker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val NOW: Instant = Instant.parse("2026-11-10T07:00:00Z")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val NOVEMBER_27: LocalDate = LocalDate.of(2026, 11, 27)

/**
 * The visit reminder as WorkManager unique work `visit` (BRD D168, design D3, task 5.1): one run with the expected
 * delay, replaced by a new date, and cancelled. On the JVM, because `:app`'s instrumented run would meet the app's own
 * WorkManager. SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class VisitWorkTest {
    private lateinit var workManager: WorkManager
    private lateinit var classUnderTest: ReminderScheduleDataSourceImpl

    /** What each run would be given; the factory stands in for the app's worker, which needs Hilt. */
    private val inputs = mutableListOf<Data>()
    private val recordingFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker = object : Worker(appContext, workerParameters) {
            override fun doWork(): Result {
                inputs += inputData
                return Result.success()
            }
        }
    }

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(recordingFactory)
                .build()
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

    private fun work(name: String): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWork(name).get()

    @Test
    fun `When a visit is planned then one run waits until its time`() {
        classUnderTest.scheduleVisit(NOW.plus(Duration.ofDays(8)), NOVEMBER_20)

        val run = work("visit").single()
        assertEquals(WorkInfo.State.ENQUEUED, run.state)
        assertEquals(Duration.ofDays(8).toMillis(), run.initialDelayMillis)
        assertTrue(run.tags.contains(ReminderWorker::class.java.name))
    }

    @Test
    fun `When the visit moves then the old run is replaced by one for the new date`() {
        classUnderTest.scheduleVisit(NOW.plus(Duration.ofDays(8)), NOVEMBER_20)
        val first = work("visit").single().id

        classUnderTest.scheduleVisit(NOW.plus(Duration.ofDays(15)), NOVEMBER_27)

        val active = work("visit").filter { it.state == WorkInfo.State.ENQUEUED }
        assertEquals(1, active.size)
        assertNotEquals(first, active.single().id)
        assertEquals(Duration.ofDays(15).toMillis(), active.single().initialDelayMillis)
    }

    @Test
    fun `Given a time already passed when planned then it runs at once`() {
        classUnderTest.scheduleVisit(NOW.minus(Duration.ofHours(3)), NOVEMBER_20)

        assertEquals(0L, work("visit").single().initialDelayMillis)
    }

    @Test
    fun `When the visit is removed then its run is cancelled`() {
        classUnderTest.scheduleVisit(NOW.plus(Duration.ofDays(8)), NOVEMBER_20)

        classUnderTest.cancelVisit()

        assertEquals(WorkInfo.State.CANCELLED, work("visit").single().state)
    }

    @Test
    fun `When everything is planned again after an import then one immediate run`() {
        classUnderTest.planAgain()

        assertEquals(0L, work("imported").single().initialDelayMillis)
    }

    @Test
    fun `When the planned visit run comes then it is given its kind and the visit date`() {
        classUnderTest.scheduleVisit(NOW.plus(Duration.ofDays(8)), NOVEMBER_20)

        WorkManagerTestInitHelper.getTestDriver(RuntimeEnvironment.getApplication())!!
            .setInitialDelayMet(work("visit").single().id)

        val input = inputs.single()
        assertEquals(ReminderWorker.KIND_VISIT, input.getString(ReminderWorker.KEY_KIND))
        assertEquals("2026-11-20", input.getString(ReminderWorker.KEY_DATE))
    }

    @Test
    fun `When reminders are planned again after an import then the run is of the import kind`() {
        classUnderTest.planAgain()

        assertEquals(
            ReminderWorker.KIND_IMPORTED,
            inputs.single().getString(ReminderWorker.KEY_KIND)
        )
    }
}
