package com.migrely.app.datasource.network.datasource

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.migrely.app.datasource.network.model.NetworkStateDataModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class NetworkStateDataSourceImplTest {
    private val connectivityManager: ConnectivityManager = mock()
    private val classUnderTest = NetworkStateDataSourceImpl(connectivityManager)

    private fun network(internet: Boolean): Network {
        val network: Network = mock()
        val capabilities: NetworkCapabilities = mock {
            on { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } doReturn internet
        }
        given(connectivityManager.getNetworkCapabilities(network)).willReturn(capabilities)
        return network
    }

    @Suppress("DEPRECATION")
    private fun givenNetworks(active: Network?, vararg all: Network) {
        given(connectivityManager.activeNetwork).willReturn(active)
        given(connectivityManager.allNetworks).willReturn(arrayOf(*all))
    }

    @Test
    fun `Given the active network has internet then online`() {
        val wifi = network(internet = true)
        givenNetworks(active = wifi, wifi)

        assertEquals(NetworkStateDataModel.Online, classUnderTest.networkState())
    }

    @Test
    fun `Given no active network but another one has internet then online`() {
        // The default network blocked for Migrely is not blocked for Google Play (BRD EC-BIL-20).
        givenNetworks(active = null, network(internet = false), network(internet = true))

        assertEquals(NetworkStateDataModel.Online, classUnderTest.networkState())
    }

    @Test
    fun `Given an active network without internet but another one with it then online`() {
        val local = network(internet = false)
        givenNetworks(active = local, local, network(internet = true))

        assertEquals(NetworkStateDataModel.Online, classUnderTest.networkState())
    }

    @Test
    fun `Given no network at all, as in airplane mode, then offline`() {
        givenNetworks(active = null)

        assertEquals(NetworkStateDataModel.Offline, classUnderTest.networkState())
    }

    @Test
    fun `Given only networks without internet then offline`() {
        val local = network(internet = false)
        givenNetworks(active = local, local, network(internet = false))

        assertEquals(NetworkStateDataModel.Offline, classUnderTest.networkState())
    }

    @Test
    fun `Given a network Android has no capabilities for then it does not count`() {
        val gone: Network = mock()
        givenNetworks(active = gone, gone)

        assertEquals(NetworkStateDataModel.Offline, classUnderTest.networkState())
    }

    @Test
    fun `Given the permission is missing then unknown, which never blocks a purchase`() {
        given(
            connectivityManager.activeNetwork
        ).willThrow(SecurityException("ACCESS_NETWORK_STATE"))

        assertEquals(NetworkStateDataModel.Unknown, classUnderTest.networkState())
    }

    @Test
    @Suppress("DEPRECATION")
    fun `Given listing the networks fails then unknown`() {
        given(connectivityManager.activeNetwork).willReturn(null)
        given(connectivityManager.allNetworks).willThrow(IllegalStateException())

        assertEquals(NetworkStateDataModel.Unknown, classUnderTest.networkState())
    }

    @Test
    fun `Given Android has no connectivity service then unknown`() {
        assertEquals(
            NetworkStateDataModel.Unknown,
            NetworkStateDataSourceImpl(null).networkState()
        )
    }
}
