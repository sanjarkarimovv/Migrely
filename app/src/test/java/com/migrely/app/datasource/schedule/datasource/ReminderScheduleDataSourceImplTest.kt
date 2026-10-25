package com.migrely.app.datasource.schedule.datasource

import androidx.work.Operation
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.reminder.ReminderWorker
import java.util.concurrent.ExecutionException
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/** Cancelling every reminder before all data is deleted (change add-settings-and-safety, BRD D96). */
class ReminderScheduleDataSourceImplTest {
    private val result: ListenableFuture<Operation.State.SUCCESS> = mock()
    private val operation: Operation = mock { on { result } doReturn result }
    private val workManager: WorkManager = mock {
        on { cancelAllWorkByTag(ReminderWorker::class.java.name) } doReturn operation
    }
    private val classUnderTest =
        ReminderScheduleDataSourceImpl(workManager, mock<ClockDataSource>())

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
}
