package com.migrely.app.datasource.database

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** Runs a database call and turns SQLite failures into the datasource exceptions (profile section 6a). */
@Suppress("SwallowedException")
internal inline fun <T> database(block: () -> T): T = try {
    block()
} catch (exception: SQLiteFullException) {
    throw StorageFullDataException(exception)
} catch (exception: SQLiteException) {
    throw DatabaseDataException(exception)
}

/** The same for a database flow: SQLite failures end it with the datasource exceptions. */
internal fun <T> Flow<T>.databaseFailures(): Flow<T> = catch { exception ->
    throw when (exception) {
        is SQLiteFullException -> StorageFullDataException(exception)
        is SQLiteException -> DatabaseDataException(exception)
        else -> exception
    }
}
