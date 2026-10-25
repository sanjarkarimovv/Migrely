package com.migrely.app.datasource.report.datasource

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Deleting every report (change add-settings-and-safety, BRD D96); writing them is tested on a device. */
class ReportDocumentDataSourceImplDeleteTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val classUnderTest by lazy { ReportDocumentDataSourceImpl(folder.root) { it.name } }

    @Test
    fun `When deleting every report then the reports go and the rest of the cache stays`() {
        File(folder.root, "reports/a/migrely-report.pdf").apply { parentFile?.mkdirs() }
            .writeText("pdf")
        File(folder.root, "reports/b/migrely-report.pdf.partial").apply { parentFile?.mkdirs() }
            .writeText("half")
        val other = File(folder.root, "other.txt").apply { writeText("kept") }

        classUnderTest.deleteAll()

        assertFalse(File(folder.root, "reports").exists())
        assertTrue(other.exists())
    }

    @Test
    fun `Given no report was ever made when deleting every report then nothing fails`() {
        classUnderTest.deleteAll()

        assertFalse(File(folder.root, "reports").exists())
    }
}
