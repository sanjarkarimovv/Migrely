package com.migrely.app.datasource.document.datasource

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.exception.FileDataException
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

/** Picked files through the ContentResolver (change add-data-export-import). File URIs stand in for the picker's. */
@RunWith(AndroidJUnit4::class)
class DocumentDataSourceImplTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val classUnderTest = DocumentDataSourceImpl(context.contentResolver)

    private fun uriOf(name: String) = Uri.fromFile(
        File(context.cacheDir, name).apply {
            delete()
        }
    ).toString()

    @Test
    fun whenWrittenAndRead_thenTheSameText_asUtf8() {
        val uri = uriOf("doc.json")
        val text = "о‘zbek, é, ✓"

        classUnderTest.write(uri, "an older, longer text that must be replaced")
        classUnderTest.write(uri, text)

        assertEquals(text, classUnderTest.read(uri, 1_024))
    }

    @Test
    fun givenAFileLargerThanTheLimit_whenRead_thenAFileFailure() {
        val uri = uriOf("large.json")
        classUnderTest.write(uri, "x".repeat(10_000))

        assertThrows(FileDataException::class.java) { classUnderTest.read(uri, 9_999) }
    }

    @Test
    fun givenNoSuchFile_whenRead_thenAFileFailure() {
        assertThrows(FileDataException::class.java) {
            classUnderTest.read(uriOf("missing.json"), 1_024)
        }
    }
}
