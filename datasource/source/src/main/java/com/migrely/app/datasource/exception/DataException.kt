package com.migrely.app.datasource.exception

/** Base type for failures raised by datasources. Repositories turn these into failure values. */
open class DataException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)
