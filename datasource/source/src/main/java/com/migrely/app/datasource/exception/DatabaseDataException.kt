package com.migrely.app.datasource.exception

class DatabaseDataException(cause: Throwable? = null) :
    DataException("Database operation failed", cause)
