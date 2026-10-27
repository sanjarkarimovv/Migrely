package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AddPastAttackPresentationNotification
import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.ui.model.AddPastAttackEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.AttackRangeUiModel
import com.migrely.app.attack.ui.model.PastAttackProblemUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel
import com.migrely.app.attack.ui.notification.AddPastAttackEventPresenter
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class AddPastAttackNotificationUiMapperTest(
    private val notification: AddPastAttackPresentationNotification,
    private val expected: AddPastAttackEventUiModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AddPastAttackPresentationNotification.InFuture(TimeFieldPresentationModel.Start),
                AddPastAttackEventUiModel.Problem(
                    PastAttackProblemUiModel.Time(
                        TimeErrorUiModel(TimeFieldUiModel.Start, TimeErrorReasonUiModel.InFuture)
                    )
                )
            ),
            arrayOf<Any>(
                AddPastAttackPresentationNotification.EndBeforeStart,
                AddPastAttackEventUiModel.Problem(
                    PastAttackProblemUiModel.Time(
                        TimeErrorUiModel(
                            TimeFieldUiModel.End,
                            TimeErrorReasonUiModel.EndBeforeStart
                        )
                    )
                )
            ),
            arrayOf<Any>(
                AddPastAttackPresentationNotification.Overlaps(
                    listOf(
                        AttackRangePresentationModel(
                            Instant.ofEpochMilli(1_000L),
                            Instant.ofEpochMilli(2_000L)
                        ),
                        AttackRangePresentationModel(Instant.ofEpochMilli(3_000L), null)
                    )
                ),
                AddPastAttackEventUiModel.Problem(
                    PastAttackProblemUiModel.Overlaps(
                        listOf(AttackRangeUiModel(1_000L, 2_000L), AttackRangeUiModel(3_000L, null))
                    )
                )
            ),
            arrayOf<Any>(
                AddPastAttackPresentationNotification.StorageFull,
                AddPastAttackEventUiModel.Message(AttackMessageUiModel.StorageFull)
            ),
            arrayOf<Any>(
                AddPastAttackPresentationNotification.SomethingWentWrong,
                AddPastAttackEventUiModel.Message(AttackMessageUiModel.SomethingWentWrong)
            )
        )
    }

    @Test
    fun `When the notification is presented then the event reaches the screen`() {
        // Given
        val presenter: AddPastAttackEventPresenter = mock()
        val classUnderTest = AddPastAttackNotificationUiMapper(
            presenter,
            TimeFieldUiMapper(),
            AttackRangeUiMapper()
        )

        // When
        classUnderTest.toUi(notification).present()

        // Then
        verify(presenter).show(expected)
    }
}
