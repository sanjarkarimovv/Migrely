package com.migrely.app.datasource.document.datasource

/**
 * Text files the user picked through the system's file picker (BRD BAK-F5), by their content URI. No storage permission
 * is needed. Both functions block. They throw [com.migrely.app.datasource.exception.StorageFullDataException] when the
 * phone is full and [com.migrely.app.datasource.exception.FileDataException] when the file cannot be read or written.
 */
interface DocumentDataSource {
    /** Replaces the file's content with [text], in UTF-8. */
    fun write(uri: String, text: String)

    /** The file's text, read as UTF-8. A file larger than [limitBytes] throws a `FileDataException`. */
    fun read(uri: String, limitBytes: Long): String
}
