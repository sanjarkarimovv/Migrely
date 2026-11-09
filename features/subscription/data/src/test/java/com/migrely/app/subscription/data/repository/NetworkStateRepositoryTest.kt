package com.migrely.app.subscription.data.repository

import com.migrely.app.datasource.network.datasource.NetworkStateDataSource
import com.migrely.app.datasource.network.model.NetworkStateDataModel
import com.migrely.app.subscription.data.mapper.NetworkStateDomainMapper
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class NetworkStateRepositoryTest {
    private val networkStateDataSource: NetworkStateDataSource = mock()
    private val classUnderTest =
        NetworkStateRepository(networkStateDataSource, NetworkStateDomainMapper())

    @Test
    fun `When asked then each state Android reports, read again at every call`() {
        mapOf(
            NetworkStateDataModel.Online to NetworkStateDomainModel.Online,
            NetworkStateDataModel.Offline to NetworkStateDomainModel.Offline,
            NetworkStateDataModel.Unknown to NetworkStateDomainModel.Unknown
        ).forEach { (data, domain) ->
            given(networkStateDataSource.networkState()).willReturn(data)

            assertEquals(data.name, domain, classUnderTest.networkState())
        }
    }
}
