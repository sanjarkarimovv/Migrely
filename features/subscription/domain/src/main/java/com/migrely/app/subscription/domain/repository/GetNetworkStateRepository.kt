package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.NetworkStateDomainModel

/** Whether any network on the phone has internet now, read at each call (BRD D166). */
interface GetNetworkStateRepository {
    fun networkState(): NetworkStateDomainModel
}
