package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.MigraineStartDomainModel

/** When each logged migraine started, deleted ones left out; null when they cannot be read. */
interface GetMigraineStartsRepository {
    fun migraineStarts(): List<MigraineStartDomainModel>?
}
