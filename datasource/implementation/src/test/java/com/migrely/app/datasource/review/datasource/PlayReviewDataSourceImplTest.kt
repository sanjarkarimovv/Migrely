package com.migrely.app.datasource.review.datasource

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class PlayReviewDataSourceImplTest {
    private val context: Context = mock()
    private val activity: Activity = mock()
    private val reviewInfo: ReviewInfo = mock()
    private val manager: ReviewManager = mock()
    private var onScreen: Activity? = activity

    private val classUnderTest = PlayReviewDataSourceImpl(
        context = context,
        currentActivity = { onScreen },
        newManager = { manager },
        timings = PlayReviewTimings(prepareTimeoutMillis = 50L, launchTimeoutMillis = 50L),
        mainContext = Dispatchers.Unconfined,
        onMainThread = { false }
    )

    /**
     * A Google Play task that ends as [successful]: at once, through [answer] when given (as Google
     * Play answers later, on its own thread), or never when [answers] is false.
     */
    private fun <T> task(
        successful: Boolean,
        result: T? = null,
        answers: Boolean = true,
        answer: ((() -> Unit) -> Unit)? = null
    ): Task<T> {
        val task: Task<T> = mock()
        given(task.isSuccessful).willReturn(successful)
        given(task.result).willReturn(result)
        given(task.addOnCompleteListener(any<OnCompleteListener<T>>())).willAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<T>>(0)
            when {
                !answers -> Unit
                answer != null -> answer { listener.onComplete(task) }
                else -> listener.onComplete(task)
            }
            task
        }
        return task
    }

    // Each task is built before stubbing, as Mockito cannot stub one mock while making another.
    private fun givenPrepare(successful: Boolean, answers: Boolean = true) {
        val request = task(successful, reviewInfo, answers)
        given(manager.requestReviewFlow()).willReturn(request)
    }

    private fun givenLaunch(successful: Boolean, answers: Boolean = true) {
        val launch = task<Void>(successful, null, answers)
        given(manager.launchReviewFlow(activity, reviewInfo)).willReturn(launch)
    }

    @Test
    fun `Given Google Play prepares and runs the flow then completed, over the screen`() {
        givenPrepare(successful = true)
        givenLaunch(successful = true)

        assertTrue(classUnderTest.prepareReviewFlow())
        assertEquals(ReviewFlowDataModel.Completed, classUnderTest.launchReviewFlow())
        verify(manager).launchReviewFlow(activity, reviewInfo)
    }

    @Test
    fun `Given a launch has used the prepared flow then another launch is unavailable`() {
        givenPrepare(successful = true)
        givenLaunch(successful = true)
        classUnderTest.prepareReviewFlow()
        classUnderTest.launchReviewFlow()

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager).launchReviewFlow(activity, reviewInfo)
    }

    @Test
    fun `Given nothing was prepared then a launch is unavailable, and nothing opens`() {
        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given Google Play cannot prepare the flow then no, and a launch opens nothing`() {
        givenPrepare(successful = false)

        assertFalse(classUnderTest.prepareReviewFlow())
        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given Google Play does not prepare in time then no`() {
        givenPrepare(successful = true, answers = false)

        assertFalse(classUnderTest.prepareReviewFlow())
    }

    @Test
    fun `Given no activity on screen when launching then unavailable, and nothing opens`() {
        givenPrepare(successful = true)
        classUnderTest.prepareReviewFlow()
        onScreen = null

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given the launch fails or does not end in time then unavailable`() {
        givenPrepare(successful = true)
        givenLaunch(successful = false)
        classUnderTest.prepareReviewFlow()
        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())

        givenLaunch(successful = true, answers = false)
        classUnderTest.prepareReviewFlow()
        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
    }

    @Test
    fun `Given Google Play throws while preparing then no, never a crash`() {
        given(manager.requestReviewFlow()).willThrow(IllegalStateException())

        assertFalse(classUnderTest.prepareReviewFlow())
    }

    @Test
    fun `Given Google Play throws while launching then unavailable, never a crash`() {
        givenPrepare(successful = true)
        classUnderTest.prepareReviewFlow()
        given(manager.launchReviewFlow(activity, reviewInfo)).willThrow(IllegalStateException())

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
    }

    @Test(expected = IllegalStateException::class)
    fun `Given the main thread when preparing then it fails at once instead of hanging`() {
        PlayReviewDataSourceImpl(
            context = context,
            currentActivity = { activity },
            newManager = { manager },
            onMainThread = { true }
        ).prepareReviewFlow()
    }

    @Test(expected = IllegalStateException::class)
    fun `Given the main thread when launching then it fails at once instead of hanging`() {
        PlayReviewDataSourceImpl(
            context = context,
            currentActivity = { activity },
            newManager = { manager },
            onMainThread = { true }
        ).launchReviewFlow()
    }

    @Test
    fun `Given Google Play answers later on its own thread then the caller waits for it`() {
        val main = Executors.newSingleThreadExecutor()
        val play = Executors.newSingleThreadScheduledExecutor()
        val mainThread = main.submit<Thread> { Thread.currentThread() }.get()
        val calledOn = mutableListOf<Thread>()
        val later: (() -> Unit) -> Unit = { complete ->
            play.schedule(complete, 20, TimeUnit.MILLISECONDS)
        }
        val request = task(true, reviewInfo, answer = later)
        val launch = task<Void>(true, null, answer = later)
        val threaded: ReviewManager = mock()
        given(threaded.requestReviewFlow()).willAnswer {
            calledOn += Thread.currentThread()
            request
        }
        given(threaded.launchReviewFlow(activity, reviewInfo)).willAnswer {
            calledOn += Thread.currentThread()
            launch
        }
        val dataSource = PlayReviewDataSourceImpl(
            context = context,
            currentActivity = { activity },
            newManager = { threaded },
            timings = PlayReviewTimings(
                prepareTimeoutMillis = 2_000L,
                launchTimeoutMillis = 2_000L
            ),
            mainContext = main.asCoroutineDispatcher(),
            onMainThread = { Thread.currentThread() == mainThread }
        )
        try {
            assertTrue(dataSource.prepareReviewFlow())
            assertEquals(ReviewFlowDataModel.Completed, dataSource.launchReviewFlow())
            assertEquals(listOf(mainThread, mainThread), calledOn)
        } finally {
            main.shutdownNow()
            play.shutdownNow()
        }
    }
}
