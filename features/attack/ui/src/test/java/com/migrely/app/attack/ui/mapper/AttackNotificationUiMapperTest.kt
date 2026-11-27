package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.AttackRangePresentationModel
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.presentation.model.PointRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.presentation.model.TimeRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TimesUndoPresentationModel
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.AttackRangeUiModel
import com.migrely.app.attack.ui.model.CustomTagUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel
import com.migrely.app.attack.ui.model.TimesUndoUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
class AttackNotificationUiMapperTest(
    private val notification: AttackDetailsPresentationNotification,
    private val expected: AttackDetailsEventUiModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AttackDetailsPresentationNotification.TimeRejected(
                    TimeFieldPresentationModel.Start,
                    TimeRejectionPresentationModel.Overlaps(
                        listOf(
                            AttackRangePresentationModel(
                                Instant.ofEpochMilli(1_000L),
                                Instant.ofEpochMilli(2_000L)
                            ),
                            AttackRangePresentationModel(Instant.ofEpochMilli(3_000L), null)
                        ),
                        requestedStart = Instant.ofEpochMilli(500L),
                        requestedEnd = null
                    )
                ),
                AttackDetailsEventUiModel.TimeRejected(
                    TimeErrorUiModel(
                        TimeFieldUiModel.Start,
                        TimeErrorReasonUiModel.Overlaps(
                            listOf(
                                AttackRangeUiModel(1_000L, 2_000L),
                                AttackRangeUiModel(3_000L, null)
                            ),
                            requestedStartEpochMillis = 500L,
                            requestedEndEpochMillis = null
                        )
                    )
                )
            ),
            // Not a message: the screen decides whether it replaces an error (BRD D165 (4)).
            arrayOf<Any>(
                AttackDetailsPresentationNotification.Saved,
                AttackDetailsEventUiModel.Saved
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.AttacksMerged,
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.AttacksMerged)
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.TimeRejected(
                    TimeFieldPresentationModel.End,
                    TimeRejectionPresentationModel.InFuture
                ),
                AttackDetailsEventUiModel.TimeRejected(
                    TimeErrorUiModel(TimeFieldUiModel.End, TimeErrorReasonUiModel.InFuture)
                )
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.PointsRemovedByTimeChange(
                    TimesUndoPresentationModel(
                        "a",
                        Instant.ofEpochMilli(1_000L),
                        Instant.ofEpochMilli(9_000L),
                        listOf(IntensityPointPresentationModel(Instant.ofEpochMilli(8_000L), 7))
                    )
                ),
                AttackDetailsEventUiModel.Message(
                    AttackMessageUiModel.PointsRemovedByTimeChange(
                        TimesUndoUiModel(
                            "a",
                            1_000L,
                            9_000L,
                            listOf(IntensityPointUiModel(8_000L, 7))
                        )
                    )
                )
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.PointRejected(
                    PointRejectionPresentationModel.InFuture
                ),
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.PointOutsideAttack)
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.FirstMaximumRecorded,
                AttackDetailsEventUiModel.FirstMaximumRecorded
            ),
            arrayOf<Any>(
                AttackDetailsPresentationNotification.StorageFull,
                AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull)
            ),
            // The tag travels with the message, for its spoken Undo name and the Undo itself (BRD D199).
            arrayOf<Any>(
                AttackDetailsPresentationNotification.CustomTagRemoved(
                    "a",
                    CustomTagPresentationModel(
                        "t",
                        TagKindPresentationModel.Trigger,
                        "Perfume",
                        archived = false
                    )
                ),
                AttackDetailsEventUiModel.Message(
                    AttackMessageUiModel.CustomTagRemoved(
                        "a",
                        CustomTagUiModel("t", TagKindUiModel.Trigger, "Perfume", archived = false)
                    )
                )
            )
        )
    }

    @Test
    fun `When the notification is presented then the event reaches the screen`() {
        // Given
        val presenter: AttackDetailsEventPresenter = mock()
        val classUnderTest = AttackNotificationUiMapper(
            presenter,
            TimeFieldUiMapper(),
            AttackRangeUiMapper(),
            AttackIntakeUiMapper(),
            CustomTagUiMapper(TagKindUiMapper())
        )

        // When
        classUnderTest.toUi(notification).present()

        // Then
        verify(presenter).show(expected)
    }
}
