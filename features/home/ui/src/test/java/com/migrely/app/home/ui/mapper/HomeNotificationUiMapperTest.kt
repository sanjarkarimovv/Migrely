package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.HomePresentationNotification
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class HomeNotificationUiMapperTest(
    private val notification: HomePresentationNotification,
    private val expectedMessage: HomeMessageUiModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                HomePresentationNotification.AttackStarted("a"),
                HomeMessageUiModel.AttackStarted("a")
            ),
            arrayOf<Any>(
                HomePresentationNotification.AttackEnded("a"),
                HomeMessageUiModel.AttackEnded("a")
            ),
            arrayOf<Any>(HomePresentationNotification.Undone, HomeMessageUiModel.Undone),
            arrayOf<Any>(
                HomePresentationNotification.AttackDeleted("a", Instant.ofEpochMilli(9_000L)),
                HomeMessageUiModel.AttackDeleted("a", 9_000L)
            ),
            arrayOf<Any>(
                HomePresentationNotification.UndoNotPossible,
                HomeMessageUiModel.UndoNotPossible
            ),
            arrayOf<Any>(HomePresentationNotification.StorageFull, HomeMessageUiModel.StorageFull),
            arrayOf<Any>(
                HomePresentationNotification.StartMoveRejected(Instant.ofEpochMilli(5_000L)),
                HomeMessageUiModel.StartMoveRejected(5_000L)
            ),
            arrayOf<Any>(
                HomePresentationNotification.EndTimeOutOfRange,
                HomeMessageUiModel.EndTimeOutOfRange
            ),
            arrayOf<Any>(
                HomePresentationNotification.SomethingWentWrong,
                HomeMessageUiModel.SomethingWentWrong
            )
        )
    }

    @Test
    fun `When the UI notification is presented then the message reaches the presenter`() {
        // Given
        val presenter: HomeMessagePresenter = mock()
        val classUnderTest = HomeNotificationUiMapper(presenter)

        // When
        classUnderTest.toUi(notification).present()

        // Then
        verify(presenter).show(expectedMessage)
    }
}
