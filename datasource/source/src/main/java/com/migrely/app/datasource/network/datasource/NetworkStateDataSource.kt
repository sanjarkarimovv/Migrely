package com.migrely.app.datasource.network.datasource

import com.migrely.app.datasource.network.model.NetworkStateDataModel

/**
 * The phone's network state, read at the moment it is asked, with no cache and no listener (BRD D166). It needs
 * `ACCESS_NETWORK_STATE` and nothing more: the app still has no `INTERNET`.
 */
interface NetworkStateDataSource {
    /** Never fails: [NetworkStateDataModel.Unknown] when Android does not say. */
    fun networkState(): NetworkStateDataModel
}
