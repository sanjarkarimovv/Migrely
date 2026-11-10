package com.migrely.app.storage

import android.content.Context
import android.system.Os
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val MARK = "user.inode_code_cache"

/**
 * After Clear storage the `code_cache` folder is gone; keeping the mark re-creates it and installd's mark then holds its
 * real inode, so an update clears only `code_cache` (BRD D170, EC-STO-06). The folder's path is built here, not read
 * from `Context.codeCacheDir`, which would itself re-create the folder.
 */
@RunWith(AndroidJUnit4::class)
class ContextCodeCacheFolderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataFolder = context.dataDir
    private val codeCache = File(dataFolder, "code_cache")
    private val classUnderTest = CodeCacheMark(ContextCodeCacheFolder(context)) {}

    @Test
    fun givenTheFolderIsMissingWhenKeptThenTheMarkHoldsTheNewFolderInode() {
        // Given
        codeCache.deleteRecursively()
        assertFalse(codeCache.exists())

        // When
        classUnderTest.keep()

        // Then
        assertTrue(codeCache.isDirectory)
        assertEquals(Os.stat(codeCache.path).st_ino, markedInode())
    }

    @Test
    fun givenTheFolderExistsWhenKeptThenItsContentsAndTheMarkStay() {
        // Given
        classUnderTest.keep()
        val content = File(codeCache, "kept.txt").apply { writeText("kept") }
        val inodeBefore = Os.stat(codeCache.path).st_ino

        // When
        classUnderTest.keep()

        // Then
        assertEquals(inodeBefore, Os.stat(codeCache.path).st_ino)
        assertEquals(inodeBefore, markedInode())
        assertEquals("kept", content.readText())
        content.delete()
    }

    private fun markedInode(): Long =
        ByteBuffer.wrap(Os.getxattr(dataFolder.path, MARK)).order(ByteOrder.nativeOrder()).long
}
