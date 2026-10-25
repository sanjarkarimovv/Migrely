package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import java.time.Duration
import java.time.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementRulesTest {
    private val classUnderTest = EntitlementRules()

    @Test
    fun `Given an active subscription when Play answers then Premium is on`() {
        val entitlement = classUnderTest.answered(
            EntitlementDomainModel.NONE,
            listOf(purchase()),
            NOW
        )

        assertEquals(EntitlementKindDomainModel.Subscription, entitlement.kind)
        assertEquals(NOW, entitlement.lastCheck)
        assertEquals(
            PremiumStatusDomainModel.Subscribed(plan = null, trialEnd = null),
            classUnderTest.status(entitlement, NOW)
        )
    }

    @Test
    fun `Given lifetime and a subscription that renews then lifetime wins and says so`() {
        val purchases = listOf(
            purchase(),
            purchase(PurchaseProductDomainModel.Lifetime, token = "life", autoRenewing = false)
        )

        val entitlement = classUnderTest.answered(EntitlementDomainModel.NONE, purchases, NOW)

        assertEquals(
            PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = true),
            classUnderTest.status(entitlement, NOW)
        )
        assertEquals("life", entitlement.purchaseToken)
    }

    @Test
    fun `Given only a pending purchase then nothing unlocks`() {
        val pending = purchase(state = PurchaseStateDomainModel.Pending)

        val entitlement = classUnderTest.answered(EntitlementDomainModel.NONE, listOf(pending), NOW)

        assertEquals(PremiumStatusDomainModel.Pending, classUnderTest.status(entitlement, NOW))
    }

    @Test
    fun `Given a purchase whose signature does not hold then nothing unlocks`() {
        val forged = purchase(PurchaseProductDomainModel.Lifetime, signatureValid = false)

        val entitlement = classUnderTest.answered(EntitlementDomainModel.NONE, listOf(forged), NOW)

        assertEquals(PremiumStatusDomainModel.Free, classUnderTest.status(entitlement, NOW))
    }

    @Test
    fun `Given Play no longer reports the purchase after a refund then Premium ends`() {
        val entitlement = classUnderTest.answered(subscription(), emptyList(), NOW)

        assertEquals(PremiumStatusDomainModel.Free, classUnderTest.status(entitlement, NOW))
        assertEquals(NOW, entitlement.lastCheck)
    }

    @Test
    fun `Given a purchase made here with the trial then its plan and trial end are kept`() {
        val bought = BoughtDomainModel("sub", PremiumPlanDomainModel.Yearly, Period.ofDays(7))

        val entitlement = classUnderTest.answered(
            EntitlementDomainModel.NONE,
            listOf(purchase(at = NOW)),
            NOW,
            bought
        )

        assertEquals(PremiumPlanDomainModel.Yearly, entitlement.plan)
        assertEquals(NOW.plus(Duration.ofDays(7)), entitlement.trialEnd)
        assertEquals(
            PremiumStatusDomainModel.Subscribed(
                PremiumPlanDomainModel.Yearly,
                NOW.plusSeconds(604_800)
            ),
            classUnderTest.status(entitlement, NOW)
        )
    }

    @Test
    fun `Given the same purchase at the next check then the plan and trial end stay`() {
        val previous = subscription(PremiumPlanDomainModel.Yearly, trialEnd = NOW.plusSeconds(60))

        val entitlement = classUnderTest.answered(previous, listOf(purchase()), NOW)

        assertEquals(PremiumPlanDomainModel.Yearly, entitlement.plan)
        assertEquals(NOW.plusSeconds(60), entitlement.trialEnd)
    }

    @Test
    fun `Given another purchase than the one known then its plan is unknown`() {
        val previous = subscription(PremiumPlanDomainModel.Monthly, token = "old")

        val entitlement = classUnderTest.answered(previous, listOf(purchase(token = "new")), NOW)

        assertEquals(null, entitlement.plan)
        assertEquals("new", entitlement.purchaseToken)
    }

    @Test
    fun `Given the trial has ended then the status no longer shows it`() {
        val ended = subscription(PremiumPlanDomainModel.Yearly, trialEnd = NOW.minusSeconds(1))

        assertEquals(
            PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, trialEnd = null),
            classUnderTest.status(ended, NOW)
        )
    }

    @Test
    fun `Given a check yesterday when Play cannot be reached then the subscription stays on`() {
        val previous = subscription(lastCheck = NOW.minus(Duration.ofDays(1)))

        assertEquals(previous, classUnderTest.unanswered(previous, NOW))
    }

    @Test
    fun `Given the last answer is over 3 days old when Play cannot be reached then it ends`() {
        val previous = subscription(lastCheck = NOW.minus(Duration.ofDays(3)).minusSeconds(1))

        val entitlement = classUnderTest.unanswered(previous, NOW)

        assertEquals(EntitlementKindDomainModel.None, entitlement.kind)
        assertEquals(previous.purchaseToken, entitlement.purchaseToken)
    }

    @Test
    fun `Given lifetime checked long ago when Play cannot be reached then it stays on`() {
        val lifetime = subscription(lastCheck = NOW.minus(Duration.ofDays(90)))
            .copy(kind = EntitlementKindDomainModel.Lifetime)

        assertEquals(lifetime, classUnderTest.unanswered(lifetime, NOW))
    }

    @Test
    fun `Given a check 10 minutes ago then Play is not due, and after 15 minutes it is`() {
        assertFalse(classUnderTest.isDue(subscription(lastCheck = NOW.minusSeconds(600)), NOW))
        assertTrue(classUnderTest.isDue(subscription(lastCheck = NOW.minusSeconds(900)), NOW))
        assertTrue(classUnderTest.isDue(EntitlementDomainModel.NONE, NOW))
    }

    @Test
    fun `Given a pending payment then Play is due every time`() {
        val pending = subscription(lastCheck = NOW.minusSeconds(1))
            .copy(kind = EntitlementKindDomainModel.Pending)

        assertTrue(classUnderTest.isDue(pending, NOW))
    }

    @Test
    fun `Given the clock went back before the last check then Play is due`() {
        assertTrue(classUnderTest.isDue(subscription(lastCheck = NOW.plusSeconds(3_600)), NOW))
    }

    @Test
    fun `Given a trial of 14 days from Play then the trial ends 14 days after the purchase`() {
        val bought = BoughtDomainModel("sub", PremiumPlanDomainModel.Yearly, Period.ofWeeks(2))

        val entitlement =
            classUnderTest.answered(
                EntitlementDomainModel.NONE,
                listOf(purchase(at = NOW)),
                NOW,
                bought
            )

        assertEquals(NOW.plus(Duration.ofDays(14)), entitlement.trialEnd)
    }

    @Test
    fun `Given a clock set back before the last answer when Play is unreachable then it ends`() {
        val previous = subscription(lastCheck = NOW.plus(Duration.ofHours(2)))

        assertEquals(
            EntitlementKindDomainModel.None,
            classUnderTest.unanswered(previous, NOW).kind
        )
    }

    @Test
    fun `Given lifetime when the clock was moved back then it stays on`() {
        val lifetime = subscription(lastCheck = NOW.plus(Duration.ofDays(2)))
            .copy(kind = EntitlementKindDomainModel.Lifetime)

        assertEquals(lifetime, classUnderTest.unanswered(lifetime, NOW))
    }

    @Test
    fun `Given a sheet launched before the app closed then its purchase gets its plan and trial`() {
        val launched = LaunchedPurchaseDomainModel(
            PremiumPlanDomainModel.Yearly,
            Period.ofDays(7),
            NOW.minus(Duration.ofMinutes(3))
        )

        assertEquals(
            BoughtDomainModel("new", PremiumPlanDomainModel.Yearly, Period.ofDays(7)),
            classUnderTest.boughtFrom(
                launched,
                listOf(purchase(token = "new", at = NOW.minus(Duration.ofMinutes(2)))),
                EntitlementDomainModel.NONE,
                NOW
            )
        )
    }

    @Test
    fun `Given a launch older than a day or a purchase before it then it gives nothing`() {
        val old = LaunchedPurchaseDomainModel(
            PremiumPlanDomainModel.Monthly,
            null,
            NOW.minus(Duration.ofDays(2))
        )
        val recent = old.copy(at = NOW.minus(Duration.ofMinutes(1)))

        assertEquals(
            null,
            classUnderTest.boughtFrom(
                old,
                listOf(purchase(at = NOW)),
                EntitlementDomainModel.NONE,
                NOW
            )
        )
        assertEquals(
            null,
            classUnderTest.boughtFrom(
                recent,
                listOf(purchase(at = NOW.minus(Duration.ofDays(30)))),
                EntitlementDomainModel.NONE,
                NOW
            )
        )
    }
}
