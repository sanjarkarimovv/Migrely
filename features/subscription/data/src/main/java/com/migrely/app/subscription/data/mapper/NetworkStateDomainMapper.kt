package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.network.model.NetworkStateDataModel
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel

class NetworkStateDomainMapper {
    fun toDomain(state: NetworkStateDataModel): NetworkStateDomainModel = when (state) {
        NetworkStateDataModel.Online -> NetworkStateDomainModel.Online
        NetworkStateDataModel.Offline -> NetworkStateDomainModel.Offline
        NetworkStateDataModel.Unknown -> NetworkStateDomainModel.Unknown
    }
}
