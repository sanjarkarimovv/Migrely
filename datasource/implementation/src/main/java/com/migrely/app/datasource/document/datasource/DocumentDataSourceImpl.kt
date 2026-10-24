package com.migrely.app.datasource.document.datasource

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.file.file
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.OutputStream

private const val TRUNCATE = "wt"
private const val WRITE = "w"
private const val BUFFER_BYTES = 8 * 1024

class DocumentDataSourceImpl(private val contentResolver: ContentResolver) : DocumentDataSource {
    override fun write(uri: String, text: String) {
        val document = Uri.parse(uri)
        try {
            file { open(document).use { it.write(text.toByteArray(Charsets.UTF_8)) } }
        } catch (failure: DataException) {
            // The picker created this file for the export; a half-written one must not pass for a backup.
            deleteQuietly(document)
            throw failure
        }
    }

    override fun read(uri: String, limitBytes: Long): String = file {
        val input = contentResolver.openInputStream(Uri.parse(uri))
            ?: throw FileDataException("Could not open $uri")
        input.use { stream ->
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(BUFFER_BYTES)
            var read = stream.read(buffer)
            while (read >= 0) {
                bytes.write(buffer, 0, read)
                if (bytes.size() >
                    limitBytes
                ) {
                    throw FileDataException("Larger than $limitBytes bytes")
                }
                read = stream.read(buffer)
            }
            bytes.toString(Charsets.UTF_8.name())
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun deleteQuietly(document: Uri) {
        try {
            DocumentsContract.deleteDocument(contentResolver, document)
        } catch (exception: Exception) {
            // Best effort: some providers cannot delete, and the failure is already being reported.
        }
    }

    /** Some providers do not take "wt"; a file just created by the picker is empty, so "w" is as good. */
    @Suppress("SwallowedException")
    private fun open(uri: Uri): OutputStream = try {
        contentResolver.openOutputStream(uri, TRUNCATE)
    } catch (exception: FileNotFoundException) {
        contentResolver.openOutputStream(uri, WRITE)
    } catch (exception: IllegalArgumentException) {
        contentResolver.openOutputStream(uri, WRITE)
    } ?: throw FileDataException("Could not open $uri")
}
