package com.migrely.app.datasource.file

import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.io.FileNotFoundException
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FileFailuresTest {
    @Test
    fun `Given the phone is full when writing then storage full`() {
        assertThrows(StorageFullDataException::class.java) {
            file {
                throw IOException(
                    "write failed",
                    IOException("write failed: ENOSPC (No space left on device)")
                )
            }
        }
    }

    @Test
    fun `Given any other failure when reading or writing then a file failure`() {
        listOf(
            FileNotFoundException("gone"),
            SecurityException("permission revoked"),
            IllegalArgumentException("bad uri")
        ).forEach { failure ->
            assertThrows(FileDataException::class.java) { file { throw failure } }
        }
    }

    @Test
    fun `Given it works then its value`() {
        assertEquals(3, file { 3 })
    }
}
