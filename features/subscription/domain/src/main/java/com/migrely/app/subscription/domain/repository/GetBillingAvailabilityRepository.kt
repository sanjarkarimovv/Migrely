package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel

interface GetBillingAvailabilityRepository {
    fun billingAvailability(): BillingAvailabilityDomainModel
}
