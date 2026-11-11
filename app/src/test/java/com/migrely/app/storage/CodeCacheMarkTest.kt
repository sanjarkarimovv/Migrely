package com.migrely.app.storage

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Keeps installd's `code_cache` mark true at every process start, and never stops the start (BRD D170, EC-STO-07). */
class CodeCacheMarkTest {
    private val warnings = mutableListOf<String>()

    @Test
    fun `When kept then the folder is ensured once and nothing is logged`() {
        // Given
        val folder = FakeCodeCacheFolder()
        val classUnderTest = CodeCacheMark(folder) { warnings.add(it) }

        // When
        classUnderTest.keep()

        // Then
        assertEquals(1, folder.ensureCalls)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `Given ensuring throws when kept then it is caught and only its kind is logged`() {
        // Given
        val classUnderTest = CodeCacheMark(
            FakeCodeCacheFolder(failure = IllegalStateException(PRIVATE_TEXT))
        ) { warnings.add(it) }

        // When
        classUnderTest.keep()

        // Then
        assertEquals(listOf("code_cache check failed: IllegalStateException"), warnings)
    }

    @Test
    fun `Given ensuring throws a security exception when kept then it is caught`() {
        // Given
        val classUnderTest = CodeCacheMark(
            FakeCodeCacheFolder(failure = SecurityException(PRIVATE_TEXT))
        ) { warnings.add(it) }

        // When
        classUnderTest.keep()

        // Then
        assertEquals(listOf("code_cache check failed: SecurityException"), warnings)
    }

    @Test
    fun `Given ensuring throws a checked exception when kept then it is caught`() {
        // Given
        val classUnderTest = CodeCacheMark(
            FakeCodeCacheFolder(failure = IOException(PRIVATE_TEXT))
        ) { warnings.add(it) }

        // When
        classUnderTest.keep()

        // Then
        assertEquals(listOf("code_cache check failed: IOException"), warnings)
    }

    @Test
    fun `Given ensuring fails when kept then no message text reaches the log`() {
        // Given
        val classUnderTest = CodeCacheMark(
            FakeCodeCacheFolder(failure = RuntimeException(PRIVATE_TEXT, IOException(PRIVATE_TEXT)))
        ) { warnings.add(it) }

        // When
        classUnderTest.keep()

        // Then
        assertTrue(warnings.none { it.contains(PRIVATE_TEXT) })
    }

    private class FakeCodeCacheFolder(private val failure: Exception? = null) : CodeCacheFolder {
        var ensureCalls = 0
            private set

        override fun ensure() {
            ensureCalls++
            failure?.let { throw it }
        }
    }

    private companion object {
        const val PRIVATE_TEXT = "/data/user/0/com.migrely.app/databases/migrely.db Sumatriptan"
    }
}
