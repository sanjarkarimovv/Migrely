package com.migrely.app.test.server.response

import com.migrely.app.test.server.MockResponse

class SequenceResponseFactory(private vararg val responses: MockResponseFactory) :
    MockResponseFactory {
    private var responseIndex = 0
    override fun mockResponse(): MockResponse {
        val mockResponse = responses[responseIndex]
        responseIndex++
        if (responseIndex == responses.size) {
            responseIndex = 0
        }
        return mockResponse.mockResponse()
    }
}
