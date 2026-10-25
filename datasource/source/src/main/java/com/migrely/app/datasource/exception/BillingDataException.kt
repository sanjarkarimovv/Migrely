package com.migrely.app.datasource.exception

/** Google Play did not answer a billing request. */
open class BillingDataException(message: String? = null, cause: Throwable? = null) :
    DataException(message ?: "Google Play billing failed", cause)
