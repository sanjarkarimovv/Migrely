package com.migrely.app.test.server

import com.migrely.app.test.server.response.MockResponseFactory

data class MockRequestResponseFactory(
    val request: MockRequest,
    val responseFactory: MockResponseFactory
)
