package com.migrely.app.home.domain.model

import java.time.Instant

/** An attack deleted from its details screen that can still be restored (BRD D23). */
data class DeletedAttackDomainModel(val id: String, val deletedAt: Instant)
