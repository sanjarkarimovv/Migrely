package com.migrely.app.test.server

data class MockResponse(
    val upgradeToWebSocket: Boolean = false,
    val code: Int = 200,
    val headers: List<Pair<String, String>> = emptyList(),
    val body: String = ""
)
