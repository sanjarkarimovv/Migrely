package com.migrely.app.architecture.domain

import com.migrely.app.architecture.domain.exception.DomainException
import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.domain.usecase.UseCase

class UseCaseExecutor {
    fun <OUTPUT> execute(
        useCase: UseCase<Unit, OUTPUT>,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) = execute(useCase, Unit, onResult, onException)

    fun <INPUT, OUTPUT> execute(
        useCase: UseCase<INPUT, OUTPUT>,
        value: INPUT,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        try {
            useCase.execute(value, onResult)
        } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
            val domainException =
                ((throwable as? DomainException) ?: UnknownDomainException(throwable))
            onException(domainException)
        }
    }
}
