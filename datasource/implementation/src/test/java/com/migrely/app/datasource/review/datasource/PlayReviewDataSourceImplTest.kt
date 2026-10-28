package com.migrely.app.datasource.review.datasource

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
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
        timings = PlayReviewTimings(requestTimeoutMillis = 50L, launchTimeoutMillis = 50L),
        mainContext = Dispatchers.Unconfined
    )

    /** A Google Play task that ends at once as [successful], or never ends when [answers] is false. */
    private fun <T> task(successful: Boolean, result: T? = null, answers: Boolean = true): Task<T> {
        val task: Task<T> = mock {
            on { isSuccessful } doReturn successful
            on { this.result } doReturn result
        }
        given(task.addOnCompleteListener(any<OnCompleteListener<T>>())).willAnswer { invocation ->
            if (answers) invocation.getArgument<OnCompleteListener<T>>(0).onComplete(task)
            task
        }
        return task
    }

    // Each task is built before stubbing, as Mockito cannot stub one mock while making another.
    private fun givenRequest(successful: Boolean, answers: Boolean = true) {
        val request = task(successful, reviewInfo, answers)
        given(manager.requestReviewFlow()).willReturn(request)
    }

    private fun givenLaunch(successful: Boolean, answers: Boolean = true) {
        val launch = task<Void>(successful, null, answers)
        given(manager.launchReviewFlow(activity, reviewInfo)).willReturn(launch)
    }

    @Test
    fun `Given Google Play runs the flow to its end then completed, over the activity on screen`() {
        givenRequest(successful = true)
        givenLaunch(successful = true)

        assertEquals(ReviewFlowDataModel.Completed, classUnderTest.launchReviewFlow())
        verify(manager).launchReviewFlow(activity, reviewInfo)
    }

    @Test
    fun `Given Google Play cannot prepare the flow then unavailable, and nothing is launched`() {
        givenRequest(successful = false)

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given no activity on screen then unavailable, and nothing is launched`() {
        givenRequest(successful = true)
        onScreen = null

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given the launch fails then unavailable`() {
        givenRequest(successful = true)
        givenLaunch(successful = false)

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
    }

    @Test
    fun `Given Google Play does not answer the request in time then unavailable`() {
        givenRequest(successful = true, answers = false)

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
        verify(manager, never()).launchReviewFlow(any(), any())
    }

    @Test
    fun `Given the flow does not end in time then unavailable`() {
        givenRequest(successful = true)
        givenLaunch(successful = true, answers = false)

        assertEquals(ReviewFlowDataModel.Unavailable, classUnderTest.launchReviewFlow())
    }

    @Test
    fun `Given Google Play throws then unavailable, never a crash`() {
        val throwing: ReviewManager = mock {
            on { requestReviewFlow() } doThrow
                IllegalStateException()
        }
        val dataSource = PlayReviewDataSourceImpl(
            context = context,
            currentActivity = { activity },
            newManager = { throwing },
            mainContext = Dispatchers.Unconfined
        )

        assertEquals(ReviewFlowDataModel.Unavailable, dataSource.launchReviewFlow())
    }
}
