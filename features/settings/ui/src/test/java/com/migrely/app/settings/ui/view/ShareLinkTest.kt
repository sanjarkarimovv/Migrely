package com.migrely.app.settings.ui.view

import java.net.URI
import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The "In-app Settings, Share Migrely" row of `docs/release/play-links.md` (BRD D184), copied when this was built
 * (design D6). If the table changes, the app changes in a change of its own.
 */
private const val TABLE_URL = "https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=app" +
    "&utm_medium=share&utm_campaign=settings_share" +
    "&referrer=utm_source%3Dapp%26utm_medium%3Dshare%26utm_campaign%3Dsettings_share"

class ShareLinkTest {
    private fun parameters(query: String): Map<String, String> = query.split("&").associate {
        val (name, value) = it.split("=", limit = 2)
        name to value
    }

    @Test
    fun `The link is the table's Share Migrely link, byte for byte`() {
        assertEquals(TABLE_URL, SHARE_LINK)
    }

    @Test
    fun `The decoded referrer carries the same tags as the top level`() {
        val top = parameters(URI(SHARE_LINK).rawQuery)
        val referrer = parameters(URLDecoder.decode(top.getValue("referrer"), "UTF-8"))

        assertEquals(
            mapOf("utm_source" to "app", "utm_medium" to "share", "utm_campaign" to "settings_share"),
            referrer
        )
        assertEquals(referrer, top.filterKeys { it.startsWith("utm_") })
    }

    @Test
    fun `The link and the store page always name com migrely app, never the running package`() {
        assertEquals("com.migrely.app", parameters(URI(SHARE_LINK).rawQuery)["id"])
        assertEquals("market://details?id=com.migrely.app", STORE_PAGE_IN_PLAY)
        assertEquals("https://play.google.com/store/apps/details?id=com.migrely.app", STORE_PAGE_ON_WEB)
    }
}
