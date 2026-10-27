package com.migrely.app.settings.ui.view

import androidx.compose.ui.platform.UriHandler
import org.junit.Assert.assertEquals
import org.junit.Test

class PolicyLinkTest {
    @Test
    fun `When a browser is there then the address opens`() {
        val opened = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }

        uriHandler.openInBrowserIfAny("https://migrely.health/privacy/")

        assertEquals(listOf("https://migrely.health/privacy/"), opened)
    }

    @Test
    fun `When no app opens web pages then nothing is thrown`() {
        // Compose's AndroidUriHandler reports a missing browser this way.
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) = throw IllegalArgumentException("Can't open $uri.")
        }

        uriHandler.openInBrowserIfAny("https://migrely.health/privacy/")
    }
}
