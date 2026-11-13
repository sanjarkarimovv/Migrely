package com.migrely.app.datasource.schedule.datasource

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.reminder.ReminderWorker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ExecutionException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW = Instant.parse("2026-10-24T15:01:00Z")
private val DATE = LocalDate.of(2026, 10, 24)

/**
 * Cancelling every reminder before all data is deleted (change add-settings-and-safety, BRD D96), and the two ways the
 * daily reminder is planned (change keep-late-daily-reminder, BRD D176).
 */
class ReminderScheduleDataSourceImplTest {
    private val result: ListenableFuture<Operation.State.SUCCESS> = mock()
    private val operation: Operation = mock { on { result } doReturn result }
    private val workManager: WorkManager = mock {
        on { cancelAllWorkByTag(ReminderWorker::class.java.name) } doReturn operation
    }
    private val clockDataSource: ClockDataSource = mock { on { now() } doReturn NOW }
    private val classUnderTest = ReminderScheduleDataSourceImpl(workManager, clockDataSource)

    @Test
    fun `When cancelling everything then every reminder run goes, and it waits until stored`() {
        classUnderTest.cancelAll()

        verify(workManager).cancelAllWorkByTag(ReminderWorker::class.java.name)
        verify(result).get()
    }

    @Test
    fun `Given WorkManager cannot store it when cancelling everything then a database failure`() {
        given(result.get()).willThrow(ExecutionException(IllegalStateException("disk")))

        assertThrows(DatabaseDataException::class.java) { classUnderTest.cancelAll() }
    }

    @Test
    fun `When planning the daily run only if none then kept, with the same name and input`() {
        classUnderTest.scheduleDailyIfNone(NOW.plusSeconds(60), DATE)

        assertDailyEnqueued(ExistingWorkPolicy.KEEP, Duration.ofSeconds(60))
    }

    @Test
    fun `When planning the daily run then any planned run is replaced`() {
        classUnderTest.scheduleDaily(NOW.plusSeconds(60), DATE)

        assertDailyEnqueued(ExistingWorkPolicy.REPLACE, Duration.ofSeconds(60))
    }

    private fun assertDailyEnqueued(policy: ExistingWorkPolicy, delay: Duration) {
        val request = argumentCaptor<OneTimeWorkRequest>()
        verify(workManager).enqueueUniqueWork(eq("daily"), eq(policy), request.capture())
        val spec = request.firstValue.workSpec
        assertEquals(delay.toMillis(), spec.initialDelay)
        assertEquals(
            ReminderWorker.KIND_DAILY,
            spec.input.getString(ReminderWorker.KEY_KIND)
        )
        assertEquals(DATE.toString(), spec.input.getString(ReminderWorker.KEY_DATE))
    }
}
