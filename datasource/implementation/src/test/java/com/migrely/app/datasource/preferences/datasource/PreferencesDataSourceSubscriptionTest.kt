package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.datasource.preferences.model.LaunchedPurchaseDataModel
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
import org.mockito.kotlin.verifyNoInteractions

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

private fun editor(commits: Boolean = true): SharedPreferences.Editor = mock {
    on { putBoolean(any(), any()) } doReturn it
    on { putString(any(), anyOrNull()) } doReturn it
    on { putLong(any(), any()) } doReturn it
    on { remove(any()) } doReturn it
    on { commit() } doReturn commits
}

/**
 * The entitlement cache, in its own file that is never backed up (BRD D115), and the paywall marks
 * and trial banner, which stay with the settings (change add-subscription).
 */
class PreferencesDataSourceSubscriptionTest {
    private val settingsEditor = editor()
    private val entitlementEditor = editor()
    private val settings: SharedPreferences = mock { on { edit() } doReturn settingsEditor }
    private val entitlementFile: SharedPreferences =
        mock { on { edit() } doReturn entitlementEditor }
    private val classUnderTest = PreferencesDataSourceImpl(settings, entitlementFile)

    @Test
    fun `Given nothing stored then there is no entitlement and no paywall has shown`() {
        assertNull(classUnderTest.entitlement())
        assertFalse(classUnderTest.paywallShown(PaywallTriggerDataModel.Onboarding))
        assertNull(classUnderTest.lastPaywallShownMillis())
        assertNull(classUnderTest.trialBannerDismissedFor())
    }

    @Test
    fun `Given a stored entitlement when reading then every field comes from its own file`() {
        val stored: SharedPreferences = mock {
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

        assertEquals(YEARLY_TRIAL, PreferencesDataSourceImpl(settings, stored).entitlement())
        verifyNoInteractions(settings)
    }

    @Test
    fun `Given a kind this version does not know when reading then there is no entitlement`() {
        val stored: SharedPreferences = mock {
            on { getString("entitlement_kind", null) } doReturn "family"
        }

        assertNull(PreferencesDataSourceImpl(settings, stored).entitlement())
    }

    @Test
    fun `When saving an entitlement then its keys go to its own file and unknown times go`() {
        classUnderTest.setEntitlement(
            YEARLY_TRIAL.copy(
                kind = EntitlementKindDataModel.Lifetime,
                trialEndMillis = null,
                subscriptionRenewsToo = true
            )
        )

        inOrder(entitlementEditor) {
            verify(entitlementEditor).putString("entitlement_kind", "lifetime")
            verify(entitlementEditor).putString("entitlement_product", "migrely_premium")
            verify(entitlementEditor).putString("entitlement_base_plan", "yearly")
            verify(entitlementEditor).putString("entitlement_token", "token")
            verify(entitlementEditor).putLong("entitlement_purchase_time", 1_000L)
            verify(entitlementEditor).putBoolean("entitlement_auto_renewing", true)
            verify(entitlementEditor).remove("entitlement_trial_end")
            verify(entitlementEditor).putLong("entitlement_last_check", 3_000L)
            verify(entitlementEditor).putBoolean("entitlement_subscription_too", true)
            verify(entitlementEditor).putBoolean("entitlement_acknowledgement_pending", false)
            verify(entitlementEditor).commit()
        }
        verifyNoInteractions(settingsEditor)
    }

    @Test
    fun `When a paywall shows itself then its trigger and time stay with the settings`() {
        classUnderTest.markPaywallShown(PaywallTriggerDataModel.FifthMigraine, atMillis = 5_000L)

        inOrder(settingsEditor) {
            verify(settingsEditor).putBoolean("paywall_shown_fifth_migraine", true)
            verify(settingsEditor).putLong("paywall_last_shown", 5_000L)
            verify(settingsEditor).commit()
        }
        verifyNoInteractions(entitlementEditor)
    }

    @Test
    fun `Given marks stored when reading then they come back`() {
        val stored: SharedPreferences = mock {
            on { getBoolean("paywall_shown_onboarding", false) } doReturn true
            on { contains("paywall_last_shown") } doReturn true
            on { getLong("paywall_last_shown", 0L) } doReturn 5_000L
            on { contains("trial_banner_dismissed") } doReturn true
            on { getLong("trial_banner_dismissed", 0L) } doReturn 2_000L
        }
        val classUnderTest = PreferencesDataSourceImpl(stored, entitlementFile)

        assertTrue(classUnderTest.paywallShown(PaywallTriggerDataModel.Onboarding))
        assertFalse(classUnderTest.paywallShown(PaywallTriggerDataModel.FifthMigraine))
        assertEquals(5_000L, classUnderTest.lastPaywallShownMillis())
        assertEquals(2_000L, classUnderTest.trialBannerDismissedFor())
    }

    @Test
    fun `When dismissing the trial banner then its trial end is kept`() {
        classUnderTest.dismissTrialBanner(2_000L)

        verify(settingsEditor).putLong("trial_banner_dismissed", 2_000L)
    }

    @Test
    fun `Given the disk refuses when saving an entitlement then throws`() {
        val refusing = editor(commits = false)
        val classUnderTest =
            PreferencesDataSourceImpl(settings, mock { on { edit() } doReturn refusing })

        assertThrows(FileDataException::class.java) { classUnderTest.setEntitlement(YEARLY_TRIAL) }
    }

    @Test
    fun `Given the entitlement's file refuses when deleting all data then throws first`() {
        val refusing: SharedPreferences.Editor = mock {
            on { clear() } doReturn it
            on { commit() } doReturn false
        }
        val classUnderTest =
            PreferencesDataSourceImpl(settings, mock { on { edit() } doReturn refusing })

        assertThrows(FileDataException::class.java) { classUnderTest.clear() }
        verifyNoInteractions(settingsEditor)
    }

    @Test
    fun `When keeping and forgetting a launched purchase then it is in the entitlement file`() {
        classUnderTest.setLaunchedPurchase(
            LaunchedPurchaseDataModel("migrely_premium", "yearly", "P7D", 5_000L)
        )
        classUnderTest.setLaunchedPurchase(null)

        inOrder(entitlementEditor) {
            verify(entitlementEditor).putString("launched_product", "migrely_premium")
            verify(entitlementEditor).putString("launched_base_plan", "yearly")
            verify(entitlementEditor).putString("launched_trial", "P7D")
            verify(entitlementEditor).putLong("launched_at", 5_000L)
            verify(entitlementEditor).remove("launched_product")
            verify(entitlementEditor).remove("launched_at")
        }
        verifyNoInteractions(settingsEditor)
    }

    @Test
    fun `Given a launched purchase kept then it reads back`() {
        val stored: SharedPreferences = mock {
            on { getString("launched_product", null) } doReturn "migrely_lifetime"
            on { contains("launched_at") } doReturn true
            on { getLong("launched_at", 0L) } doReturn 5_000L
        }

        assertEquals(
            LaunchedPurchaseDataModel("migrely_lifetime", null, null, 5_000L),
            PreferencesDataSourceImpl(settings, stored).launchedPurchase()
        )
        assertNull(classUnderTest.launchedPurchase())
    }
}
