package com.migrely.app.datasource.network.datasource

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.migrely.app.datasource.network.model.NetworkStateDataModel

/**
 * Reads [connectivityManager] at each call (BRD D166, EC-BIL-20). Online when any network has
 * [NetworkCapabilities.NET_CAPABILITY_INTERNET]: the active one first, then every other one, because Android gives no
 * active network when the default one is blocked for this app, and a block on Migrely is not a block on Google Play.
 * VALIDATED is not asked, so a captive portal still counts as online. Any failure is [NetworkStateDataModel.Unknown],
 * which never blocks a purchase.
 */
class NetworkStateDataSourceImpl(private val connectivityManager: ConnectivityManager?) :
    NetworkStateDataSource {
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun networkState(): NetworkStateDataModel = try {
        connectivityManager?.let(::stateOf) ?: NetworkStateDataModel.Unknown
    } catch (exception: RuntimeException) {
        NetworkStateDataModel.Unknown
    }

    private fun stateOf(manager: ConnectivityManager): NetworkStateDataModel {
        // There is no synchronous replacement for allNetworks that needs no listener.
        @Suppress("DEPRECATION")
        val online = hasInternet(manager, manager.activeNetwork) ||
            manager.allNetworks.any { network -> hasInternet(manager, network) }
        return if (online) NetworkStateDataModel.Online else NetworkStateDataModel.Offline
    }

    private fun hasInternet(manager: ConnectivityManager, network: Network?): Boolean =
        network != null &&
            manager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
