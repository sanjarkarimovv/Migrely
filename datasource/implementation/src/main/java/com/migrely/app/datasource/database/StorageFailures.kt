package com.migrely.app.datasource.database

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException

/** Runs a database call and turns SQLite failures into the datasource exceptions (profile section 6a). */
@Suppress("SwallowedException")
internal inline fun <T> database(block: () -> T): T = try {
    block()
} catch (exception: SQLiteFullException) {
    throw StorageFullDataException(exception)
} catch (exception: SQLiteException) {
    throw DatabaseDataException(exception)
}
