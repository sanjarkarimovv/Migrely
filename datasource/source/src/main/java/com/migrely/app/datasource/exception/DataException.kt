package com.migrely.app.datasource.exception

/**
 * Base type for failures raised by datasources. Repositories turn these into failure values.
 * It is unchecked (a [RuntimeException]) so Java callers and test doubles do not treat it as a checked exception.
 */
open class DataException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
