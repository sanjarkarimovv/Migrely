package com.migrely.app.test.server.response

import com.migrely.app.test.server.MockResponse

sealed class ErrorResponseFactory {
    object NotFound : MockResponseFactory {
        override fun mockResponse() = MockResponse(code = 404)
    }
}
