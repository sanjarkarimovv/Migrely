package com.migrely.app.datasource.preferences.datasource

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private val LIFETIME = EntitlementDataModel(
    kind = EntitlementKindDataModel.Lifetime,
    productId = "migrely_lifetime",
    basePlanId = null,
    purchaseToken = "token",
    purchaseTimeMillis = 1_000L,
    autoRenewing = false,
    trialEndMillis = null,
    lastCheckMillis = 3_000L,
    subscriptionRenewsToo = false
)

/**
 * On real preferences files: the entitlement lands in `migrely_entitlement.xml`, the file the
 * backup rules leave out, and never in `migrely_preferences.xml`, which is backed up (BRD D115).
 */
@RunWith(AndroidJUnit4::class)
class PreferencesDataSourceImplFilesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val settings = context.getSharedPreferences(
        PreferencesDataSourceImpl.FILE,
        Context.MODE_PRIVATE
    )
    private val entitlement = context.getSharedPreferences(
        PreferencesDataSourceImpl.ENTITLEMENT_FILE,
        Context.MODE_PRIVATE
    )
    private val classUnderTest = PreferencesDataSourceImpl(settings, entitlement)

    @Before
    fun setUp() {
        settings.edit().clear().commit()
        entitlement.edit().clear().commit()
    }

    @After
    fun tearDown() = setUp()

    @Test
    fun givenAnEntitlement_whenSaved_thenItIsInItsOwnFileOnly() {
        // When
        classUnderTest.setEntitlement(LIFETIME)
        classUnderTest.markPaywallShown(PaywallTriggerDataModel.Onboarding, atMillis = 5_000L)

        // Then
        assertEquals("migrely_entitlement", PreferencesDataSourceImpl.ENTITLEMENT_FILE)
        assertEquals("lifetime", entitlement.getString("entitlement_kind", null))
        assertTrue(settings.all.keys.none { it.startsWith("entitlement_") })
        assertTrue(entitlement.all.keys.none { it.startsWith("paywall_") })
        assertEquals(LIFETIME, classUnderTest.entitlement())
    }

    @Test
    fun givenEverythingStored_whenDeletingAllData_thenBothFilesAreEmpty() {
        // Given
        classUnderTest.setEntitlement(LIFETIME)
        classUnderTest.setPeriodTracking(false)

        // When
        classUnderTest.clear()

        // Then
        assertNull(classUnderTest.entitlement())
        assertTrue(entitlement.all.isEmpty())
        assertTrue(settings.all.isEmpty())
        assertFalse(classUnderTest.paywallShown(PaywallTriggerDataModel.Onboarding))
    }
}
