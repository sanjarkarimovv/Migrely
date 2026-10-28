package com.migrely.app.datasource.review.datasource

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** How long [PlayReviewDataSourceImpl] waits for Google Play; tests shorten the waits. */
class PlayReviewTimings(
    val requestTimeoutMillis: Long = 10_000L,
    /** The flow ends only when its card closes, so a reader may take a while (design D1). */
    val launchTimeoutMillis: Long = 10 * 60_000L
)

/**
 * Google Play's in-app review (BRD D129, D131, change add-in-app-review design D1). It talks to the
 * Play Store app, needs no permission, and says neither whether a card showed nor whether a rating
 * was given.
 * - [currentActivity] is the activity on screen, which the flow opens over. Without one the flow is
 *   unavailable.
 * - Google Play is called on [mainContext], as it answers there, and each answer is awaited on the
 *   caller's thread.
 * - [newManager] builds the manager; tests pass their own.
 */
class PlayReviewDataSourceImpl(
    context: Context,
    private val currentActivity: () -> Activity?,
    newManager: (Context) -> ReviewManager = ReviewManagerFactory::create,
    private val timings: PlayReviewTimings = PlayReviewTimings(),
    private val mainContext: CoroutineContext = Dispatchers.Main
) : ReviewDataSource {
    private val manager: ReviewManager = newManager(context)

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun launchReviewFlow(): ReviewFlowDataModel = runBlocking {
        try {
            val request = withContext(mainContext) { manager.requestReviewFlow() }
            if (!request.succeeded(timings.requestTimeoutMillis)) {
                return@runBlocking ReviewFlowDataModel.Unavailable
            }
            // Read after Google Play answers, so the flow opens over the activity on screen now.
            val activity = currentActivity() ?: return@runBlocking ReviewFlowDataModel.Unavailable
            val launch = withContext(mainContext) {
                manager.launchReviewFlow(activity, request.result)
            }
            if (launch.succeeded(timings.launchTimeoutMillis)) {
                ReviewFlowDataModel.Completed
            } else {
                ReviewFlowDataModel.Unavailable
            }
        } catch (exception: RuntimeException) {
            ReviewFlowDataModel.Unavailable
        }
    }

    /** Whether the task succeeds within [timeoutMillis]. A failure or no answer in time is false. */
    private suspend fun Task<*>.succeeded(timeoutMillis: Long): Boolean {
        val done = CompletableDeferred<Boolean>()
        addOnCompleteListener { done.complete(it.isSuccessful) }
        return withTimeoutOrNull(timeoutMillis) { done.await() } == true
    }
}
