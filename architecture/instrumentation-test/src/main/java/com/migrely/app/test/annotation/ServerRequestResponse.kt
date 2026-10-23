package com.migrely.app.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class ServerRequestResponse(val requestResponseIds: Array<String>)
