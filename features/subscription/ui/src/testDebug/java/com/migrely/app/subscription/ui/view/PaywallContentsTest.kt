package com.migrely.app.subscription.ui.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallOffersUiModel
import com.migrely.app.subscription.ui.model.PaywallProductUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.TrialLengthUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LINE =
    "Purchases need Google Play and an internet connection. Your diary works offline."
private const val UNREACHABLE_TEXT =
    "Check your connection, then try again. Your diary works offline."
private const val RESTORE = "Restore purchases"
private const val BENEFITS = "What Premium adds"
private const val MANAGE = "Manage subscription"

/** The column's 16dp and a little: no other block fits between the terms and the line. */
private val SPACING_LIMIT = 24.dp

private val YEARLY = PaywallProductUiModel(
    PremiumPlanUiModel.Yearly,
    "$29.99",
    TrialLengthUiModel(months = 0, days = 7),
    "yearly",
    current = false,
    choosable = true
)
private val MONTHLY = PaywallProductUiModel(
    PremiumPlanUiModel.Monthly,
    "$4.99",
    null,
    "monthly",
    current = false,
    choosable = true
)
private val LIFETIME = PaywallProductUiModel(
    PremiumPlanUiModel.Lifetime,
    "$59.99",
    null,
    null,
    current = false,
    choosable = true
)
private val PRODUCTS = listOf(YEARLY, MONTHLY, LIFETIME)

/** The line that says what a purchase depends on, and the order around it (BRD D153). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1600dp")
class PaywallContentsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(viewState: PaywallViewStateUiModel, chosen: PremiumPlanUiModel? = null) {
        rule.setContent {
            MigrelyTheme {
                PaywallContents(
                    context = PaywallContextUiModel.Report,
                    viewState = viewState,
                    chosen = chosen,
                    unlocked = null,
                    actions = PaywallActions({}, {}, {}, {}, {}, {}, {}),
                    messages = {}
                )
            }
        }
    }

    private fun ready(status: PremiumStatusUiModel, products: List<PaywallProductUiModel>) =
        PaywallViewStateUiModel.Ready(PaywallOffersUiModel(status, products))

    private fun topOf(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().top

    private fun bottomOf(text: String): Dp =
        rule.onNodeWithText(text).getUnclippedBoundsInRoot().bottom

    private fun assertLineDirectlyUnder(terms: String) {
        rule.onNodeWithText(LINE).assertIsDisplayed()
        assertTrue("the line is under the terms", topOf(LINE) >= bottomOf(terms))
        assertTrue("nothing sits between them", topOf(LINE) - bottomOf(terms) < SPACING_LIMIT)
    }

    @Test
    fun `Yearly shows the line under its terms`() {
        show(ready(PremiumStatusUiModel.Free, PRODUCTS))

        assertLineDirectlyUnder(
            "Renews automatically. Cancel anytime in Google Play. " +
                "Cancel before the trial ends and you won't be charged."
        )
    }

    @Test
    fun `Monthly shows the line under its terms`() {
        show(ready(PremiumStatusUiModel.Free, PRODUCTS), PremiumPlanUiModel.Monthly)

        assertLineDirectlyUnder("Renews automatically. Cancel anytime in Google Play.")
    }

    @Test
    fun `Lifetime shows the line under its terms`() {
        show(ready(PremiumStatusUiModel.Free, PRODUCTS), PremiumPlanUiModel.Lifetime)

        assertLineDirectlyUnder("One payment. Nothing renews.")
    }

    @Test
    fun `The buy button stays above the line`() {
        show(ready(PremiumStatusUiModel.Free, PRODUCTS))

        assertTrue(bottomOf("Start free trial") <= topOf(LINE))
    }

    @Test
    fun `A pending payment shows its notice and the line`() {
        show(ready(PremiumStatusUiModel.Pending, PRODUCTS))

        rule.onNodeWithText("Payment pending").assertIsDisplayed()
        rule.onNodeWithText(LINE).assertIsDisplayed()
    }

    @Test
    fun `A subscriber moving plan sees the line above Manage subscription`() {
        show(
            ready(
                PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Monthly, null),
                listOf(
                    MONTHLY.copy(current = true, choosable = false),
                    YEARLY,
                    LIFETIME
                )
            )
        )

        assertTrue(bottomOf(LINE) <= topOf(MANAGE))
    }

    @Test
    fun `A lifetime owner has no button and no line`() {
        show(
            ready(
                PremiumStatusUiModel.Lifetime(subscriptionRenewsToo = false),
                listOf(LIFETIME.copy(current = true, choosable = false))
            )
        )

        rule.onNodeWithText(LINE).assertDoesNotExist()
    }

    @Test
    fun `A subscriber with nothing to move to has no line and can still manage`() {
        show(
            ready(
                PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Yearly, null),
                listOf(YEARLY.copy(current = true, choosable = false))
            )
        )

        rule.onNodeWithText(LINE).assertDoesNotExist()
        rule.onNodeWithText(MANAGE).assertIsDisplayed()
    }

    @Test
    fun `While loading there is no line`() {
        show(PaywallViewStateUiModel.Loading)

        rule.onNodeWithText(LINE).assertDoesNotExist()
    }

    @Test
    fun `With no region there is no line`() {
        show(PaywallViewStateUiModel.Unavailable)

        rule.onNodeWithText("Premium isn't available in your region yet").assertIsDisplayed()
        rule.onNodeWithText(LINE).assertDoesNotExist()
    }

    @Test
    fun `With Play unreachable the notice speaks about the diary and there is no line`() {
        show(PaywallViewStateUiModel.Unreachable)

        rule.onNodeWithText(UNREACHABLE_TEXT).assertIsDisplayed()
        rule.onNodeWithText("Try again").assertIsDisplayed()
        rule.onNodeWithText(LINE).assertDoesNotExist()
    }

    @Test
    fun `Restore comes before what Premium adds with the plans`() {
        show(ready(PremiumStatusUiModel.Free, PRODUCTS))

        assertTrue(bottomOf(RESTORE) <= topOf(BENEFITS))
    }

    @Test
    fun `Restore comes before what Premium adds while loading`() {
        show(PaywallViewStateUiModel.Loading)

        assertTrue(bottomOf(RESTORE) <= topOf(BENEFITS))
    }

    @Test
    fun `Restore comes before what Premium adds with no region`() {
        show(PaywallViewStateUiModel.Unavailable)

        assertTrue(bottomOf(RESTORE) <= topOf(BENEFITS))
    }

    @Test
    fun `Restore comes before what Premium adds with Play unreachable`() {
        show(PaywallViewStateUiModel.Unreachable)

        assertTrue(bottomOf(RESTORE) <= topOf(BENEFITS))
    }
}
