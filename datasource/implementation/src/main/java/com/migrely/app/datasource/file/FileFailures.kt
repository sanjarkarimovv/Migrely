package com.migrely.app.datasource.file

import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.io.IOException

private const val NO_SPACE = "ENOSPC"

/**
 * Runs a file call and turns its failures into the datasource exceptions: a full phone is
 * [StorageFullDataException], anything else that stops the file is [FileDataException]. A document provider is another
 * app, and its failures cross Binder as whatever runtime exception it threw, so those are caught too: none of them may
 * reach a use case running in the background, where nothing would catch it.
 */
@Suppress("SwallowedException", "TooGenericExceptionCaught")
internal inline fun <T> file(block: () -> T): T = try {
    block()
} catch (exception: IOException) {
    throw if (exception.isNoSpace()) {
        StorageFullDataException(
            exception
        )
    } else {
        FileDataException(cause = exception)
    }
} catch (exception: SecurityException) {
    throw FileDataException(cause = exception)
} catch (exception: IllegalArgumentException) {
    throw FileDataException(cause = exception)
} catch (exception: IllegalStateException) {
    throw FileDataException(cause = exception)
} catch (exception: UnsupportedOperationException) {
    throw FileDataException(cause = exception)
} catch (exception: NullPointerException) {
    throw FileDataException(cause = exception)
}

internal fun Throwable.isNoSpace(): Boolean =
    generateSequence(this, Throwable::cause).any { it.message?.contains(NO_SPACE) == true }
