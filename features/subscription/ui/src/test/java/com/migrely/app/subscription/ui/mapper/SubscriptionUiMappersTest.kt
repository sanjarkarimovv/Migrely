package com.migrely.app.subscription.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallProductPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.model.PremiumNotePresentationModel
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import com.migrely.app.subscription.presentation.model.TrialBannerViewState
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallEventUiModel
import com.migrely.app.subscription.ui.model.PaywallMessageBehaviourUiModel
import com.migrely.app.subscription.ui.model.PaywallMessageUiModel
import com.migrely.app.subscription.ui.model.PaywallOffersUiModel
import com.migrely.app.subscription.ui.model.PaywallProductUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumNoteUiModel
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.TrialBannerViewStateUiModel
import com.migrely.app.subscription.ui.model.TrialLengthUiModel
import java.time.Instant
import java.time.Period
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val TRIAL_END: Instant = Instant.parse("2026-11-01T09:00:00Z")
private val STATUS_MAPPER = PremiumStatusUiMapper(PremiumPlanUiMapper())

@RunWith(Parameterized::class)
class PremiumStatusUiMapperTest(
    private val status: PremiumStatusPresentationModel,
    private val expected: PremiumStatusUiModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(PremiumStatusPresentationModel.Free, PremiumStatusUiModel.Free),
            arrayOf(PremiumStatusPresentationModel.Pending, PremiumStatusUiModel.Pending),
            arrayOf(
                PremiumStatusPresentationModel.Subscribed(
                    PremiumPlanPresentationModel.Yearly,
                    TRIAL_END
                ),
                PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Yearly, TRIAL_END.toEpochMilli())
            ),
            arrayOf(
                PremiumStatusPresentationModel.Subscribed(
                    PremiumPlanPresentationModel.Monthly,
                    null
                ),
                PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Monthly, null)
            ),
            arrayOf(
                PremiumStatusPresentationModel.Lifetime(subscriptionRenewsToo = true),
                PremiumStatusUiModel.Lifetime(subscriptionRenewsToo = true)
            )
        )
    }

    @Test
    fun `When mapping the status then each keeps what it shows`() {
        assertEquals(expected, STATUS_MAPPER.toUi(status))
    }
}

class SubscriptionUiMappersTest {
    private val card = PaywallProductPresentationModel(
        PremiumPlanPresentationModel.Lifetime,
        "€59.99",
        freeTrial = Period.ofWeeks(2),
        offerToken = "t",
        current = false,
        choosable = true
    )
    private val cardUi = PaywallProductUiModel(
        PremiumPlanUiModel.Lifetime,
        "€59.99",
        trial = TrialLengthUiModel(months = 0, days = 14),
        offerToken = "t",
        current = false,
        choosable = true
    )

    @Test
    fun `Given each paywall state when mapping then the screen gets its own`() {
        val mapper = PaywallViewStateUiMapper(STATUS_MAPPER, PremiumPlanUiMapper())

        assertEquals(
            listOf(
                PaywallViewStateUiModel.Loading,
                PaywallViewStateUiModel.Ready(
                    PaywallOffersUiModel(PremiumStatusUiModel.Pending, listOf(cardUi))
                ),
                PaywallViewStateUiModel.Working,
                PaywallViewStateUiModel.Unavailable,
                PaywallViewStateUiModel.Unreachable
            ),
            listOf(
                PaywallViewState.Loading,
                PaywallViewState.Ready(PremiumStatusPresentationModel.Pending, listOf(card)),
                PaywallViewState.Working,
                PaywallViewState.Unavailable,
                PaywallViewState.Unreachable
            ).map(mapper::toUi)
        )
    }

    @Test
    fun `When a card is chosen then it maps back for the view model`() {
        assertEquals(card, PaywallProductPresentationMapper().toPresentation(cardUi))
    }

    @Test
    fun `Given each context then it maps to the same one`() {
        assertEquals(
            PaywallContextPresentationModel.entries,
            PaywallContextUiModel.entries.map(PaywallContextPresentationMapper()::toPresentation)
        )
    }

    @Test
    fun `When a notification is presented then the screen's presenter gets the event`() = runTest {
        val presenter = PaywallEventPresenter()
        val mapper = PaywallNotificationUiMapper(presenter, STATUS_MAPPER)
        val event = async(start = UNDISPATCHED) { presenter.events.first() }

        mapper.toUi(PaywallPresentationNotification.NothingToRestore).present()

        assertEquals(
            PaywallEventUiModel.Message(PaywallMessageUiModel.NothingToRestore),
            event.await()
        )
    }

    @Test
    fun `When Premium unlocks then the event carries the status`() = runTest {
        val presenter = PaywallEventPresenter()
        val mapper = PaywallNotificationUiMapper(presenter, STATUS_MAPPER)
        val event = async(start = UNDISPATCHED) { presenter.events.first() }

        mapper.toUi(
            PaywallPresentationNotification.Unlocked(PremiumStatusPresentationModel.Lifetime(false))
        ).present()

        assertEquals(
            PaywallEventUiModel.Unlocked(PremiumStatusUiModel.Lifetime(false)),
            event.await()
        )
    }

    @Test
    fun `Given each Premium section state when mapping then the note comes along`() {
        val mapper = PremiumSectionViewStateUiMapper(STATUS_MAPPER)

        assertEquals(
            PremiumSectionViewStateUiModel.Ready(
                PremiumSectionUiModel(PremiumStatusUiModel.Free, PremiumNoteUiModel.Unreachable)
            ),
            mapper.toUi(
                PremiumSectionViewState.Ready(
                    PremiumStatusPresentationModel.Free,
                    PremiumNotePresentationModel.Unreachable
                )
            )
        )
        assertEquals(
            PremiumNoteUiModel.entries,
            PremiumNotePresentationModel.entries.map {
                val ready = mapper.toUi(
                    PremiumSectionViewState.Ready(PremiumStatusPresentationModel.Free, it)
                ) as PremiumSectionViewStateUiModel.Ready
                ready.section.note
            }
        )
        assertEquals(
            PremiumSectionViewStateUiModel.Checking,
            mapper.toUi(PremiumSectionViewState.Checking)
        )
    }

    @Test
    fun `When mapping a paywall message then no purchase found is information, the rest errors`() {
        val mapper = PaywallMessageBehaviourUiMapper()

        assertEquals(
            PaywallMessageBehaviourUiModel(SnackbarTone.Information, 8_000L),
            mapper.toUi(PaywallMessageUiModel.NothingToRestore)
        )
        assertEquals(
            PaywallMessageBehaviourUiModel(SnackbarTone.Error, 8_000L),
            mapper.toUi(PaywallMessageUiModel.PlayUnreachable)
        )
        assertEquals(
            PaywallMessageBehaviourUiModel(SnackbarTone.Error, 8_000L),
            mapper.toUi(PaywallMessageUiModel.SomethingWentWrong)
        )
    }

    @Test
    fun `When mapping an offline message then it is information for 8 seconds`() {
        // Information is polite for TalkBack (BRD D166).
        val mapper = PaywallMessageBehaviourUiMapper()

        listOf(
            PaywallMessageUiModel.OfflineToBuy,
            PaywallMessageUiModel.OfflineToRestore,
            PaywallMessageUiModel.OfflineToManage
        ).forEach { message ->
            assertEquals(
                message.name,
                PaywallMessageBehaviourUiModel(SnackbarTone.Information, 8_000L),
                mapper.toUi(message)
            )
        }
    }

    @Test
    fun `When an offline notification is presented then the screen gets its message`() = runTest {
        val presenter = PaywallEventPresenter()
        val mapper = PaywallNotificationUiMapper(presenter, STATUS_MAPPER)
        val events = async(start = UNDISPATCHED) { presenter.events.take(3).toList() }

        mapper.toUi(PaywallPresentationNotification.OfflineToBuy).present()
        mapper.toUi(PaywallPresentationNotification.OfflineToRestore).present()
        mapper.toUi(PaywallPresentationNotification.OfflineToManage).present()

        assertEquals(
            listOf(
                PaywallEventUiModel.Message(PaywallMessageUiModel.OfflineToBuy),
                PaywallEventUiModel.Message(PaywallMessageUiModel.OfflineToRestore),
                PaywallEventUiModel.Message(PaywallMessageUiModel.OfflineToManage)
            ),
            events.await()
        )
    }

    @Test
    fun `Given the trial banner state when mapping then its end is in milliseconds`() {
        val mapper = TrialBannerViewStateUiMapper()

        assertEquals(
            TrialBannerViewStateUiModel.Shown(TRIAL_END.toEpochMilli(), true, false, false),
            mapper.toUi(
                TrialBannerViewState.Shown(
                    TRIAL_END,
                    endsToday = true,
                    renews = false,
                    offlineToManage = false
                )
            )
        )
        assertEquals(
            TrialBannerViewStateUiModel.Shown(TRIAL_END.toEpochMilli(), false, true, true),
            mapper.toUi(
                TrialBannerViewState.Shown(
                    TRIAL_END,
                    endsToday = false,
                    renews = true,
                    offlineToManage = true
                )
            )
        )
        assertEquals(TrialBannerViewStateUiModel.Hidden, mapper.toUi(TrialBannerViewState.Hidden))
    }
}
