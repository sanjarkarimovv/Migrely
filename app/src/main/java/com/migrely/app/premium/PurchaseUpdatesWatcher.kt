package com.migrely.app.premium

import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val UPDATES_BUFFER = 8

/**
 * Listens, for as long as the app runs, to Google Play's purchase changes that no sheet waits for,
 * such as a pending payment completed at a shop (change add-subscription, review I1). Each one is
 * checked by [watch], which acknowledges the purchase and keeps the entitlement, and then
 * [updates] tells the screens that show the status, the paywall and Settings, to read it again.
 * Started once by `MigrelyApplication`; a failure ends the watch quietly, and the next start, return
 * or check reads Play anyway.
 */
class PurchaseUpdatesWatcher(
    private val watch: () -> Flow<PremiumStatusDomainModel>,
    backgroundContext: CoroutineContext
) {
    private val scope = CoroutineScope(SupervisorJob() + backgroundContext)
    private val checked = MutableSharedFlow<Unit>(extraBufferCapacity = UPDATES_BUFFER)
    private var started = false

    /** A signal after each change Google Play reported has been checked. */
    val updates: Flow<Unit> = checked.asSharedFlow()

    @Synchronized
    fun start() {
        if (started) return
        started = true
        scope.launch { watch().catch { }.collect { checked.tryEmit(Unit) } }
    }
}
