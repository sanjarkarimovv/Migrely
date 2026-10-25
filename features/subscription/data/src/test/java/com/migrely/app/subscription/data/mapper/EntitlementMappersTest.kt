package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class EntitlementMappersTest(
    private val stored: EntitlementDataModel,
    private val entitlement: EntitlementDomainModel
) {
    companion object {
        private fun data(
            kind: EntitlementKindDataModel,
            product: String?,
            basePlan: String?,
            trialEnd: Long? = null
        ) = EntitlementDataModel(
            kind,
            product,
            basePlan,
            "t",
            1_000L,
            true,
            trialEnd,
            3_000L,
            false
        )

        private fun domain(
            kind: EntitlementKindDomainModel,
            plan: PremiumPlanDomainModel?,
            trialEnd: Long? = null
        ) = EntitlementDomainModel(
            kind,
            plan,
            "t",
            Instant.ofEpochMilli(1_000L),
            true,
            trialEnd?.let(Instant::ofEpochMilli),
            Instant.ofEpochMilli(3_000L),
            false
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun cases(): Collection<Array<Any>> = listOf(
            arrayOf(
                data(EntitlementKindDataModel.Subscription, "migrely_premium", "yearly", 2_000L),
                domain(
                    EntitlementKindDomainModel.Subscription,
                    PremiumPlanDomainModel.Yearly,
                    2_000L
                )
            ),
            arrayOf(
                data(EntitlementKindDataModel.Subscription, "migrely_premium", "monthly"),
                domain(EntitlementKindDomainModel.Subscription, PremiumPlanDomainModel.Monthly)
            ),
            arrayOf(
                data(EntitlementKindDataModel.Subscription, "migrely_premium", null),
                domain(EntitlementKindDomainModel.Subscription, null)
            ),
            arrayOf(
                data(EntitlementKindDataModel.Lifetime, "migrely_lifetime", null),
                domain(EntitlementKindDomainModel.Lifetime, PremiumPlanDomainModel.Lifetime)
            ),
            arrayOf(
                data(EntitlementKindDataModel.Pending, "migrely_premium", "monthly"),
                domain(EntitlementKindDomainModel.Pending, PremiumPlanDomainModel.Monthly)
            ),
            arrayOf(
                data(EntitlementKindDataModel.None, null, null),
                domain(EntitlementKindDomainModel.None, null)
            ),
            arrayOf(
                data(EntitlementKindDataModel.Lifetime, "migrely_lifetime", null)
                    .copy(acknowledgementPending = true),
                domain(EntitlementKindDomainModel.Lifetime, PremiumPlanDomainModel.Lifetime)
                    .copy(acknowledgementPending = true)
            )
        )
    }

    @Test
    fun `When reading the stored entitlement then it maps to the domain`() {
        assertEquals(entitlement, EntitlementDomainMapper().toDomain(stored))
    }

    @Test
    fun `When storing the entitlement then it maps back the same`() {
        assertEquals(stored, EntitlementDataMapper().toData(entitlement))
    }
}

class EntitlementDomainMapperNothingStoredTest {
    @Test
    fun `Given nothing stored when reading then it is none`() {
        assertEquals(EntitlementDomainModel.NONE, EntitlementDomainMapper().toDomain(null))
    }
}
