package com.migrely.app.subscription.data.repository

import com.migrely.app.datasource.network.datasource.NetworkStateDataSource
import com.migrely.app.subscription.data.mapper.NetworkStateDomainMapper
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel
import com.migrely.app.subscription.domain.repository.GetNetworkStateRepository

/** The phone's network state for buy, restore and manage, read at each call (BRD D166). */
class NetworkStateRepository(
    private val networkStateDataSource: NetworkStateDataSource,
    private val networkStateDomainMapper: NetworkStateDomainMapper
) : GetNetworkStateRepository {
    override fun networkState(): NetworkStateDomainModel =
        networkStateDomainMapper.toDomain(networkStateDataSource.networkState())
}
