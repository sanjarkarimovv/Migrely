package com.migrely.app.storage

import android.content.Context
import android.system.Os
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val MARK = "user.inode_code_cache"
private const val CONTENT = "kept.txt"

/**
 * After Clear storage the `code_cache` folder is gone; keeping the mark re-creates it and installd's mark then holds its
 * real inode, so an update clears only `code_cache` (BRD D170, EC-STO-06). The folder's path is built here, not read
 * from `Context.codeCacheDir`, which would itself re-create the folder. A stale mark is written from the app's own
 * process, as `ContextImpl` writes the real one: the data folder's own inode, which `code_cache` can never take, so the
 * test cannot pass by the folder getting its old inode back.
 */
@RunWith(AndroidJUnit4::class)
class ContextCodeCacheFolderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataFolder = context.dataDir
    private val codeCache = File(dataFolder, "code_cache")
    private val classUnderTest = CodeCacheMark(ContextCodeCacheFolder(context)) {}

    @Test
    fun givenTheFolderIsMissingAndTheMarkIsStaleWhenKeptThenTheMarkHoldsTheNewFolderInode() {
        // Given
        codeCache.deleteRecursively()
        assertFalse(codeCache.exists())
        val staleInode = Os.stat(dataFolder.path).st_ino
        writeMark(staleInode)
        assertEquals(staleInode, markedInode())

        // When
        classUnderTest.keep()

        // Then
        assertNotEquals(staleInode, markedInode())
        assertEquals(Os.stat(codeCache.path).st_ino, markedInode())
        assertTrue(codeCache.isDirectory)
    }

    @Test
    fun givenTheFolderExistsWhenKeptThenItsContentsAndTheMarkStay() {
        // Given
        classUnderTest.keep()
        val content = File(codeCache, CONTENT).apply { writeText("kept") }
        val inodeBefore = Os.stat(codeCache.path).st_ino

        // When
        classUnderTest.keep()

        // Then
        assertEquals(inodeBefore, Os.stat(codeCache.path).st_ino)
        assertEquals(inodeBefore, markedInode())
        assertEquals("kept", content.readText())
    }

    /** Never leaves the device without `code_cache` or with a stale mark, whatever failed. */
    @After
    fun tearDown() {
        File(context.codeCacheDir, CONTENT).delete()
        writeMark(Os.stat(codeCache.path).st_ino)
    }

    private fun writeMark(inode: Long) {
        val value = ByteBuffer.allocate(Long.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .putLong(inode)
            .array()
        Os.setxattr(dataFolder.path, MARK, value, 0)
    }

    private fun markedInode(): Long =
        ByteBuffer.wrap(Os.getxattr(dataFolder.path, MARK)).order(ByteOrder.nativeOrder()).long
}
