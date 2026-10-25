package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel
import java.time.Instant

interface MarkPaywallShownRepository {
    fun markPaywallShown(
        trigger: PaywallTriggerDomainModel,
        at: Instant
    ): SubscriptionWriteResultDomainModel
}
