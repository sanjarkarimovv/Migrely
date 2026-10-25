package com.migrely.app.backup

import com.migrely.app.datasource.preferences.datasource.PreferencesDataSourceImpl
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

private const val SHARED_PREFERENCES = "sharedpref"
private const val DATABASE = "database"

/** Google datatransport's database of the Play Billing library's usage logs (review M12). */
private val BILLING_LOGS = listOf("", "-journal", "-wal", "-shm")
    .map { "com.google.android.datatransport.events$it" }

/**
 * The premium entitlement is never backed up or moved to a new phone (BRD D115): a purchase belongs
 * to a Google account, and a new install asks Google Play for the account signed in there.
 */
class BackupRulesTest {
    private val entitlementFile = PreferencesDataSourceImpl.ENTITLEMENT_FILE + ".xml"

    private fun rules(name: String): Element = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(File("src/main/res/xml/$name"))
        .documentElement

    private fun Element.children(tag: String): List<Element> =
        (0 until childNodes.length).map(childNodes::item).filterIsInstance<Element>()
            .filter { it.tagName == tag }

    private fun Element.excludesEntitlement() = children("exclude").any {
        it.getAttribute("domain") == SHARED_PREFERENCES &&
            it.getAttribute("path") == entitlementFile
    }

    @Test
    fun `Given Android 11 and older then each preferences include has the entitlement's exclude`() {
        val content = rules("backup_rules.xml")
        val includes = content.children("include")
            .filter { it.getAttribute("domain") == SHARED_PREFERENCES }
        val excludes = content.children("exclude").filter {
            it.getAttribute("domain") == SHARED_PREFERENCES &&
                it.getAttribute("path") == entitlementFile
        }

        assertEquals(2, includes.size)
        assertEquals(includes.size, excludes.size)
    }

    @Test
    fun `Given Android 12 and later then cloud backup and device transfer leave it out`() {
        val rules = rules("data_extraction_rules.xml")

        listOf("cloud-backup", "device-transfer").forEach { section ->
            val element = rules.children(section).single()
            assertTrue(section, element.excludesEntitlement())
        }
    }

    private fun Element.excludedDatabases() = children("exclude")
        .filter { it.getAttribute("domain") == DATABASE }
        .map { it.getAttribute("path") }

    @Test
    fun `Given Android 11 and older then billing logs are left out beside each database include`() {
        val content = rules("backup_rules.xml")
        val includes = content.children("include").count { it.getAttribute("domain") == DATABASE }

        assertEquals(2, includes)
        BILLING_LOGS.forEach { file ->
            assertEquals(file, includes, content.excludedDatabases().count { it == file })
        }
    }

    @Test
    fun `Given Android 12 and later then both sections leave the billing logs out`() {
        val rules = rules("data_extraction_rules.xml")

        listOf("cloud-backup", "device-transfer").forEach { section ->
            assertEquals(
                section,
                BILLING_LOGS,
                rules.children(section).single().excludedDatabases()
            )
        }
    }
}
