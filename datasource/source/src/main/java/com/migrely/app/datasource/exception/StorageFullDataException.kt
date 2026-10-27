package com.migrely.app.datasource.exception

/** The device has no free storage left for the write (BRD EC-STO-02). */
class StorageFullDataException(cause: Throwable? = null) :
    DataException("Device storage is full", cause)
