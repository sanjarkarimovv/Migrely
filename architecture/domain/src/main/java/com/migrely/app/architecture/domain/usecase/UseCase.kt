package com.migrely.app.architecture.domain.usecase

interface UseCase<REQUEST, RESULT> {
    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
}
