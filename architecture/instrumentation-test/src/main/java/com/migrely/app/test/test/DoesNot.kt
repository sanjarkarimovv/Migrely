package com.migrely.app.test.test

import junit.framework.AssertionFailedError

fun doesNot(description: String, block: () -> Unit) {
    try {
        block()
        error("Unexpected: $description")
    } catch (_: AssertionFailedError) {
    }
}
