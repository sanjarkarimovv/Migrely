package com.migrely.app.datasource.review.datasource

import android.app.Activity
import android.content.Context
import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val NOT_ON_MAIN_THREAD = "Await Google Play's review flow off the main thread"

/** How long [PlayReviewDataSourceImpl] waits for Google Play; tests shorten the waits. */
class PlayReviewTimings(
    val prepareTimeoutMillis: Long = 10_000L,
    /** The flow ends only when its card closes, so a reader may take a while (design D1). */
    val launchTimeoutMillis: Long = 10 * 60_000L
)

/**
 * Google Play's in-app review (BRD D129, D131, change add-in-app-review design D1). It talks to the
 * Play Store app, needs no permission, and says neither whether a card showed nor whether a rating
 * was given.
 * - [currentActivity] is the activity on screen, which the flow opens over. It is read in the same
 *   main-thread step that launches. Without one the flow is unavailable.
 * - Google Play is called on [mainContext], as it answers there, and each answer is awaited on the
 *   caller's thread, which therefore must not be the main thread ([onMainThread]).
 * - [newManager] builds the manager; tests pass their own.
 */
class PlayReviewDataSourceImpl(
    context: Context,
    private val currentActivity: () -> Activity?,
    newManager: (Context) -> ReviewManager = ReviewManagerFactory::create,
    private val timings: PlayReviewTimings = PlayReviewTimings(),
    private val mainContext: CoroutineContext = Dispatchers.Main,
    private val onMainThread: () -> Boolean = { Looper.myLooper() == Looper.getMainLooper() }
) : ReviewDataSource {
    private val manager: ReviewManager = newManager(context)

    /** What Google Play prepared, until a launch uses it. */
    private val prepared = AtomicReference<ReviewInfo?>()

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun prepareReviewFlow(): Boolean {
        checkOffMainThread()
        return runBlocking {
            try {
                val request = withContext(mainContext) { manager.requestReviewFlow() }
                val ready = request.succeeded(timings.prepareTimeoutMillis)
                prepared.set(if (ready) request.result else null)
                ready
            } catch (exception: RuntimeException) {
                prepared.set(null)
                false
            }
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun launchReviewFlow(): ReviewFlowDataModel {
        checkOffMainThread()
        val info = prepared.getAndSet(null) ?: return ReviewFlowDataModel.Unavailable
        return runBlocking {
            try {
                val launch = withContext(mainContext) {
                    currentActivity()?.let { activity -> manager.launchReviewFlow(activity, info) }
                }
                if (launch != null && launch.succeeded(timings.launchTimeoutMillis)) {
                    ReviewFlowDataModel.Completed
                } else {
                    ReviewFlowDataModel.Unavailable
                }
            } catch (exception: RuntimeException) {
                ReviewFlowDataModel.Unavailable
            }
        }
    }

    /** Waiting here for an answer Google Play gives on the main thread would never end (review M-2). */
    private fun checkOffMainThread() {
        check(!onMainThread()) { NOT_ON_MAIN_THREAD }
    }

    /** Whether the task succeeds within [timeoutMillis]. A failure or no answer in time is false. */
    private suspend fun Task<*>.succeeded(timeoutMillis: Long): Boolean {
        val done = CompletableDeferred<Boolean>()
        addOnCompleteListener { done.complete(it.isSuccessful) }
        return withTimeoutOrNull(timeoutMillis) { done.await() } == true
    }
}
