package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.EntitlementDomainModel

/** The entitlement as last decided; [EntitlementDomainModel.NONE] when nothing can be read. */
interface GetEntitlementRepository {
    fun entitlement(): EntitlementDomainModel
}
