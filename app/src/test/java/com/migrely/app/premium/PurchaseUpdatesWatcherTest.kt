package com.migrely.app.premium

import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseUpdatesWatcherTest {
    @Test
    fun `Given Play reports changes then the screens hear of each once it is checked`() =
        runBlocking {
            val checks = MutableSharedFlow<PremiumStatusDomainModel>()
            var watches = 0
            val classUnderTest = PurchaseUpdatesWatcher(
                { checks.also { watches++ } },
                Dispatchers.Unconfined
            )
            val heard = async(Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
                withTimeout(5_000L) { classUnderTest.updates.take(2).toList() }
            }

            classUnderTest.start()
            classUnderTest.start()
            checks.emit(PremiumStatusDomainModel.Pending)
            checks.emit(PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = false))

            assertEquals(listOf(Unit, Unit), heard.await())
            assertEquals(1, watches)
        }

    @Test
    fun `Given the watch fails then it ends quietly`() = runBlocking {
        val classUnderTest = PurchaseUpdatesWatcher(
            { flow { throw IllegalStateException("Play went away") } },
            Dispatchers.Unconfined
        )

        classUnderTest.start()

        val heard = async(Dispatchers.Default) {
            withTimeoutOrNull(100L) { classUnderTest.updates.first() }
        }
        assertEquals(null, heard.await())
    }
}
