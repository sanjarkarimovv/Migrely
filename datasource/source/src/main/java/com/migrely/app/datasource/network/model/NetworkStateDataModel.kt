package com.migrely.app.datasource.network.model

/** Whether any network on the phone has internet, as Android reports it (BRD D166). */
enum class NetworkStateDataModel {
    /** At least one network has internet. Validation is not asked for. */
    Online,

    /** No network on the phone has internet. */
    Offline,

    /** Android did not say, for example because the permission is missing. */
    Unknown
}
