package com.migrely.app.datasource.schedule.datasource

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.premium.PremiumCheckWorker
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/** The background checks of Premium while a payment is pending or unacknowledged (review I2). */
class PremiumCheckScheduleDataSourceImplTest {
    private val result: ListenableFuture<Operation.State.SUCCESS> = mock()
    private val operation: Operation = mock { on { result } doReturn result }
    private val workManager: WorkManager = mock {
        on { cancelUniqueWork("premium-check") } doReturn operation
    }
    private val classUnderTest = PremiumCheckScheduleDataSourceImpl(workManager)

    @Test
    fun `When planning then one unique work runs about every 12 hours with no constraints`() {
        classUnderTest.schedule()

        val request = argumentCaptor<PeriodicWorkRequest>()
        verify(workManager).enqueueUniquePeriodicWork(
            eq("premium-check"),
            eq(ExistingPeriodicWorkPolicy.KEEP),
            request.capture()
        )
        val spec = request.firstValue.workSpec
        assertEquals(TimeUnit.HOURS.toMillis(12), spec.intervalDuration)
        assertEquals(PremiumCheckWorker::class.java.name, spec.workerClassName)
        assertEquals(Constraints.NONE, spec.constraints)
        assertTrue("premium-check" in request.firstValue.tags)
    }

    @Test
    fun `When cancelling then it waits until stored, and a failure is a database one`() {
        classUnderTest.cancel()
        verify(result).get()

        given(result.get()).willThrow(ExecutionException(IllegalStateException("disk")))
        assertThrows(DatabaseDataException::class.java) { classUnderTest.cancel() }
    }
}
