package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel

interface GetAutomaticCopyRepository {
    fun automaticCopy(): AutomaticCopyDomainModel
}
