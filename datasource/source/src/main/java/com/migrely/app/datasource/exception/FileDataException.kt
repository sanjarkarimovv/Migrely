package com.migrely.app.datasource.exception

/** A file could not be read or written: it is gone, unreadable, too large, or its storage refused it. */
class FileDataException(message: String? = null, cause: Throwable? = null) :
    DataException(message ?: "File operation failed", cause)
