package com.migrely.app.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class LocalStore(val localStoreDataIds: Array<String>)
