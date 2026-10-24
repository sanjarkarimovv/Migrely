package com.migrely.app.test.server.response

import com.migrely.app.test.server.MockResponse

interface MockResponseFactory {
    fun mockResponse(): MockResponse
}
