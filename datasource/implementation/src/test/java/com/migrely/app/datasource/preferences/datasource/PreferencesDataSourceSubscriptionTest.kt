package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val YEARLY_TRIAL = EntitlementDataModel(
    kind = EntitlementKindDataModel.Subscription,
    productId = "migrely_premium",
    basePlanId = "yearly",
    purchaseToken = "token",
    purchaseTimeMillis = 1_000L,
    autoRenewing = true,
    trialEndMillis = 2_000L,
    lastCheckMillis = 3_000L,
    subscriptionRenewsToo = false
)

/** The entitlement cache, the paywall marks and the trial banner (change add-subscription). */
class PreferencesDataSourceSubscriptionTest {
    private val editor: SharedPreferences.Editor = mock {
        on { putBoolean(any(), any()) } doReturn it
        on { putString(any(), anyOrNull()) } doReturn it
        on { putLong(any(), any()) } doReturn it
        on { remove(any()) } doReturn it
        on { commit() } doReturn true
    }

    @Test
    fun `Given nothing stored then there is no entitlement and no paywall has shown`() {
        val classUnderTest = PreferencesDataSourceImpl(mock { on { edit() } doReturn editor })

        assertNull(classUnderTest.entitlement())
        assertFalse(classUnderTest.paywallShown(PaywallTriggerDataModel.Onboarding))
        assertNull(classUnderTest.lastPaywallShownMillis())
        assertNull(classUnderTest.trialBannerDismissedFor())
    }

    @Test
    fun `Given a stored entitlement when reading then every field comes back`() {
        val preferences: SharedPreferences = mock {
            on { getString("entitlement_kind", null) } doReturn "subscription"
            on { getString("entitlement_product", null) } doReturn "migrely_premium"
            on { getString("entitlement_base_plan", null) } doReturn "yearly"
            on { getString("entitlement_token", null) } doReturn "token"
            on { contains("entitlement_purchase_time") } doReturn true
            on { getLong("entitlement_purchase_time", 0L) } doReturn 1_000L
            on { getBoolean("entitlement_auto_renewing", false) } doReturn true
            on { contains("entitlement_trial_end") } doReturn true
            on { getLong("entitlement_trial_end", 0L) } doReturn 2_000L
            on { contains("entitlement_last_check") } doReturn true
            on { getLong("entitlement_last_check", 0L) } doReturn 3_000L
        }

        assertEquals(YEARLY_TRIAL, PreferencesDataSourceImpl(preferences).entitlement())
    }

    @Test
    fun `Given a kind this version does not know when reading then there is no entitlement`() {
        val preferences: SharedPreferences = mock {
            on { getString("entitlement_kind", null) } doReturn "family"
        }

        assertNull(PreferencesDataSourceImpl(preferences).entitlement())
    }

    @Test
    fun `When saving an entitlement then the stored keys never change and unknown times go`() {
        val classUnderTest = PreferencesDataSourceImpl(mock { on { edit() } doReturn editor })

        classUnderTest.setEntitlement(
            YEARLY_TRIAL.copy(
                kind = EntitlementKindDataModel.Lifetime,
                trialEndMillis = null,
                subscriptionRenewsToo = true
            )
        )

        inOrder(editor) {
            verify(editor).putString("entitlement_kind", "lifetime")
            verify(editor).putString("entitlement_product", "migrely_premium")
            verify(editor).putString("entitlement_base_plan", "yearly")
            verify(editor).putString("entitlement_token", "token")
            verify(editor).putLong("entitlement_purchase_time", 1_000L)
            verify(editor).putBoolean("entitlement_auto_renewing", true)
            verify(editor).remove("entitlement_trial_end")
            verify(editor).putLong("entitlement_last_check", 3_000L)
            verify(editor).putBoolean("entitlement_subscription_too", true)
            verify(editor).commit()
        }
    }

    @Test
    fun `When a paywall shows itself then its trigger and time are kept together`() {
        val classUnderTest = PreferencesDataSourceImpl(mock { on { edit() } doReturn editor })

        classUnderTest.markPaywallShown(PaywallTriggerDataModel.FifthMigraine, atMillis = 5_000L)

        inOrder(editor) {
            verify(editor).putBoolean("paywall_shown_fifth_migraine", true)
            verify(editor).putLong("paywall_last_shown", 5_000L)
            verify(editor).commit()
        }
    }

    @Test
    fun `Given marks stored when reading then they come back`() {
        val preferences: SharedPreferences = mock {
            on { getBoolean("paywall_shown_onboarding", false) } doReturn true
            on { contains("paywall_last_shown") } doReturn true
            on { getLong("paywall_last_shown", 0L) } doReturn 5_000L
            on { contains("trial_banner_dismissed") } doReturn true
            on { getLong("trial_banner_dismissed", 0L) } doReturn 2_000L
        }
        val classUnderTest = PreferencesDataSourceImpl(preferences)

        assertTrue(classUnderTest.paywallShown(PaywallTriggerDataModel.Onboarding))
        assertFalse(classUnderTest.paywallShown(PaywallTriggerDataModel.FifthMigraine))
        assertEquals(5_000L, classUnderTest.lastPaywallShownMillis())
        assertEquals(2_000L, classUnderTest.trialBannerDismissedFor())
    }

    @Test
    fun `When dismissing the trial banner then its trial end is kept`() {
        val classUnderTest = PreferencesDataSourceImpl(mock { on { edit() } doReturn editor })

        classUnderTest.dismissTrialBanner(2_000L)

        verify(editor).putLong("trial_banner_dismissed", 2_000L)
    }

    @Test
    fun `Given the disk refuses when saving an entitlement then throws`() {
        val refusing: SharedPreferences.Editor = mock {
            on { putBoolean(any(), any()) } doReturn it
            on { putString(any(), anyOrNull()) } doReturn it
            on { putLong(any(), any()) } doReturn it
            on { remove(any()) } doReturn it
            on { commit() } doReturn false
        }
        val classUnderTest = PreferencesDataSourceImpl(mock { on { edit() } doReturn refusing })

        assertThrows(FileDataException::class.java) { classUnderTest.setEntitlement(YEARLY_TRIAL) }
    }
}
